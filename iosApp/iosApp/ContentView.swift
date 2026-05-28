import LocalAuthentication
import SharedLogic
import SwiftUI
import WebKit

struct ContentView: View {
    var body: some View {
        ConveneWebView()
            .ignoresSafeArea()
    }
}

// WKWebView Wrapper
struct ConveneWebView: UIViewRepresentable {

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    func makeUIView(context: Context) -> WKWebView {
        let userContentController = WKUserContentController()

        // Register Native handlers — Web → Native
        userContentController.add(context.coordinator, name: "loadEvents")
        userContentController.add(context.coordinator, name: "saveEvent")
        userContentController.add(context.coordinator, name: "toggleFavorite")
        userContentController.add(context.coordinator, name: "requestBiometric")

        let config = WKWebViewConfiguration()
        config.userContentController = userContentController

        let webView = WKWebView(frame: .zero, configuration: config)
        context.coordinator.webView = webView

        // Load index.html from bundle
        if let url = Bundle.main.url(forResource: "index", withExtension: "html") {
            webView.loadFileURL(url, allowingReadAccessTo: url.deletingLastPathComponent())
        }

        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {}
}

// Native Bridge (Web -> Native)
class Coordinator: NSObject, WKScriptMessageHandler {

    weak var webView: WKWebView?
    private let repository = EventRepository(storage: IosStorage())

    override init() {
        super.init()
        repository.seedDemoData()
    }

    func userContentController(
        _ userContentController: WKUserContentController,
        didReceive message: WKScriptMessage
    ) {
        switch message.name {
        case "loadEvents":
            loadEvents()
        case "saveEvent":
            if let json = message.body as? String {
                saveEvent(json: json)
            }
        case "toggleFavorite":
            if let id = message.body as? String {
                repository.toggleFavorite(eventId: id)
                loadEvents()
            }
        case "requestBiometric":
            showBiometricPrompt()
        default:
            break
        }
    }

    // MARK: - Native -> Web
    private func loadEvents() {
        let events = repository.getAllEvents()
        let json = encodeEvents(events)
        let escaped = json.replacingOccurrences(of: "'", with: "\\'")
        DispatchQueue.main.async {
            self.webView?.evaluateJavaScript("onEventsLoaded('\(escaped)')", completionHandler: nil)
        }
    }

    private func saveEvent(json: String) {
        if let event = decodeEvent(json) {
            repository.saveEvent(event: event)
            loadEvents()
        }
    }

    private func showBiometricPrompt() {
        // Biometric on iOS handled via LocalAuthentication
        let context = LAContext()
        var error: NSError?

        if context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) {
            context.evaluatePolicy(
                .deviceOwnerAuthenticationWithBiometrics,
                localizedReason: "Authenticate to save session"
            ) { success, _ in
                DispatchQueue.main.async {
                    self.webView?.evaluateJavaScript("onAuthResult(\(success))", completionHandler: nil)
                }
            }
        } else {
            DispatchQueue.main.async {
                self.webView?.evaluateJavaScript("onAuthResult(false)", completionHandler: nil)
            }
        }
    }

    // Helpers
    private func encodeEvents(_ events: [Event]) -> String {
        let array = events.map { event in
            """
            {"id":"\(event.id)","title":"\(event.title)","speaker":"\(event.speaker)","room":"\(event.room)","timeMs":\(event.timeMs),"isFavorite":\(event.isFavorite)}
            """
        }
        return "[\(array.joined(separator: ","))]"
    }

    private func decodeEvent(_ json: String) -> Event? {
        guard let data = json.data(using: .utf8),
            let dict = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
            let id = dict["id"] as? String,
            let title = dict["title"] as? String,
            let speaker = dict["speaker"] as? String,
            let room = dict["room"] as? String,
            let timeMs = dict["timeMs"] as? Int64,
            let isFavorite = dict["isFavorite"] as? Bool
        else { return nil }

        return Event(id: id, title: title, speaker: speaker, room: room, timeMs: timeMs, isFavorite: isFavorite)
    }

    struct ContentView_Previews: PreviewProvider {
        static var previews: some View {
            ContentView()
        }
    }
}
