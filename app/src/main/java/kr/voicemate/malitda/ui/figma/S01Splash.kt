package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kr.voicemate.malitda.R

/**
 * S01 · 시작(스플래시) — 디자이너 공식 원안 풀-블리드 히어로.
 * 배경·캐릭터 7종·브랜드 로고·슬로건("당신의 말을, 그대로 잇다" / "내 말을 더 정확하게 전달해요")이
 * 일체화된 공식 원안 이미지(변경 금지). 이미지 비율 853:1844 = 390:843.095로 프레임과 정확히 일치.
 * 화면 아무 곳이나 탭하면 시작.
 */
@Composable
fun S01Splash(onStart: () -> Unit) {
    FaithfulFrame(canvas = Color(0xFF8E88FB)) {
        // 공식 원안 히어로(로고·캐릭터·슬로건 일체형)
        artCrop(R.drawable.bg_s01_hero, 0f, 0f, 390f, 843.095f)

        // 전체 화면 탭 = 시작
        Box(
            Modifier
                .box(0f, 0f, 390f, 843.095f)
                .clickable(onClickLabel = "시작") { onStart() }
                .semantics { contentDescription = "말잇다 시작하기" },
        )
    }
}
