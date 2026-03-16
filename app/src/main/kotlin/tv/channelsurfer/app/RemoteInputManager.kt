package tv.channelsurfer.app

import android.view.KeyEvent
import android.webkit.WebView

/**
 * Isolated helper class to intercept and manage hardware key events from a Smart TV remote.
 * Translates specific keys to JavaScript window events or delegates to standard WebView focus navigation.
 */
class RemoteInputManager(private val webView: WebView) {

    /**
     * Should be called from Activity.dispatchKeyEvent.
     * Returns true if the key event was consumed, false to let the system handle it.
     */
    fun handleKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action

        // Handle Back button explicitly
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (action == KeyEvent.ACTION_DOWN && webView.canGoBack()) {
                webView.goBack()
                return true
            }
            // Let the system handle it (e.g., exiting the app) if we can't go back
            return false
        }

        // Map Android TV hardware keys to web KeyboardEvent keys
        val jsKeyMapping = mapOf(
            KeyEvent.KEYCODE_MENU to "ContextMenu",
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE to "MediaPlayPause",
            KeyEvent.KEYCODE_MEDIA_PLAY to "MediaPlay",
            KeyEvent.KEYCODE_MEDIA_PAUSE to "MediaPause",
            KeyEvent.KEYCODE_MEDIA_STOP to "MediaStop"
        )

        val jsKey = jsKeyMapping[keyCode]
        if (jsKey != null) {
            // Inject a Javascript KeyboardEvent for specific keys (down and up)
            val eventType = if (action == KeyEvent.ACTION_DOWN) "keydown" else "keyup"
            
            // Only fire keyup and keydown for specific actions
            if (action == KeyEvent.ACTION_DOWN || action == KeyEvent.ACTION_UP) {
                injectJavaScriptKeyEvent(eventType, jsKey, keyCode)
            }
            return true
        }

        // For D-Pad (Up, Down, Left, Right, Center) and Enter,
        // delegate to standard WebView dispatch for native DOM focus navigation.
        val isDPadOrEnter = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER -> true
            else -> false
        }

        if (isDPadOrEnter) {
            // Let the WebView handle focus navigation
            return webView.dispatchKeyEvent(event)
        }

        return false
    }

    private fun injectJavaScriptKeyEvent(type: String, key: String, nativeKeyCode: Int) {
        val jsCode = """
            (function() {
                var event = new KeyboardEvent('$type', {
                    key: '$key',
                    code: '$key',
                    keyCode: $nativeKeyCode,
                    which: $nativeKeyCode,
                    bubbles: true,
                    cancelable: true
                });
                document.dispatchEvent(event);
            })();
        """.trimIndent()
        
        webView.evaluateJavascript(jsCode, null)
    }
}
