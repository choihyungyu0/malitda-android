package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kr.voicemate.malitda.R
import kotlin.math.abs

private val TTS_RATES = listOf(0.6f, 0.8f, 1.0f, 1.25f, 1.5f)
private val TEAL = Color(0xFF00CDBA)

/**
 * S04 · 접근성 설정 — 구현정본 node 86:153. 글자 크기·TTS 속도·진동·시각 강조를 실제 설정에 저장.
 */
@Composable
fun S04Settings(
    fontScale: Float,
    ttsRate: Float,
    haptics: Boolean,
    emphasis: Boolean,
    quick: Boolean,
    onFontScale: (Float) -> Unit,
    onTtsRate: (Float) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onEmphasis: (Boolean) -> Unit,
    onQuick: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDefault: () -> Unit,
) {
    val ttsIdx = TTS_RATES.indices.minByOrNull { abs(TTS_RATES[it] - ttsRate) } ?: 2
    val frac = ((fontScale - 0.9f) / 0.25f).coerceIn(0f, 1f)
    FaithfulFrame {
        // ── 01 backgrounds (카드) ──
        val card = RoundedCornerShape(d(22f))
        listOf(228f to 113f, 347f to 93f, 447f to 60f, 511f to 56f, 568f to 51f).forEach { (t, h) ->
            Box(Modifier.box(21f, t, 348f, h).shadow(d(2f), card, clip = false).background(MalSurface, card))
        }
        Box(Modifier.box(22f, 628f, 346f, 89f).background(Color(0xFFF4EEFF), RoundedCornerShape(d(20f))))
        // 글자크기 아이콘 배경(Aa)
        Box(Modifier.box(35f, 244f, 33f, 33f).background(Color(0xFF9B6AEA), RoundedCornerShape(d(16.5f))))
        // 슬라이더 트랙·진행·손잡이
        Box(Modifier.box(79f, 299f, 233f, 4f).background(Color(0xFFDFDBE6), RoundedCornerShape(d(2f))))
        Box(Modifier.box(79f, 299f, 233f * frac, 4f).background(brandBrush(), RoundedCornerShape(d(2f))))
        Box(Modifier.box(79f + 233f * frac - 7f, 293f, 14f, 14f).background(MalSurface, RoundedCornerShape(d(7f))).border(BorderStroke(d(1f), Color(0xFFBFA5FC)), RoundedCornerShape(d(7f))))
        // TTS 속도 탭 5
        val tabX = listOf(35.66f, 99.67f, 163.68f, 227.69f, 291.7f)
        tabX.forEachIndexed { i, x ->
            val sel = i == ttsIdx; val ts = RoundedCornerShape(d(12.8f))
            Box(Modifier.box(x, 405.55f, 64.01f, 25.6f).background(if (sel) Color(0xFFE8DBFF) else MalSurface, ts).border(BorderStroke(if (sel) d(1.5f) else d(1f), if (sel) Color(0xFF7546EB) else Color(0xFFE5DEED)), ts))
        }
        // 스위치 2
        Switch(this, 460.87f, 22.4f, haptics)
        Switch(this, 518.93f, 23.32f, emphasis)
        // 빠른확인 끄기/켜기
        val qOff = RoundedCornerShape(d(14.4f)); val qOn = RoundedCornerShape(d(14.4f))
        Box(Modifier.box(243.69f, 577.46f, 53.49f, 28.8f).background(if (!quick) Color(0xFFE8DBFF) else MalSurface, qOff).border(BorderStroke(if (!quick) d(1.5f) else d(1f), if (!quick) Color(0xFF7546EB) else Color(0xFFE5DEED)), qOff))
        Box(Modifier.box(297.19f, 577.46f, 54.87f, 28.8f).background(if (quick) Color(0xFFE8DBFF) else MalSurface, qOn).border(BorderStroke(if (quick) d(1.5f) else d(1f), if (quick) Color(0xFF7546EB) else Color(0xFFE5DEED)), qOn))
        // 버튼
        Box(Modifier.box(19.2f, 735.19f, 351.59f, 51.21f).background(Brush.horizontalGradient(listOf(Color(0xFF079EFF), Color(0xFF754BFF), Color(0xFFFF4C9C))), RoundedCornerShape(d(24f))))

        // ── 02 art ──
        artCrop(R.drawable.pose_s04_purple, 215.8f, 160.02f, 154.994f, 74.982f)
        artCrop(R.drawable.pose_s04_preview, 171.45f, 630.95f, 185.17f, 86.87f)

        // ── 03 icons ──
        art(R.drawable.brand_logo, 132.13f, 26.06f, 132.134f, 37.186f)
        artCrop(R.drawable.ic_s04_tts, 34.75f, 356.62f, 35.205f, 35.205f)
        artCrop(R.drawable.ic_s04_vibration, 34.75f, 455.84f, 35.205f, 35.205f)
        artCrop(R.drawable.ic_s04_emphasis, 34.75f, 514.82f, 35.205f, 35.205f)
        artCrop(R.drawable.ic_s04_quick, 34.75f, 572.88f, 35.205f, 35.205f)

        // ── 04 text ──
        T("접근성을\n나에게 맞게 설정해요", 20f, 82f, 350f, 35f, FontWeight.Normal, brush = juaTitleBrush(), align = TextAlign.Center, family = JuaFont, lineMul = 1.1f)
        T("앱을 더 쉽게 사용할 수 있도록\n다양한 기능을 조절할 수 있어요.", 35f, 171f, 190f, 14f, FontWeight.Normal, color = MalMuted)
        T("Aa", 35f, 249f, 33f, 18f, FontWeight.Bold, color = Color.White, align = TextAlign.Center)
        T("글자 크기", 80f, 240f, 234f, 17f, FontWeight.Bold, color = MalInk)
        T("글자 크기를 조절해요.", 80f, 266f, 245f, 11f, FontWeight.Normal, color = MalMuted)
        T("A", 43f, 289f, 28f, 24f, FontWeight.Normal, color = MalMuted)
        T("A", 331f, 285f, 25f, 27f, FontWeight.Normal, color = MalInk)
        T("작게                 보통                 크게", 79f, 316f, 239f, 11f, FontWeight.Normal, color = Color(0xFF7D6A9A))
        T("TTS 속도", 80f, 359f, 234f, 17f, FontWeight.Bold, color = MalInk)
        T("음성으로 읽어주는 속도를 조절해요.", 80f, 385f, 260f, 11f, FontWeight.Normal, color = MalMuted)
        listOf("아주 느리게", "느리게", "보통", "빠르게", "아주 빠르게").forEachIndexed { i, lbl ->
            T(lbl, tabX[i], 406.85f, 64.01f, 9.5f, FontWeight.Bold, color = Color(0xFF7546EB), align = TextAlign.Center)
        }
        T("진동 알림", 80f, 454f, 234f, 16f, FontWeight.Bold, color = MalInk)
        T("중요한 알림을 진동으로 알려줘요.", 80f, 485f, 225f, 10f, FontWeight.Normal, color = MalMuted)
        T("시각 강조", 80f, 517f, 234f, 16f, FontWeight.Bold, color = MalInk)
        T("중요한 요소를 더 뚜렷하게 보여줘요.", 80f, 548f, 226f, 10f, FontWeight.Normal, color = MalMuted)
        T("빠른 확인 모드", 80f, 573f, 154f, 16f, FontWeight.Bold, color = MalInk)
        T("확인은 줄여도 승인은 직접 해요.", 80f, 604f, 230f, 9.5f, FontWeight.Normal, color = MalMuted)
        T("끄기", 249.7f, 580.36f, 41.49f, 11f, FontWeight.Bold, color = Color(0xFF7546EB), align = TextAlign.Center)
        T("켜기", 303.19f, 580.36f, 42.87f, 11f, FontWeight.Bold, color = Color(0xFF7546EB), align = TextAlign.Center)
        T("미리보기", 40f, 644f, 140f, 16f, FontWeight.Bold, color = MalInk)
        T("글자를 이렇게\n보여드려요.", 40f, 665f, 154f, 16f * fontScale, FontWeight.Normal, color = MalMuted)
        T("저장하고 계속", 20f, 745.79f, 350f, 22f, FontWeight.Normal, color = Color.White, align = TextAlign.Center, family = JuaFont)
        T("기본값으로 시작", 118.47f, 794.57f, 157.62f, 11f, FontWeight.Bold, color = Color(0xFF7546EB), align = TextAlign.Center)

        // ── 05 hit areas ──
        Box(
            Modifier.box(75.9f, 285.76f, 237.75f, 41.15f)
                .pointerInput(Unit) { detectTapGestures { off -> onFontScale((0.9f + (off.x / size.width).coerceIn(0f, 1f) * 0.25f)) } }
                .semantics { contentDescription = "글자 크기 조절" },
        )
        tabX.forEachIndexed { i, x ->
            Box(Modifier.box(x, 405.55f, 64.01f, 25.6f).clickable { onTtsRate(TTS_RATES[i]) }.semantics { contentDescription = "TTS 속도 ${i + 1}단계" })
        }
        Box(Modifier.box(314.56f, 460.87f, 42.06f, 22.4f).clickable { onHaptics(!haptics) }.semantics { contentDescription = "진동 알림" })
        Box(Modifier.box(314.56f, 518.93f, 42.06f, 23.32f).clickable { onEmphasis(!emphasis) }.semantics { contentDescription = "시각 강조" })
        Box(Modifier.box(243.69f, 577.46f, 53.49f, 28.8f).clickable { onQuick(false) }.semantics { contentDescription = "빠른 확인 끄기" })
        Box(Modifier.box(297.19f, 577.46f, 54.87f, 28.8f).clickable { onQuick(true) }.semantics { contentDescription = "빠른 확인 켜기" })
        Box(Modifier.box(19.2f, 735.19f, 351.59f, 51.21f).clickable { onSave() }.semantics { contentDescription = "저장하고 계속" })
        Box(Modifier.box(112.47f, 788.69f, 169.62f, 34.75f).clickable { onDefault() }.semantics { contentDescription = "기본값으로 시작" })
    }
}

/** 스위치 시각(off 회색/on 청록, 흰 손잡이). */
@Composable
private fun Switch(scope: FrameScope, top: Float, h: Float, on: Boolean) = with(scope) {
    val r = RoundedCornerShape(d(h / 2f))
    Box(Modifier.box(314.56f, top, 42.06f, h).background(if (on) TEAL else Color(0xFFD7D1DF), r))
    val tsz = h - 4f
    val tx = if (on) 314.56f + 42.06f - tsz - 2f else 314.56f + 2f
    Box(Modifier.box(tx, top + 2f, tsz, tsz).background(MalSurface, CircleShape))
}
