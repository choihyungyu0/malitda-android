package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign

private val DIM = Color(0x59080512)
private val MODAL_ACCENT = Color(0xFF7142E8)

/**
 * 모달 오버레이 프레임: 딤 배경(밖 탭=닫기) + 390×843 좌표계.
 * 화면 위에 겹쳐 그린다(내비 라우트가 아닌 dialog 상태로).
 */
@Composable
fun ModalFrame(onDismiss: () -> Unit, content: @Composable FrameScope.() -> Unit) {
    Box(
        Modifier.fillMaxSize().background(DIM).pointerInput(Unit) { detectTapGestures { onDismiss() } },
    ) {
        FaithfulFrame(canvas = Color.Transparent) { content() }
    }
}

/** 옵션 리스트 모달(O_MENU·O_CATEGORY 등): 제목+부제+최대5버튼(첫 버튼 강조). */
@Composable
fun OptionListModal(
    title: String,
    subtitle: String,
    options: List<Pair<String, () -> Unit>>,
    onDismiss: () -> Unit,
) {
    ModalFrame(onDismiss) {
        val surf = RoundedCornerShape(d(24f))
        Box(
            Modifier.box(16f, 195.55f, 358f, 452f).shadow(d(6f), surf, clip = false)
                .background(MalCanvas, surf).pointerInput(Unit) { detectTapGestures {} },
        )
        T(title, 40f, 219.55f, 310f, 22f, FontWeight.Bold, color = MalInk)
        T(subtitle, 40f, 275.55f, 310f, 14f, FontWeight.Normal, color = MalMuted)
        val tops = listOf(325.55f, 387.55f, 449.55f, 511.55f, 573.55f)
        options.take(5).forEachIndexed { i, (label, action) ->
            val top = tops[i]; val first = i == 0
            val bs = RoundedCornerShape(d(24f))
            if (first) {
                Box(Modifier.box(40f, top, 310f, 48f).background(brandBrush(), bs))
            } else {
                Box(Modifier.box(40f, top, 310f, 48f).shadow(d(2f), bs, clip = false).background(MalSurface, bs).border(BorderStroke(d(1f), Color(0xFFE5DEEC)), bs))
            }
            T(label, 50f, top + 12f, 290f, 16f, FontWeight.Bold, color = if (first) Color.White else MODAL_ACCENT, align = TextAlign.Center)
            Box(Modifier.box(40f, top, 310f, 48f).clickable(onClickLabel = label) { action() }.semantics { contentDescription = label })
        }
    }
}

/** 안내/확인 모달(O_INPUT_ERROR·O_COPIED·O_SHARE·O_CONTACT·O_PRIVACY): 제목+본문+1~2버튼. */
@Composable
fun InfoModal(
    surfTop: Float,
    surfH: Float,
    title: String,
    body: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    onDismiss: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: () -> Unit = {},
) {
    ModalFrame(onDismiss) {
        val surf = RoundedCornerShape(d(24f))
        val bottom = surfTop + surfH
        Box(Modifier.box(16f, surfTop, 358f, surfH).shadow(d(6f), surf, clip = false).background(MalCanvas, surf).pointerInput(Unit) { detectTapGestures {} })
        T(title, 40f, surfTop + 24f, 310f, 22f, FontWeight.Bold, color = MalInk)
        T(body, 40f, surfTop + 80f, 310f, 14f, FontWeight.Normal, color = MalMuted)
        val pTop = if (secondaryLabel != null) bottom - 136f else bottom - 74f
        val bs = RoundedCornerShape(d(24f))
        Box(Modifier.box(40f, pTop, 310f, 48f).background(brandBrush(), bs))
        T(primaryLabel, 50f, pTop + 12f, 290f, 16f, FontWeight.Bold, color = Color.White, align = TextAlign.Center)
        Box(Modifier.box(40f, pTop, 310f, 48f).clickable(onClickLabel = primaryLabel) { onPrimary() }.semantics { contentDescription = primaryLabel })
        if (secondaryLabel != null) {
            val sTop = bottom - 74f
            Box(Modifier.box(40f, sTop, 310f, 48f).background(MalSurface, bs).border(BorderStroke(d(1f), Color(0xFFE5DEEC)), bs))
            T(secondaryLabel, 50f, sTop + 12f, 290f, 16f, FontWeight.Bold, color = MODAL_ACCENT, align = TextAlign.Center)
            Box(Modifier.box(40f, sTop, 310f, 48f).clickable(onClickLabel = secondaryLabel) { onSecondary() }.semantics { contentDescription = secondaryLabel })
        }
    }
}

/** 입력 모달(O_SEARCH_*·O_EDIT_*): 제목+부제+입력칸+검색/닫기. */
@Composable
fun InputModal(
    surfTop: Float,
    surfH: Float,
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalFrame(onDismiss) {
        val surf = RoundedCornerShape(d(24f))
        val bottom = surfTop + surfH
        Box(Modifier.box(16f, surfTop, 358f, surfH).shadow(d(6f), surf, clip = false).background(MalCanvas, surf).pointerInput(Unit) { detectTapGestures {} })
        T(title, 40f, surfTop + 24f, 310f, 22f, FontWeight.Bold, color = MalInk)
        T(subtitle, 40f, surfTop + 80f, 310f, 14f, FontWeight.Normal, color = MalMuted)
        val fieldShape = RoundedCornerShape(d(12f))
        Box(Modifier.box(40f, surfTop + 126f, 310f, 88f).background(MalSurface, fieldShape).border(BorderStroke(d(1f), Color(0xFF8C4DF2)), fieldShape))
        BasicTextField(
            value = value, onValueChange = { if (it.length <= 40) onValueChange(it.replace("\n", "")) },
            singleLine = true,
            textStyle = TextStyle(
                color = Color(0xFF142445), fontSize = fs(14f), lineHeight = lh(14f),
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Proportional, LineHeightStyle.Trim.None),
            ),
            cursorBrush = SolidColor(Color(0xFF7546EB)),
            modifier = Modifier.box(52f, surfTop + 140f, 286f, 30f).padding(top = d(2f)).semantics { contentDescription = "$title 입력" },
        )
        val bs = RoundedCornerShape(d(24f))
        val pTop = bottom - 136f; val sTop = bottom - 74f
        Box(Modifier.box(40f, pTop, 310f, 48f).background(brandBrush(), bs))
        T(primaryLabel, 50f, pTop + 12f, 290f, 16f, FontWeight.Bold, color = Color.White, align = TextAlign.Center)
        Box(Modifier.box(40f, pTop, 310f, 48f).clickable(onClickLabel = primaryLabel) { onPrimary() }.semantics { contentDescription = primaryLabel })
        Box(Modifier.box(40f, sTop, 310f, 48f).background(MalSurface, bs).border(BorderStroke(d(1f), Color(0xFFE5DEEC)), bs))
        T(secondaryLabel, 50f, sTop + 12f, 290f, 16f, FontWeight.Bold, color = MODAL_ACCENT, align = TextAlign.Center)
        Box(Modifier.box(40f, sTop, 310f, 48f).clickable(onClickLabel = secondaryLabel) { onSecondary() }.semantics { contentDescription = secondaryLabel })
    }
}
