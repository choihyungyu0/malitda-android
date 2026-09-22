package kr.voicemate.malitda.ui.figma

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import kr.voicemate.malitda.R

// 구현정본 S03 색 토큰
private val CANVAS = Color(0xFFFFFDFA)
private val SURFACE = Color(0xFFFFFFFF)
private val MUTED = Color(0xFF667085)
private val INK = Color(0xFF152346)
private val CB_BORDER = Color(0xFFA9A7B5)
private val LATER_BORDER = Color(0xFFE5DEED)
private val C_PURPLE = Color(0xFF9660F7)
private val C_TEAL = Color(0xFF00BDB0)
private val C_BLUE = Color(0xFF2784FF)
private val C_LINK = Color(0xFF3683FF)
private val C_LATER = Color(0xFF7546EB)
private val BRAND_A = Color(0xFF4B65FF)
private val BRAND_B = Color(0xFFB44DFF)

// 텍스트 헬퍼 T()는 FaithfulFrame.kt의 공용본 사용.

/** 체크박스 시각(선택 시 브랜드 그라디언트 + 흰 체크). 크기는 box()로 지정된다. */
@Composable
private fun FrameScope.CheckboxVisual(checked: Boolean, brand: Brush, modifier: Modifier) {
    val shape = RoundedCornerShape(d(5f))
    Box(
        modifier
            .clip(shape)
            .then(
                if (checked) Modifier.background(brand)
                else Modifier.background(SURFACE).border(BorderStroke(d(1.2f), CB_BORDER), shape),
            ),
    ) {
        if (checked) {
            Canvas(Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val sw = h * 0.12f
                drawLine(Color.White, Offset(w * 0.26f, h * 0.52f), Offset(w * 0.43f, h * 0.68f), sw, StrokeCap.Round)
                drawLine(Color.White, Offset(w * 0.43f, h * 0.68f), Offset(w * 0.74f, h * 0.33f), sw, StrokeCap.Round)
            }
        }
    }
}

/**
 * S03 · 안내·동의 — 구현정본(node 86:147)을 네이티브로 무손실 재현.
 * 배경/카드/버튼은 디자이너 스펙대로 그린 네이티브 도형, 아트·아이콘은 디자이너 실제 에셋,
 * 텍스트는 실제 TEXT, 체크박스·버튼은 실제 작동(동의 게이트).
 */
