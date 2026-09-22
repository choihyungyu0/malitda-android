package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kr.voicemate.malitda.R

private val ACCENT = Color(0xFF7142E8)

/**
 * 범용 안내/확인 화면(S22~S27 공통 골격). 안내 카드 + 제목 + 본문 + 최대 2버튼.
 * art(선택)와 각 텍스트 좌표는 구현정본 S23 기준 골격을 따른다.
 */
@Composable
fun InfoScreen(
    title: String,
    body: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: () -> Unit = {},
    titleColor: Color = ACCENT,
    art: Int? = null,
    artBox: FloatArray? = null,
) {
    FaithfulFrame {
        Box(Modifier.box(24f, 200f, 342f, 390f).background(Color(0xFFF4EEFF), RoundedCornerShape(d(22f))))
        Box(Modifier.box(24f, 650f, 342f, 54f).background(brandBrush(), RoundedCornerShape(d(24f))))
        val sec = RoundedCornerShape(d(24f))
        if (secondaryLabel != null) {
            Box(Modifier.box(24f, 720f, 342f, 54f).shadow(d(2f), sec, clip = false).background(MalSurface, sec).border(BorderStroke(d(1f), Color(0xFFE5DEED)), sec))
        }

        if (art != null && artBox != null) artCrop(art, artBox[0], artBox[1], artBox[2], artBox[3])
        art(R.drawable.brand_logo, 123.45f, 22.86f, 141.735f, 39.888f)

        T(title, 24f, 95f, 342f, 27f, FontWeight.Bold, color = titleColor)
        T(body, 42f, 220f, 306f, 16f, FontWeight.Normal, color = MalInk)
        T(primaryLabel, 24f, 665f, 342f, 17f, FontWeight.Bold, color = Color.White, align = TextAlign.Center)
        if (secondaryLabel != null) T(secondaryLabel, 24f, 735f, 342f, 17f, FontWeight.Bold, color = ACCENT, align = TextAlign.Center)

        Box(Modifier.box(24f, 650f, 342f, 54f).clickable(onClickLabel = primaryLabel) { onPrimary() }.semantics { contentDescription = primaryLabel })
        if (secondaryLabel != null) {
            Box(Modifier.box(24f, 720f, 342f, 54f).clickable(onClickLabel = secondaryLabel) { onSecondary() }.semantics { contentDescription = secondaryLabel })
        }
    }
}
