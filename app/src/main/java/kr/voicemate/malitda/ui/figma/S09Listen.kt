package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
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

/**
 * S09 · 말하기(녹음) — 구현정본 node 86:183.
 * 녹음/인식 중 상태 표시. 중지→인식, 취소→홈.
 */
@Composable
fun S09Listen(status: String, onStop: () -> Unit, onCancel: () -> Unit) {
    FaithfulFrame {
        // 동심원
        Box(Modifier.box(79f, 221f, 232f, 232f).background(Color(0xFFF8F4FF), CircleShape))
        Box(Modifier.box(91.5f, 233f, 207f, 207f).background(Color(0xFFF0E8FF), CircleShape))
        Box(Modifier.box(104.5f, 246f, 181f, 181f).background(Color(0xFFEADDFD), CircleShape))
        Box(Modifier.box(112.5f, 253f, 165f, 165f).shadow(d(2f), CircleShape, clip = false).background(MalSurface, CircleShape))
        // 버튼 배경
        Box(Modifier.box(47.09f, 662.95f, 295.36f, 68.58f).background(brandBrush(), RoundedCornerShape(d(24f))))
        val cancelShape = RoundedCornerShape(d(23.545f))
        Box(Modifier.box(148.59f, 747.54f, 92.36f, 47.09f).shadow(d(2f), cancelShape, clip = false).background(MalSurface, cancelShape).border(BorderStroke(d(1f), Color(0xFFE5DEED)), cancelShape))

        // 아트 + 아이콘
        art(R.drawable.art_purple, 224f, 421f, 140f)
        art(R.drawable.brand_logo, 126f, 24f, 152f, 43f)
        Image(painterResource(R.drawable.ic_mic_s09), null, Modifier.box(158f, 301f, 77f, 77f))

        // 텍스트
        T("말씀해 주세요", 24f, 86f, 342f, 30f, FontWeight.Black, brush = brandBrush(), align = TextAlign.Center)
        T(status, 81f, 514f, 182f, 25f, FontWeight.Bold, color = Color(0xFF8245E7), align = TextAlign.Center)
        T("●   ●   ●", 133f, 552f, 118f, 16f, FontWeight.Normal, color = Color(0xFFAE8BEF))
        T("또렷하게 천천히 말씀해 주세요", 25f, 589f, 340f, 16f, FontWeight.Normal, color = MalMuted, align = TextAlign.Center)
        T("중지", 53f, 685.74f, 283.36f, 18f, FontWeight.Bold, color = Color.White, align = TextAlign.Center)
        T("취소", 154.6f, 759.59f, 80.36f, 18f, FontWeight.Bold, color = Color(0xFF7546EB), align = TextAlign.Center)

        // 터치영역
        Box(Modifier.box(47.09f, 662.95f, 295.36f, 68.58f).clickable(onClickLabel = "중지") { onStop() }.semantics { contentDescription = "녹음 중지" })
        Box(Modifier.box(148.59f, 747.54f, 92.36f, 47.09f).clickable(onClickLabel = "취소") { onCancel() }.semantics { contentDescription = "녹음 취소" })
    }
}
