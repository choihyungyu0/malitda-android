package kr.voicemate.malitda

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kr.voicemate.malitda.data.db.MeaningEntity
import kr.voicemate.malitda.ui.testing.CoreQaActivity
import kr.voicemate.malitda.ui.theme.A11yPrefs
import kr.voicemate.malitda.ui.theme.MalitdaTheme
import kr.voicemate.malitda.ui.v2.U03Confirm
import kr.voicemate.malitda.ui.v2.U03Unsure
import kr.voicemate.malitda.ui.vm.SessionViewModel.V2Origin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Isolated rendering and callback probe for the frozen surface-C composables.
 * This is not MainActivity navigation or an end-to-end approval/persistence test.
 * Only synthetic text and local drawable resources are used: no DB, mic, network, or TTS engine.
 */
@RunWith(AndroidJUnit4::class)
class FinalSubmissionSurfaceCWarningProbeTest {
    private val inst = InstrumentationRegistry.getInstrumentation()
    private val yes = AtomicInteger()
    private val other = AtomicInteger()
    private val unsure = AtomicInteger()
    private val recheck = AtomicInteger()
    private val listen = AtomicInteger()
    private val swipes = AtomicInteger()
    private val output by lazy {
        val external = inst.targetContext.getExternalFilesDir(null)
            ?: error("Target external files directory is unavailable")
        File(external, "surface-c-warning-probe/${System.currentTimeMillis()}-${UUID.randomUUID()}")
            .apply { check(mkdirs()) { "Cannot create probe evidence directory: $this" } }
    }

    private val raw = "물을 1잔 주세요."
    private val longMeaning = "물을 2잔 주세요. 지금은 차가운 물을 마시고 싶어요. " +
        "컵을 책상 위에 놓아 주세요. 제가 천천히 마실 수 있도록 잠깐 기다려 주세요. " +
        "더 필요하면 제가 다시 알려 드릴게요."
    private val warning = "숫자나 부정 표현이 달라졌습니다. 원하는 뜻인지 다시 확인합니다."

    private fun nodes(node: AccessibilityNodeInfo?): List<AccessibilityNodeInfo> =
        if (node == null) emptyList()
        else listOf(node) + (0 until node.childCount).flatMap { nodes(node.getChild(it)) }

    private fun ownRoot(): AccessibilityNodeInfo? = inst.uiAutomation.rootInActiveWindow
        ?.takeIf { it.packageName?.toString() == inst.targetContext.packageName }

    private fun current() = nodes(ownRoot())

    private fun bounds(node: AccessibilityNodeInfo) = Rect().also(node::getBoundsInScreen)

    private fun visible(text: String): AccessibilityNodeInfo? = current().firstOrNull {
        it.isVisibleToUser && !bounds(it).isEmpty &&
            (it.text?.toString() == text || it.contentDescription?.toString() == text)
    }

    private fun waitFor(text: String): AccessibilityNodeInfo {
        repeat(50) {
            visible(text)?.let { return it }
            SystemClock.sleep(120)
        }
        error("Missing visible exact text: $text\n${nodeDump()}")
    }

