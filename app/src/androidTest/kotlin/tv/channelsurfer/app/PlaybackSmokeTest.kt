package tv.channelsurfer.app

import android.view.View
import android.webkit.WebView
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * 5-second end-to-end smoke test.
 *
 * Sequence:
 *   1. Launch the app; wait up to 5 s for channelsurfer.tv to finish loading.
 *   2. Tap the centre of the screen — the trusted pointer event that
 *      channelsurfer.tv requires to dismiss its "PRESS TO START" overlay.
 *   3. Wait 2 s for the YouTube player to initialise.
 *   4. Assert the overlay is gone and the player has surfaced channel info
 *      (program title, "Ch N – …" line), confirming playback began.
 *
 * Why a tap and not a D-pad keypress:
 *   channelsurfer.tv's start gate checks event.isTrusted and requires a real
 *   pointer interaction. JavaScript-dispatched click/keyboard events are
 *   blocked (isTrusted == false). UiDevice.click() injects a trusted
 *   MotionEvent at the OS level, exactly as a real touch would.
 *
 * Requires the TV emulator to have an active internet connection.
 * Run with: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class PlaybackSmokeTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun smokeTest_playbackStartsWithinFiveSeconds() {
        // ── 1. Wait up to 5 s for page to finish loading ──────────────────────
        var pageLoaded = false
        val loadDeadline = System.currentTimeMillis() + 5_000L
        while (!pageLoaded && System.currentTimeMillis() < loadDeadline) {
            activityRule.scenario.onActivity { activity ->
                pageLoaded = activity.findViewById<View>(R.id.splash_image).visibility == View.GONE
            }
            if (!pageLoaded) Thread.sleep(200)
        }

        var errorVisible = false
        activityRule.scenario.onActivity { activity ->
            errorVisible = activity.findViewById<View>(R.id.error_text).visibility == View.VISIBLE
        }
        assertFalse(
            "Network error shown — verify the TV emulator has an active internet connection",
            errorVisible
        )
        assertTrue("channelsurfer.tv did not finish loading within 5 seconds", pageLoaded)

        // ── 2. Tap screen centre to start playback ────────────────────────────
        // channelsurfer.tv shows "PRESS TO START" until a trusted pointer event
        // is received. UiDevice.click() injects a real MotionEvent (isTrusted=true)
        // at the OS level — the same thing a physical remote "select" generates
        // via the TV's pointer emulation layer.
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.click(device.displayWidth / 2, device.displayHeight / 2)

        // ── 3. Wait 2 s for the YouTube player to initialise ──────────────────
        Thread.sleep(2_000)

        // ── 4. Assert playback is active ──────────────────────────────────────
        val latch = CountDownLatch(1)
        val jsValue = AtomicReference<String>()
        activityRule.scenario.onActivity { activity ->
            activity.findViewById<WebView>(R.id.webview)
                .evaluateJavascript("document.body ? document.body.innerText : ''") {
                    v -> jsValue.set(v); latch.countDown()
                }
        }
        assertTrue("JavaScript evaluation timed out", latch.await(2, TimeUnit.SECONDS))

        val bodyText = jsValue.get()?.removeSurrounding("\"") ?: ""

        // "PRESS TO START" disappears once the player takes over
        assertFalse(
            "The 'PRESS TO START' overlay should be gone after tapping " +
            "(body snippet: '${bodyText.take(150)}')",
            bodyText.contains("PRESS TO START", ignoreCase = true)
        )

        // The player surfaces the current channel line, e.g. "Ch 19 – Lofi Car"
        assertTrue(
            "Channel info should be visible once playback starts " +
            "(body snippet: '${bodyText.take(150)}')",
            bodyText.contains("Ch ", ignoreCase = false)
        )
    }
}
