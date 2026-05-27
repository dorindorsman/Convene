package com.example.convene

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.example.convene.model.Event
import com.example.convene.repository.EventRepository
import kotlinx.serialization.json.Json

class WebAppInterface(
    private val activity: MainActivity,
    private val webView: WebView,
    private val repository: EventRepository
) {

    // Web → Native: Load events
    @JavascriptInterface
    fun loadEvents() {
        val json = Json.encodeToString(repository.getAllEvents())
        activity.runOnUiThread {
            webView.evaluateJavascript("onEventsLoaded('${json.replace("'", "\\'")}' )", null)
        }
    }

    // Web → Native: Save event
    @JavascriptInterface
    fun saveEvent(eventJson: String) {
        val event = Json.decodeFromString<Event>(eventJson)
        repository.saveEvent(event)
    }

    // Web → Native: Toggle favorite
    @JavascriptInterface
    fun toggleFavorite(eventId: String) {
        repository.toggleFavorite(eventId)
    }

    // Web → Native: Request biometric authentication
    @JavascriptInterface
    fun requestBiometric() {
        activity.runOnUiThread {
            activity.showBiometricPrompt()
        }
    }
}