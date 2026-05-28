# Convene — Hybrid Mobile PoC

A Proof of Concept for a hybrid mobile application in the domain of event and conference management.
The app demonstrates the integration layer between a Web Layer and a Native Layer,
using Kotlin Multiplatform (KMP) as the architectural foundation.

Implemented for both **Android** and **iOS**.

---

## Running Instructions

### Android
1. Clone the repository
2. Open the project in Android Studio (latest stable)
3. Wait for Gradle sync to complete
4. Run the `androidApp` configuration on a device or emulator (API 26+)
5. For biometric testing on emulator: Extended Controls → Fingerprint → Touch Sensor

### iOS
1. Run in Terminal: `./gradlew :sharedLogic:linkDebugFrameworkIosSimulatorArm64`
2. Open `iosApp/iosApp.xcodeproj` in Xcode
3. Add `SharedLogic.framework` under Frameworks, Libraries, and Embedded Content
4. Run on iOS Simulator (iPhone 16 recommended)
5. For Face ID: Simulator → Features → Face ID → Enrolled → Matching Face

---

## Architecture

```
Convene/
├── sharedLogic/
│   ├── commonMain/
│   │   ├── model/          # Event data class
│   │   ├── repository/     # EventRepository
│   │   └── storage/        # StorageApi interface
│   ├── androidMain/        # AndroidStorage (EncryptedSharedPreferences)
│   └── iosMain/            # IosStorage (in-memory stub)
│
├── androidApp/
│   ├── MainActivity.kt         # WebView host + Biometric
│   ├── WebAppInterface.kt      # JS Bridge (@JavascriptInterface)
│   ├── NotificationHelper.kt   # Local notifications
│   └── assets/index.html       # Shared Web Layer
│
└── iosApp/
    └── ContentView.swift        # WKWebView host + Face ID bridge
```

### Why KMP?
KMP allows sharing business logic (models, repository, storage interface) across platforms
while keeping Native capabilities (biometric, storage, notifications) platform-specific.
Adding full iOS persistence requires only implementing `StorageApi` via Keychain —
the rest of the logic is already shared.

---

## Web ↔ Native Communication

### Android
```javascript
// Web -> Native
Android.loadEvents()
Android.saveEvent(json)
Android.toggleFavorite(id)
Android.requestBiometric()

// Native -> Web
webView.evaluateJavascript("onEventsLoaded('$json')", null)
webView.evaluateJavascript("onAuthResult(true)", null)
```

### iOS
```javascript
// Web -> Native
window.webkit.messageHandlers.loadEvents.postMessage('')
window.webkit.messageHandlers.saveEvent.postMessage(json)

// Native -> Web
webView.evaluateJavaScript("onEventsLoaded('\(json)')")
webView.evaluateJavaScript("onAuthResult(\(success))")
```

### Cross-platform detection in JS
```javascript
const isIOS = !!(window.webkit && window.webkit.messageHandlers);

function callNative(method, data) {
    if (isIOS) {
        window.webkit.messageHandlers[method].postMessage(data || '');
    } else {
        if (data !== undefined) Android[method](data);
        else Android[method]();
    }
}
```

---

## Native Capabilities

| Capability | Android | iOS |
|------------|---------|-----|
| Biometric Auth | BiometricPrompt | LocalAuthentication (Face ID) |
| Secure Storage | EncryptedSharedPreferences | In-memory stub (Keychain ready) |
| Local Notifications | NotificationManager | - |
| Lifecycle | onResume → onAppResumed() | - |

---

## Key Technical Decisions

| Decision | Rationale |
|----------|-----------|
| KMP over React Native/Flutter | Native-first thinking with shared logic |
| WebView Bridge over Capacitor | Full control over the bridge, explicit and inspectable |
| Shared `index.html` | One Web Layer for both platforms |
| `StorageApi` interface | Platform-specific implementations without touching shared logic |
| Package structure (model/repository/storage) | Clean separation of concerns |

---

## Tradeoffs & Limitations

- `JavascriptInterface` is single-threaded — heavy operations can block the UI thread
- No type safety across the bridge — JS and Kotlin/Swift communicate via raw JSON strings
- `EncryptedSharedPreferences` is deprecated — DataStore would be preferred in production
- iOS storage is in-memory only — data is lost on app restart (Keychain implementation ready to add)
- `setJavaScriptEnabled(true)` requires hardening in production (WebViewAssetLoader)

---

## Future Production-Grade Solution

- Replace WebView Bridge with **Capacitor.js** for typed plugin system
- Use **TypeScript** on the Web Layer for type-safe bridge communication
- Replace storage with **DataStore** (Android) and **Keychain** (iOS)
- Add **CI/CD pipeline** with GitHub Actions
- Add **unit tests** for repository and bridge logic
- Use **WebViewAssetLoader** for enhanced security