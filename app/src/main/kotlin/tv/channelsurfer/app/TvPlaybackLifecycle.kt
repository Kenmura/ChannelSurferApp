package tv.channelsurfer.app

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * A standalone lifecycle observer that manages TV-specific playback requirements:
 * 1. Keeping the screen awake (FLAG_KEEP_SCREEN_ON) while the app is visible.
 * 2. Requesting and abandoning Android Audio Focus to play nicely with other TV applications.
 */
class TvPlaybackLifecycle(
    private val context: Context,
    private val window: Window
) : DefaultLifecycleObserver {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        
        // Keep the TV screen awake while our app is in the foreground performing playback
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        requestAudioFocus()
    }

    override fun onStop(owner: LifecycleOwner) {
        // Allow the TV screen to turn off / screensaver to engage when backgrounded
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        abandonAudioFocus()
        
        super.onStop(owner)
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener {
                    // Handle audio focus loss/gain (e.g., pausing webview playback on loss)
                }
                .build().also { request ->
                    audioManager.requestAudioFocus(request)
                }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                { /* Handle audio focus changes */ },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { request ->
                audioManager.abandonAudioFocusRequest(request)
                audioFocusRequest = null
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }
}