    /** Touch the observed scroll node, rather than depending on ACTION_SCROLL_FORWARD. */
    private fun swipe(forward: Boolean) {
        checkNotNull(ownRoot()) { "Probe will not inject touches into another app" }
        val container = current().filter { it.isScrollable && it.isVisibleToUser }
            .maxByOrNull { bounds(it).let { rect -> rect.width().toLong() * rect.height() } }
            ?: error("No visible scroll container\n${nodeDump()}")
        val rect = bounds(container)
        check(rect.width() > 4 && rect.height() > 20) { "Unusable scroll bounds: $rect" }
        val x = rect.centerX().toFloat()
        val upper = rect.top + rect.height() * 0.18f
        val lower = rect.bottom - rect.height() * 0.18f
        val start = if (forward) lower else upper
        val end = if (forward) upper else lower
        val downTime = SystemClock.uptimeMillis()
        fun send(action: Int, y: Float) {
            val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, y, 0)
            try {
                event.source = InputDevice.SOURCE_TOUCHSCREEN
                assertTrue("Touch injection failed", inst.uiAutomation.injectInputEvent(event, true))
            } finally {
                event.recycle()
            }
        }
        send(MotionEvent.ACTION_DOWN, start)
        repeat(12) { step ->
            SystemClock.sleep(25)
            send(MotionEvent.ACTION_MOVE, start + (end - start) * (step + 1) / 12)
        }
        send(MotionEvent.ACTION_UP, end)
        swipes.incrementAndGet()
        SystemClock.sleep(350)
    }

    private fun scrollTo(text: String): AccessibilityNodeInfo {
        visible(text)?.let { return it }
        for (forward in listOf(true, false)) {
            repeat(18) {
                swipe(forward)
                visible(text)?.let { return it }
            }
        }
        error("Exact text remained unreachable after touch swipes: $text\n${nodeDump()}")
    }

    private fun click(text: String) {
        var node: AccessibilityNodeInfo? = scrollTo(text)
        while (node != null && !node.isClickable) node = node.parent
        assertNotNull("No clickable parent for: $text", node)
        val clickable = checkNotNull(node)
        assertTrue("Response is disabled: $text", clickable.isEnabled)
        assertTrue("Click failed: $text", clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        SystemClock.sleep(300)
    }

    private fun assertCallbacks(
        approvals: Int = 0,
        otherChoices: Int = 0,
        unsureChoices: Int = 0,
        rechecks: Int = 0,
        listens: Int = 0,
    ) {
        assertEquals("Approval callback", approvals, yes.get())
        assertEquals("Other-meaning callback", otherChoices, other.get())
        assertEquals("Unsure callback", unsureChoices, unsure.get())
        assertEquals("Recheck callback", rechecks, recheck.get())
        assertEquals("Listen callback", listens, listen.get())
    }

    private fun nodeDump() = current().joinToString("\n") {
        "visible=${it.isVisibleToUser} enabled=${it.isEnabled} clickable=${it.isClickable} " +
            "scrollable=${it.isScrollable} bounds=${bounds(it)} class=${it.className} " +
            "text=${it.text} description=${it.contentDescription}"
    }

    private fun capture(name: String) {
        checkNotNull(ownRoot()) { "Probe will not capture another app" }
        SystemClock.sleep(180)
        File(output, "$name.txt").writeText(nodeDump())
        val screenshot = inst.uiAutomation.takeScreenshot() ?: error("Screenshot unavailable")
        val small = if (screenshot.width > 720) Bitmap.createScaledBitmap(
            screenshot, 720, (screenshot.height.toLong() * 720 / screenshot.width).toInt(), true,
        ) else screenshot
        try {
            File(output, "$name.png").outputStream().use {
                check(small.compress(Bitmap.CompressFormat.PNG, 100, it)) { "PNG write failed" }
            }
        } finally {
            if (small !== screenshot) small.recycle()
            screenshot.recycle()
        }
    }

    @Test
    fun longSampleAtTwoHundredPercentKeepsWarningAndThreeChoicesReachable() {
        val sample = MeaningEntity(
            id = 1L, profileId = 1L, text = longMeaning, imageKey = "water", situation = null,
            confirmed = false, active = true, createdAt = 0L, updatedAt = 0L, useCount = 0,
            isSample = true,
        )
        val showUnsure = mutableStateOf(false)
        val scenario = ActivityScenario.launch(CoreQaActivity::class.java)
        try {
            File(output, "fixture.txt").writeText(
                "Isolated CoreQaActivity + real U03Confirm/U03Unsure; not MainActivity evidence.\n" +
                    "fontScale=2.0; origin=VOICE; isSample=true; confirmed=false\n" +
                    "heard=$raw\nmeaning=$longMeaning\n" +
                    "No database, microphone, network, or TTS engine. Listen uses a counter.\n",
            )
            scenario.onActivity { activity ->
                activity.setContent {
                    MalitdaTheme(fontScale = 2f, a11y = A11yPrefs(haptics = false)) {
                        if (showUnsure.value) {
                            U03Unsure(
                                meaning = sample,
                                onListen = { listen.incrementAndGet() },
                                onRecheck = { recheck.incrementAndGet(); showUnsure.value = false },
                                onPick = { other.incrementAndGet() },
                                onStop = {},
                                onHelp = {},
                            )
                        } else {
                            U03Confirm(
                                meaning = sample,
                                origin = V2Origin.VOICE,
                                studentName = "격리 검증",
                                heard = raw,
                                onListen = { listen.incrementAndGet() },
                                onYes = { yes.incrementAndGet() },
                                onOther = { other.incrementAndGet() },
                                onUnsure = { unsure.incrementAndGet(); showUnsure.value = true },
                                onBack = {},
                                onHelp = {},
                            )
                        }
                    }
                }
            }
            waitFor("이 뜻이 맞나요?")
            assertCallbacks()
            scrollTo(longMeaning)
            capture("01-long-synthetic-meaning")
            scrollTo("들은 말 · 원문\n$raw")
            capture("02-exact-heard-original")
            scrollTo(warning)
            capture("03-number-change-warning")
            scrollTo("연습용 예시 · 개인 확인 전")
            capture("04-sample-origin")

            // The original primary button includes its checkmark in the accessible Text node.
            scrollTo("✓  맞아요")
            scrollTo("다른 뜻이에요")
            scrollTo("잘 모르겠어요")
            capture("05-three-responses")
            assertTrue("Long-text probe must exercise real touch scrolling", swipes.get() > 0)
            assertCallbacks() // Rendering and scrolling alone must never approve or choose a response.

            click("소리로 확인하기")
            assertCallbacks(listens = 1)
            click("다른 뜻이에요")
            assertCallbacks(otherChoices = 1, listens = 1)
            click("잘 모르겠어요")
            waitFor("천천히 다시 확인해요")
            assertCallbacks(otherChoices = 1, unsureChoices = 1, listens = 1)
            scrollTo("함께 다시 확인")
            capture("06-unsure-recheck")
            click("함께 다시 확인")
            waitFor("이 뜻이 맞나요?")
            assertCallbacks(otherChoices = 1, unsureChoices = 1, rechecks = 1, listens = 1)
            scrollTo(warning)
            capture("07-warning-after-recheck")

            // Only this explicit final selection may dispatch the approval callback.
            click("✓  맞아요")
            assertCallbacks(approvals = 1, otherChoices = 1, unsureChoices = 1, rechecks = 1, listens = 1)
            capture("08-explicit-yes-callback")
            File(output, "result.txt").writeText(
                "PASS: exact raw/warning/sample text and all three responses reachable at fontScale=2.0.\n" +
                    "Approvals before explicit yes=0; after explicit yes=${yes.get()}.\n" +
                    "other=${other.get()}; unsure=${unsure.get()}; recheck=${recheck.get()}; " +
                    "listen=${listen.get()}; touchSwipes=${swipes.get()}.\n" +
                    "Other/recheck routing is a test harness; product navigation/storage is not asserted.\n",
            )
        } catch (failure: Throwable) {
            runCatching { File(output, "failure.txt").writeText(failure.stackTraceToString()) }
            runCatching { capture("failure") }
            throw failure
        } finally {
            scenario.close()
        }
    }
}
