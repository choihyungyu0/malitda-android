package kr.voicemate.malitda.domain

import kr.voicemate.malitda.data.settings.ConfirmationMode
import kr.voicemate.malitda.data.settings.ListeningTimeLimit
import kr.voicemate.malitda.data.settings.ProfilePreferences
import org.junit.Assert.*
import org.junit.Test

class ProfilePreferencesTest {
    @Test fun `one two or three visible candidates remain explicit user choices`() {
        (1..3).forEach { count -> assertEquals(count, ProfilePreferences(candidateCount = count).normalized().candidateCount) }
        assertEquals(1, ProfilePreferences(candidateCount = -3).normalized().candidateCount)
        assertEquals(3, ProfilePreferences(candidateCount = 999).normalized().candidateCount)
    }

    @Test fun `corrupted preferences fall back safely`() {
        assertEquals(ConfirmationMode.PICTURE, ConfirmationMode.parse("unknown"))
        assertEquals(ConfirmationMode.TTS, ConfirmationMode.parse("TTS"))
        assertEquals(1f, ProfilePreferences(ttsRate = Float.NaN).normalized().ttsRate)
        assertEquals(1.2f, ProfilePreferences(ttsRate = 999f).normalized().ttsRate)
    }

    @Test fun `preference change does not imply approval or enable pilot logging`() {
        val defaults = ProfilePreferences()
        val text = defaults.copy(confirmationMode = ConfirmationMode.TEXT)
        assertEquals(3, text.candidateCount)
        assertTrue(text.haptics)
        assertTrue(text.allowSimilarExpressions)
        assertEquals(1f, text.ttsRate)
    }

    @Test fun `listening defaults to twenty seconds and keeps all explicit choices`() {
        assertEquals(20, ProfilePreferences().listenTimeLimitSeconds)
        listOf(20, 60, 0).forEach { seconds ->
            assertEquals(seconds, ProfilePreferences(listenTimeLimitSeconds = seconds).normalized().listenTimeLimitSeconds)
        }
    }

    @Test fun `damaged listening limits fall back to twenty seconds`() {
        listOf(Int.MIN_VALUE, -1, 1, 19, 21, 59, 61, Int.MAX_VALUE).forEach { seconds ->
            assertEquals(20, ProfilePreferences(listenTimeLimitSeconds = seconds).normalized().listenTimeLimitSeconds)
            assertEquals(ListeningTimeLimit.TWENTY_SECONDS, ListeningTimeLimit.fromSeconds(seconds))
        }
    }

    @Test fun `manual ending has no automatic deadline and its guidance says so`() {
        val manual = ListeningTimeLimit.fromSeconds(0)
        assertEquals(ListeningTimeLimit.MANUAL, manual)
        assertEquals(0, manual.seconds)
        assertNull(manual.autoStopDelayMillis)
        assertEquals(20_000L, ListeningTimeLimit.fromSeconds(20).autoStopDelayMillis)
        assertEquals(60_000L, ListeningTimeLimit.fromSeconds(60).autoStopDelayMillis)
        assertTrue(manual.guidance.contains("자동으로 끝나지 않아요"))
        assertTrue(manual.guidance.contains("말하기 끝내기"))
        assertTrue(ListeningTimeLimit.fromSeconds(20).guidance.contains("20초"))
        assertTrue(ListeningTimeLimit.fromSeconds(60).guidance.contains("60초"))
    }
}
