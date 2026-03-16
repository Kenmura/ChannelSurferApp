package tv.channelsurfer.app

import android.content.pm.ActivityInfo
import android.view.KeyEvent
import android.webkit.WebView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for MainActivity on Android TV emulator.
 *
 * Run with:
 *   ./gradlew connectedAndroidTest
 *
 * Requires a running TV emulator (API 21+, android.software.leanback feature).
 * In Android Studio: AVD Manager → Create Device → TV → select a system image.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    // -------------------------------------------------------------------------
    // Launch & lifecycle
    // -------------------------------------------------------------------------

    @Test
    fun activityLaunches_withoutCrash() {
        activityRule.scenario.onActivity { activity ->
            assertNotNull(activity)
            assertFalse("Activity should not be finishing immediately after launch", activity.isFinishing)
        }
    }

    @Test
    fun activityRecreation_doesNotCrash() {
        // Simulates HDMI display switch or language change (configChanges not covering all cases)
        activityRule.scenario.recreate()
        activityRule.scenario.onActivity { activity ->
            assertFalse("Activity should survive recreation", activity.isFinishing)
        }
        onView(withId(R.id.webview)).check(matches(isDisplayed()))
    }

    // -------------------------------------------------------------------------
    // Layout & initial view state
    // -------------------------------------------------------------------------

    @Test
    fun webView_isDisplayed() {
        onView(withId(R.id.webview)).check(matches(isDisplayed()))
    }

    @Test
    fun errorText_isInitiallyHidden() {
        // Error view should only appear after a main-frame network error
        onView(withId(R.id.error_text))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // -------------------------------------------------------------------------
    // TV-specific: orientation & focus
    // -------------------------------------------------------------------------

    @Test
    fun activity_isLockedToLandscape() {
        activityRule.scenario.onActivity { activity ->
            assertEquals(
                "TV activities must be landscape — remote navigation assumes fixed layout",
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                activity.requestedOrientation
            )
        }
    }

    @Test
    fun webView_hasFocus_forRemoteNavigation() {
        // WebView must hold focus so D-pad events are routed to it without a tap
        activityRule.scenario.onActivity { activity ->
            val webView = activity.findViewById<WebView>(R.id.webview)
            assertTrue("WebView must have focus for TV remote D-pad navigation", webView.hasFocus())
        }
    }

    // -------------------------------------------------------------------------
    // WebView settings required for TV media playback
    // -------------------------------------------------------------------------

    @Test
    fun webView_javaScriptIsEnabled() {
        activityRule.scenario.onActivity { activity ->
            val webView = activity.findViewById<WebView>(R.id.webview)
            assertTrue(
                "JavaScript must be enabled — channelsurfer.tv is a JS app",
                webView.settings.javaScriptEnabled
            )
        }
    }

    @Test
    fun webView_mediaPlayback_doesNotRequireUserGesture() {
        // On TV there is no touch gesture — video must autoplay
        activityRule.scenario.onActivity { activity ->
            val webView = activity.findViewById<WebView>(R.id.webview)
            assertFalse(
                "mediaPlaybackRequiresUserGesture must be false for TV autoplay",
                webView.settings.mediaPlaybackRequiresUserGesture
            )
        }
    }

    @Test
    fun webView_domStorageIsEnabled() {
        activityRule.scenario.onActivity { activity ->
            val webView = activity.findViewById<WebView>(R.id.webview)
            assertTrue("DOM storage must be enabled for session/localStorage", webView.settings.domStorageEnabled)
        }
    }

    // -------------------------------------------------------------------------
    // URL loading
    // -------------------------------------------------------------------------

    @Test
    fun webView_loadsChannelSurferUrl() {
        // Poll up to 5 s for WebView to begin navigation (loadUrl is async)
        var url: String? = null
        val deadline = System.currentTimeMillis() + 5_000L
        while (url == null && System.currentTimeMillis() < deadline) {
            activityRule.scenario.onActivity { activity ->
                val wv = activity.findViewById<WebView>(R.id.webview)
                url = wv.url ?: wv.originalUrl
            }
            if (url == null) Thread.sleep(200)
        }

        assertNotNull("WebView should begin loading within 5 seconds of launch", url)
        assertTrue(
            "WebView should load channelsurfer.tv (got: $url)",
            url!!.contains("channelsurfer.tv")
        )
    }

    // -------------------------------------------------------------------------
    // TV remote input — verify no crash and events are consumed/delegated
    // -------------------------------------------------------------------------

    @Test
    fun remoteControl_dpadKeys_areHandledWithoutCrash() {
        activityRule.scenario.onActivity { activity ->
            listOf(
                KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_RIGHT,
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER,
            ).forEach { keyCode ->
                activity.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                activity.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
            }
        }
    }

    @Test
    fun remoteControl_mediaKeys_areInjectedAsJsEventsWithoutCrash() {
        activityRule.scenario.onActivity { activity ->
            listOf(
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                KeyEvent.KEYCODE_MEDIA_PLAY,
                KeyEvent.KEYCODE_MEDIA_PAUSE,
                KeyEvent.KEYCODE_MEDIA_STOP,
                KeyEvent.KEYCODE_MENU,
            ).forEach { keyCode ->
                activity.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                activity.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
            }
        }
    }

    @Test
    fun backButton_withNoHistory_doesNotCrash() {
        // With no back-stack in WebView the back event should fall through to the system
        activityRule.scenario.onActivity { activity ->
            activity.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK))
            activity.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK))
            // Activity may finish here — that is the intended behaviour
        }
    }
}
