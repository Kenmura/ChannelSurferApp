package tv.channelsurfer.app

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var splashImage: ImageView
    private lateinit var errorText: TextView
    private lateinit var remoteInputManager: RemoteInputManager

    companion object {
        private const val URL = "https://channelsurfer.tv"
        private const val STATE_URL = "current_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register TV playback lifecycle 
        lifecycle.addObserver(TvPlaybackLifecycle(this, window))

        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        splashImage = findViewById(R.id.splash_image)
        errorText = findViewById(R.id.error_text)

        val webViewSetup = TvWebViewSetup(webView)
        webViewSetup.configureWebView()
        webViewSetup.configureImmersiveMode(this)

        setupWebViewClients()
        
        remoteInputManager = RemoteInputManager(webView)

        val urlToLoad = savedInstanceState?.getString(STATE_URL) ?: URL
        webView.loadUrl(urlToLoad)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebViewClients() {
        webView.setBackgroundColor(Color.BLACK)

        // Cache for offline resilience
        webView.settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                splashImage.visibility = View.GONE
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    errorText.visibility = View.VISIBLE
                    splashImage.visibility = View.GONE
                }
            }
        }

        webView.webChromeClient = WebChromeClient()

        webView.isFocusable = true
        webView.isFocusableInTouchMode = true
        webView.requestFocus()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (remoteInputManager.handleKeyEvent(event)) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_URL, webView.url)
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
