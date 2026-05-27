package com.example.convene

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.convene.repository.EventRepository
import com.example.convene.storage.AndroidStorage
import androidx.biometric.BiometricPrompt

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var repository: EventRepository
    private lateinit var biometricPrompt: BiometricPrompt

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup repository with Android secure storage
        repository = EventRepository(AndroidStorage(this))
        repository.seedDemoData()

        // Setup WebView
        webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()

        // Register the bridge between Web and Native
        webView.addJavascriptInterface(
            WebAppInterface(this, webView, repository),
            "Android"
        )

        webView.loadUrl("file:///android_asset/index.html")

        setupBiometric()
    }

    // Lifecycle: notify Web layer when app resumes
    override fun onResume() {
        super.onResume()
        webView.evaluateJavascript("onAppResumed()", null)
    }

    // Biometric setup
    private fun setupBiometric() {
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    // Native → Web: authentication result
                    webView.evaluateJavascript("onAuthResult(true)", null)
                }
                override fun onAuthenticationFailed() {
                    webView.evaluateJavascript("onAuthResult(false)", null)
                }
                override fun onAuthenticationError(code: Int, msg: CharSequence) {
                    webView.evaluateJavascript("onAuthResult(false)", null)
                }
            })
    }

    fun showBiometricPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirm Identity")
            .setSubtitle("Authenticate to save session")
            .setNegativeButtonText("Cancel")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }
}