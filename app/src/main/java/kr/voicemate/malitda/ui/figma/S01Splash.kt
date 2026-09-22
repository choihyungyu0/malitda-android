package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kr.voicemate.malitda.R

/**
 * S01 · 시작(스플래시) — 구현정본 node 86:135.
 * 화면 아무 곳이나 탭하면 시작. 캐릭터 7종 + 브랜드 로고 + 슬로건.
 */
@Composable
fun S01Splash(onStart: () -> Unit) {
    FaithfulFrame(canvas = Color(0xFFE4E7FB)) {
        // 배경 그라디언트(위 하늘색 → 아래 연보라)
        Box(
            Modifier
                .box(0f, 0f, 390f, 844f)
                .background(Brush.verticalGradient(listOf(Color(0xFFDDF6FF), Color(0xFFECD8FD)))),
        )
        // 캐릭터 아트 7종(디자이너 실제 에셋)
        art(R.drawable.art_cloud, 220f, 250f, 146f)
        art(R.drawable.art_heart, 10f, 270f, 150f)
        art(R.drawable.art_water, 25f, 410f, 130f)
        art(R.drawable.art_green, 195f, 385f, 140f)
        art(R.drawable.art_purple, 122f, 510f, 132f)
        art(R.drawable.art_robot_mint, 237f, 552f, 134f)
        art(R.drawable.art_yellow, 24f, 600f, 124f)

        // 브랜드 공식 원본 로고(변경 금지)
        art(R.drawable.brand_logo, 40f, 188f, 300f, 84f)

        // 슬로건(로고에 이미 '당신의 말을, 그대로 잇다' 포함되어 부제만 표시)
        T("내 말을 더 정확하게 전달해요", 20f, 764f, 350f, 15f, FontWeight.Normal, color = Color(0xFF5143C8), align = TextAlign.Center)

        // 전체 화면 탭 = 시작
        Box(
            Modifier
                .box(0f, 0f, 390f, 844f)
                .clickable(onClickLabel = "시작") { onStart() }
                .semantics { contentDescription = "말잇다 시작하기" },
        )
    }
}