@Composable
fun S03Consent(
    checkedNotice: Boolean,
    checkedPrivacy: Boolean,
    onToggleNotice: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onContinue: () -> Unit,
    onLater: () -> Unit,
    onViewPolicy: () -> Unit,
) {
    val brand = Brush.horizontalGradient(listOf(BRAND_A, BRAND_B))
    val both = checkedNotice && checkedPrivacy

    FaithfulFrame(canvas = CANVAS) {
        // ── 01 backgrounds ────────────────────────────────
        val cardShape = RoundedCornerShape(d(22f))
        listOf(214f, 343f, 472f).forEach { top ->
            Box(
                Modifier
                    .box(24f, top, 342f, 120f)
                    .shadow(d(2f), cardShape, clip = false)
                    .background(SURFACE, cardShape),
            )
        }
        // 동의하고 시작 버튼 배경(그라디언트)
        Box(
            Modifier
                .box(32.46f, 701.36f, 325.08f, 51.21f)
                .alpha(if (both) 1f else 0.5f)
                .background(brand, RoundedCornerShape(d(24f))),
        )
        // 나중에 할게요 버튼 배경(흰색 테두리)
        val laterShape = RoundedCornerShape(d(22.405f))
        Box(
            Modifier
                .box(132.13f, 760.34f, 145.85f, 44.81f)
                .shadow(d(2f), laterShape, clip = false)
                .background(SURFACE, laterShape)
                .border(BorderStroke(d(1f), LATER_BORDER), laterShape),
        )
        // 체크박스 시각(선택 상태 반영)
        CheckboxVisual(checkedNotice, brand, Modifier.box(46.64f, 614.95f, 20.12f, 20.57f))
        CheckboxVisual(checkedPrivacy, brand, Modifier.box(46.64f, 662.04f, 20.12f, 20.57f))

        // ── 02 art (디자이너 실제 에셋) ─────────────────────
        Image(
            painter = painterResource(R.drawable.art_yellow),
            contentDescription = null,
            modifier = Modifier.box(281f, 154f, 90f, 90f),
            contentScale = ContentScale.Fit,
        )
        Image(
            painter = painterResource(R.drawable.art_robot_pink),
            contentDescription = null,
            modifier = Modifier.box(0f, 750f, 94f, 94f),
            contentScale = ContentScale.Fit,
        )

        // ── 03 icons (디자이너 실제 벡터) ───────────────────
        art(R.drawable.brand_logo, 126f, 24f, 152f, 43f)
        Image(painterResource(R.drawable.ic_consent_mic), null, Modifier.box(42f, 240f, 52f, 52f))
        Image(painterResource(R.drawable.ic_consent_shield), null, Modifier.box(42f, 369f, 52f, 52f))
        Image(painterResource(R.drawable.ic_consent_globe), null, Modifier.box(42f, 498f, 52f, 52f))

        // ── 04 text ────────────────────────────────────────
        T("안내를 확인하고\n동의해 주세요", 24f, 86f, 342f, 30f, FontWeight.Black, brush = brand, align = TextAlign.Center)

        T("원음성은 저장하지 않아요", 135f, 232f, 214f, 16f, FontWeight.Bold, color = C_PURPLE)
        T("인식을 위한 음성은 기기 안에서 처리하며\n원음성 파일은 별도 보관하지 않아요.", 135f, 284f, 215f, 11f, FontWeight.Normal, color = MUTED)

        T("표현·교정 이력은\n기기 안에 저장돼요", 135f, 361f, 214f, 16f, FontWeight.Bold, color = C_TEAL)
        T("내가 등록한 표현과 승인 교정은\n기기 안에서 관리할 수 있어요.", 135f, 413f, 215f, 11f, FontWeight.Normal, color = MUTED)

        T("음성은 이 기기에서\n인식해요", 135f, 490f, 214f, 16f, FontWeight.Bold, color = C_BLUE)
        T("외부 음성인식으로 자동 전환하지 않아요.\n직접 입력도 할 수 있어요.", 135f, 542f, 215f, 11f, FontWeight.Normal, color = MUTED)

        T("위 내용을 확인했어요", 78f, 619f, 274f, 14f, FontWeight.Normal, color = INK)
        T("개인정보 처리방침에 동의해요", 78f, 666f, 276f, 13f, FontWeight.Normal, color = C_LINK)

        T("동의하고 시작", 38.46f, 715.47f, 313.08f, 18f, FontWeight.Bold, color = Color.White.copy(alpha = if (both) 1f else 0.9f), align = TextAlign.Center)
        T("나중에 할게요", 138.135f, 771.24f, 133.85f, 18f, FontWeight.Bold, color = C_LATER, align = TextAlign.Center)

        // ── 05 hit areas (투명 터치, 접근성 라벨) ────────────
        Box(
            Modifier
                .box(30f, 604f, 46f, 42f)
                .toggleable(value = checkedNotice, role = Role.Checkbox) { onToggleNotice() }
                .semantics { contentDescription = "위 내용을 확인했어요" },
        )
        // 라벨도 탭하면 토글
        Box(
            Modifier
                .box(76f, 612f, 200f, 30f)
                .toggleable(value = checkedNotice, role = Role.Checkbox) { onToggleNotice() }
                .semantics { contentDescription = "위 내용을 확인했어요" },
        )
        Box(
            Modifier
                .box(30f, 652f, 46f, 42f)
                .toggleable(value = checkedPrivacy, role = Role.Checkbox) { onTogglePrivacy() }
                .semantics { contentDescription = "개인정보 처리방침에 동의해요" },
        )
        // 처리방침 텍스트: 보기(링크)
        Box(
            Modifier
                .box(76f, 656f, 200f, 30f)
                .clickable(onClickLabel = "개인정보 처리방침 보기") { onViewPolicy() }
                .semantics { contentDescription = "개인정보 처리방침 보기" },
        )
        Box(
            Modifier
                .box(32.46f, 701.36f, 325.08f, 51.21f)
                .clickable(enabled = true, onClickLabel = "동의하고 시작") { onContinue() }
                .semantics { contentDescription = "동의하고 시작" },
        )
        Box(
            Modifier
                .box(132.13f, 760.34f, 145.85f, 44.81f)
                .clickable(onClickLabel = "나중에 할게요") { onLater() }
                .semantics { contentDescription = "나중에 할게요" },
        )
    }
}

/** 데모 호스트: 상태 보유 + 토스트로 동작 확인. MainActivity에서 임시로 표시. */
@Composable
fun S03ConsentDemo() {
    var notice by remember { mutableStateOf(false) }
    var privacy by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    S03Consent(
        checkedNotice = notice,
        checkedPrivacy = privacy,
        onToggleNotice = { notice = !notice },
        onTogglePrivacy = { privacy = !privacy },
        onContinue = {
            if (notice && privacy) {
                Toast.makeText(ctx, "동의 완료 · 다음 단계로 이동", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(ctx, "두 항목에 모두 동의해야 시작할 수 있어요", Toast.LENGTH_SHORT).show()
            }
        },
        onLater = { Toast.makeText(ctx, "나중에 할게요", Toast.LENGTH_SHORT).show() },
        onViewPolicy = { Toast.makeText(ctx, "개인정보 처리방침 보기", Toast.LENGTH_SHORT).show() },
    )
}
