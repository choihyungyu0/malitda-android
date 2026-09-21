package kr.voicemate.malitda.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SentenceCleanupTest {
    @Test fun removesNonSpeechTokens() {
        assertEquals("월 세요", SentenceCleanup.clean("[2] 월 [7] 세요"))
        assertEquals("", SentenceCleanup.clean("[2] [7]"))
        assertEquals("네", SentenceCleanup.clean("<unk> 네"))
    }

    @Test fun attachesParticlesOncePerWord() {
        assertEquals("지금 학교에 가고 있어요", SentenceCleanup.clean("지금 학교 에 가 고 있어요"))
        assertEquals("오리 날씨 천마의 좋다", SentenceCleanup.clean("오리 날씨 천마 의 좋다"))
        assertEquals("잠시 후 연락 드릴게요", SentenceCleanup.clean("잠시 후 연락 드릴게요"))
    }

    @Test fun neverAttachesUnknownTokensOrFirstToken() {
        assertEquals("에 갑니다", SentenceCleanup.clean("에 갑니다"))
        assertEquals("친구 만나", SentenceCleanup.clean("친구 만나"))
    }

    @Test fun collapsesWhitespace() {
        assertEquals("네 알겠어요", SentenceCleanup.clean("  네   알겠어요  "))
    }
}
