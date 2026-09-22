package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import kr.voicemate.malitda.R

/**
 * S11 · 문장 확인·수정 — 구현정본 node 86:195.
 * 선택 문장(읽기 전용)과 직접 수정 입력(draft, 200자). 들어보기=TTS, 다시 말하기=재녹음.
 */
@Composable
fun S11Confirm(
    selectedText: String,
    draft: String,
    onDraftChange: (String) -> Unit,
    onListen: () -> Unit,
    onRerecord: () -> Unit,
    onNext: () -> Unit,
) {
    FaithfulFrame {
        // ── 01 backgrounds ──
        val card = RoundedCornerShape(d(22f))
        Box(Modifier.box(24f, 251f, 342f, 184f).shadow(d(2f), card, clip = false).background(MalSurface, card))
        Box(Modifier.box(24f, 474f, 342f, 179f).shadow(d(2f), card, clip = false).background(MalSurface, card))
        Box(Modifier.box(23f, 666f, 343f, 55f).shadow(d(2f), card, clip = false).background(MalSurface, card))
        val listenShape = RoundedCornerShape(d(22.86f))
        Box(Modifier.box(43.89f, 366.68f, 302.67f, 45.72f).shadow(d(2f), listenShape, clip = false).background(MalSurface, listenShape).border(BorderStroke(d(1f), Color(0xFFE5DEED)), listenShape))
        val inputShape = RoundedCornerShape(d(14f))
        Box(Modifier.box(33.83f, 516.19f, 323.7f, 125.28f).shadow(d(2f), inputShape, clip = false).background(MalSurface, inputShape).border(BorderStroke(d(1.4f), Color(0xFF9B65FC)), inputShape))
        Box(Modifier.box(24.69f, 738.85f, 339.25f, 50.75f).background(brandBrush(), RoundedCornerShape(d(24f))))

        // ── 02 art ──
        art(R.drawable.art_purple, 263f, 438f, 106f)

        // ── 03 icons ──
        art(R.drawable.brand_logo, 126f, 24f, 152f, 43f)
        Image(painterResource(R.drawable.ic_listen), null, Modifier.box(154f, 380f, 22f, 22f))
        Image(painterResource(R.drawable.ic_refresh), null, Modifier.box(43f, 682f, 26f, 26f))
        Image(painterResource(R.drawable.ic_listen_big), null, Modifier.box(293.07f, 298.1f, 53.49f, 53.49f))

        // ── 04 text ──
        T("문장을 확인하고\n수정해요", 24f, 86f, 342f, 30f, FontWeight.Black, brush = brandBrush(), align = TextAlign.Center)
        T("내가 말한 문장을 더 자연스럽게 다듬어봐요.", 28f, 175f, 334f, 13f, FontWeight.Normal, color = MalMuted, align = TextAlign.Center)
        T("선택한 문장", 46f, 276f, 262f, 15f, FontWeight.Bold, color = Color(0xFF8B58F1))
        T(selectedText, 46f, 307f, 246f, 20f, FontWeight.Bold, color = MalInk)
        T("문장을 직접 수정해 보세요", 40f, 489f, 291f, 14f, FontWeight.Bold, color = Color(0xFF5345EF))
        T("${draft.length} / 200", 300f, 620f, 44f, 10f, FontWeight.Normal, color = Color(0xFF8A8395), align = TextAlign.End)
        T("다시 말하기", 83f, 677f, 210f, 17f, FontWeight.Bold, color = Color(0xFF6C51ED))
        T("새로운 문장을 만들 수 있어요.", 83f, 702f, 236f, 10f, FontWeight.Normal, color = Color(0xFF8B8999))
        T("들어보기", 183f, 378f, 110f, 17f, FontWeight.Bold, color = Color(0xFF7546EB))
        T("이 문장으로 진행", 30.7f, 752.72f, 327.25f, 18f, FontWeight.Bold, color = Color.White, align = TextAlign.Center)

        // 편집 입력(draft, 200자)
        BasicTextField(
            value = draft,
            onValueChange = { if (it.length <= 200) onDraftChange(it) },
            textStyle = TextStyle(
                color = MalInk, fontSize = fs(14f), lineHeight = lh(14f),
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Proportional, LineHeightStyle.Trim.None),
            ),
            cursorBrush = SolidColor(Color(0xFF7546EB)),
            modifier = Modifier.box(33.83f, 516.19f, 323.7f, 125.28f).padding(horizontal = d(10f), vertical = d(10f))
                .semantics { contentDescription = "보낼 문장 직접 수정" },
        )

        // ── 05 hit areas ──
        Box(Modifier.box(43.89f, 366.68f, 302.67f, 45.72f).clickable(onClickLabel = "들어보기") { onListen() }.semantics { contentDescription = "선택한 문장 듣기" })
        Box(Modifier.box(293.07f, 298.1f, 53.49f, 55.32f).clickable(onClickLabel = "듣기") { onListen() }.semantics { contentDescription = "선택 문장 음성" })
        Box(Modifier.box(23.32f, 665.24f, 342.91f, 55.32f).clickable(onClickLabel = "다시 말하기") { onRerecord() }.semantics { contentDescription = "다시 말하기" })
        Box(Modifier.box(24.69f, 738.85f, 339.25f, 50.75f).clickable(onClickLabel = "진행") { onNext() }.semantics { contentDescription = "이 문장으로 진행" })
    }
}
