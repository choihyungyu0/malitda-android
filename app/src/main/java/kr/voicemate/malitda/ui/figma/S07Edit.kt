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
import kr.voicemate.malitda.domain.Category

/**
 * S07 · 표현 추가·수정(폼) — 구현정본 node 86:171.
 * 카테고리·제목(40)·내용(80) 입력, TTS 미리듣기, 저장(실제 저장소). 규칙: 공백만이면 저장 안 함.
 */
@Composable
fun S07Edit(
    category: Category,
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onCategoryTap: () -> Unit,
    onListen: () -> Unit,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
) {
    fun fieldStyle(scope: FrameScope) = TextStyle(
        color = MalInk, fontSize = scope.fs(14f), lineHeight = scope.lh(14f),
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Proportional, LineHeightStyle.Trim.None),
    )
    FaithfulFrame {
        // ── 01 backgrounds ──
        val bubble = RoundedCornerShape(d(18f))
        Box(Modifier.box(37f, 210f, 164f, 82f).shadow(d(2f), bubble, clip = false).background(MalSurface, bubble))
        val card = RoundedCornerShape(d(22f))
        Box(Modifier.box(24f, 310f, 342f, 375f).shadow(d(2f), card, clip = false).background(MalSurface, card))
        Box(Modifier.box(24f, 697f, 342f, 54f).background(Color(0xFFF4EEFF), RoundedCornerShape(d(16f))))
        // 탭
        val typeShape = RoundedCornerShape(d(11f))
        Box(Modifier.box(27.89f, 141.28f, 170.08f, 41.61f).background(Color(0xFFFAF5FF), typeShape).border(BorderStroke(d(1f), Color(0xFF8C44FF)), typeShape))
        val voiceShape = RoundedCornerShape(d(20.805f))
        Box(Modifier.box(198.43f, 141.28f, 169.17f, 41.61f).background(MalSurface, voiceShape).border(BorderStroke(d(1f), Color(0xFFE5DEED)), voiceShape))
        // 입력 배경
        val fieldShape = RoundedCornerShape(d(14f))
        Box(Modifier.box(40.69f, 351.59f, 309.53f, 37.49f).background(MalSurface, fieldShape).border(BorderStroke(d(1f), Color(0xFFDED9E5)), fieldShape))
        Box(Modifier.box(40.69f, 433.43f, 309.53f, 37.95f).background(MalSurface, fieldShape).border(BorderStroke(d(1f), Color(0xFFDED9E5)), fieldShape))
        Box(Modifier.box(40.69f, 515.28f, 309.99f, 104.24f).shadow(d(2f), fieldShape, clip = false).background(MalSurface, fieldShape).border(BorderStroke(d(1.2f), Color(0xFF9B65FC)), fieldShape))
        val listenShape = RoundedCornerShape(d(18.06f))
        Box(Modifier.box(40.69f, 633.69f, 122.99f, 36.12f).background(MalSurface, listenShape).border(BorderStroke(d(1f), Color(0xFFE5DEED)), listenShape))
        // 하단 버튼
        val cancelShape = RoundedCornerShape(d(22.405f))
        Box(Modifier.box(25.15f, 767.66f, 90.98f, 44.81f).shadow(d(2f), cancelShape, clip = false).background(MalSurface, cancelShape).border(BorderStroke(d(1f), Color(0xFFE5DEED)), cancelShape))
        Box(Modifier.box(126.65f, 767.66f, 78.18f, 44.81f).background(Color.White, cancelShape).border(BorderStroke(d(1f), Color(0xFFF0E8E8)), cancelShape))
        Box(Modifier.box(214.43f, 767.2f, 151.79f, 45.26f).background(brandBrush(), RoundedCornerShape(d(22.63f))))

        // ── 02 art ──
        artCrop(R.drawable.pose_s07, 208.94f, 192.49f, 149.965f, 117.503f)

        // ── 03 icons ──
        art(R.drawable.brand_logo, 138.08f, 24.23f, 120.703f, 33.969f)
        Image(painterResource(R.drawable.ic_back), null, Modifier.box(21.94f, 19.66f, 35.21f, 35.21f))
        Image(painterResource(R.drawable.ic_info), null, Modifier.box(38f, 712f, 22f, 22f))
        Image(painterResource(R.drawable.ic_down), null, Modifier.box(324.22f, 362.33f, 16f, 16f))

        // ── 04 text ──
        T("표현을 추가·수정해요", 20f, 86f, 350f, 29f, FontWeight.Normal, color = Color(0xFF102342), align = TextAlign.Center, family = JuaFont, lineMul = 1.2f)
        T("자주 쓰는 표현을 등록하면\n더 빠르고 정확하게\n전달할 수 있어요!", 52f, 225f, 145f, 11.8f, FontWeight.Normal, color = MalInk)
        T("카테고리", 41f, 328f, 180f, 16f, FontWeight.Bold, color = MalInk)
        T("표현 제목 (선택)", 41f, 410f, 265f, 16f, FontWeight.Bold, color = MalInk)
        T("표현 내용", 41f, 495f, 214f, 16f, FontWeight.Bold, color = MalInk)
        T("${content.length} / 80", 301f, 497f, 45f, 10f, FontWeight.Normal, color = MalMuted, align = TextAlign.End)
        T("짧고 명확한 표현이 더 잘 전달돼요.\n띄어쓰기와 문장 부호도 자연스럽게 사용해요.", 70f, 709f, 280f, 12f, FontWeight.Normal, color = MalMuted)
        T("직접 입력", 98.76f, 150.59f, 77.726f, 14f, FontWeight.Bold, color = Color(0xFF7546EB))
        T("음성으로 입력", 256.04f, 150.59f, 96.014f, 14f, FontWeight.Bold, color = Color(0xFF7546EB))
        T(category.label, 80.93f, 356.59f, 230f, 14f, FontWeight.Normal, color = Color(0xFF8E929C))
        T("TTS 미리 듣기", 80.93f, 640.25f, 79f, 13f, FontWeight.Bold, color = Color(0xFF7546EB))
        T("취소", 31.15f, 778.57f, 78.98f, 18f, FontWeight.Bold, color = MalInk, align = TextAlign.Center)
        T("삭제", 132.65f, 778.57f, 66.18f, 18f, FontWeight.Bold, color = Color(0xFFFF3B30), align = TextAlign.Center)
        T("저장", 220.43f, 778.33f, 139.79f, 18f, FontWeight.Bold, color = Color.White, align = TextAlign.Center)

        // 제목 입력(40)
        BasicTextField(
            value = title, onValueChange = { if (it.length <= 40) onTitleChange(it.replace("\n", "")) },
            singleLine = true, textStyle = fieldStyle(this), cursorBrush = SolidColor(Color(0xFF7546EB)),
            modifier = Modifier.box(50.69f, 438.43f, 289f, 30f).semantics { contentDescription = "표현 제목" },
        )
        // 내용 입력(80, 필수)
        BasicTextField(
            value = content, onValueChange = { if (it.length <= 80) onContentChange(it) },
            textStyle = fieldStyle(this), cursorBrush = SolidColor(Color(0xFF7546EB)),
            modifier = Modifier.box(50.69f, 525.28f, 289.99f, 90f).padding(top = d(2f)).semantics { contentDescription = "표현 내용" },
        )

        // ── 05 hit areas ──
        Box(Modifier.box(21.49f, 19.66f, 36.12f, 35.21f).clickable(onClickLabel = "이전") { onBack() }.semantics { contentDescription = "이전 화면" })
        Box(Modifier.box(40.69f, 351.59f, 309.53f, 37.49f).clickable(onClickLabel = "카테고리") { onCategoryTap() }.semantics { contentDescription = "카테고리 선택: ${category.label}" })
        Box(Modifier.box(40.69f, 633.69f, 122.99f, 36.12f).clickable(onClickLabel = "미리 듣기") { onListen() }.semantics { contentDescription = "TTS 미리 듣기" })
        Box(Modifier.box(25.15f, 767.66f, 90.98f, 44.81f).clickable(onClickLabel = "취소") { onCancel() }.semantics { contentDescription = "취소" })
        Box(Modifier.box(126.65f, 767.66f, 78.18f, 44.81f).clickable(onClickLabel = "삭제") { onDelete() }.semantics { contentDescription = "삭제" })
        Box(Modifier.box(214.43f, 767.2f, 151.79f, 45.26f).clickable(onClickLabel = "저장") { onSave() }.semantics { contentDescription = "저장" })
    }
}
