package tv.channelsurfer.app

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.WebSettings
import android.webkit.WebView

class TvWebViewSetup(private val webView: WebView) {

    @SuppressLint("SetJavaScriptEnabled")
    fun configureWebView() {
        val settings = webView.settings

        // Enable Javascript and DOM Storage
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        
        // Allow media playback without user gesture (essential for TV apps)
        settings.mediaPlaybackRequiresUserGesture = false

        // Modern web app settings
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.databaseEnabled = true
        settings.setSupportZoom(false)
        settings.displayZoomControls = false

        // Disable scrollbars for a clean TV experience
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
    }

    /**
     * Configures the given Activity to use Immersive Mode, completely hiding
     * the status bar and navigation bar to provide a true full-screen experience.
     */
    fun configureImmersiveMode(activity: Activity) {
        val window = activity.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
    }
}
