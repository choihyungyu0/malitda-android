package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.voicemate.malitda.R
import kotlin.math.min

/** Jua(둥근 한글) — 등록·인트로 제목/버튼용. 디자이너 지정 폰트. */
val JuaFont = FontFamily(Font(R.font.jua))

/**
 * 구현정본(Figma dev-master) 프레임 좌표계.
 * 디자인 원안은 390 × 843.095 기준. 화면에 **비율 유지·가운데 정렬**(min 스케일)로 맞춘 뒤,
 * 자식들은 디자인 원좌표(px)를 그대로 적어 배치한다(내부에서 k배 스케일).
 * 이렇게 하면 코드에 Figma 좌표를 1:1로 옮길 수 있어 픽셀 충실도가 보장된다.
 */
const val FF_W = 390f
const val FF_H = 843.09496f

// 공통 색 토큰(디자인 토큰)
val MalCanvas = Color(0xFFFFFDFA)
val MalSurface = Color(0xFFFFFFFF)
val MalMuted = Color(0xFF667085)
val MalInk = Color(0xFF152346)
val BrandA = Color(0xFF4B65FF)
val BrandB = Color(0xFFB44DFF)

/** 브랜드 그라디언트(좌→우). */
fun brandBrush() = Brush.horizontalGradient(listOf(BrandA, BrandB))

/** Jua 제목 3-stop 그라디언트(등록·인트로). */
fun juaTitleBrush() = Brush.horizontalGradient(listOf(Color(0xFF1836FF), Color(0xFF6934F6), Color(0xFFC036D9)))

/** 상대 시각 표시(오늘=시각, 어제, N일 전, 그 외 날짜). */
fun relTime(ts: Long): String {
    val days = ((System.currentTimeMillis() - ts) / 86_400_000L).toInt()
    return when {
        days <= 0 -> {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = ts }
            val h = cal.get(java.util.Calendar.HOUR_OF_DAY); val m = cal.get(java.util.Calendar.MINUTE)
            val ap = if (h < 12) "오전" else "오후"; val h12 = if (h % 12 == 0) 12 else h % 12
            "%s %d:%02d".format(ap, h12, m)
        }
        days == 1 -> "어제"
        days < 30 -> "${days}일 전"
        else -> "%tY.%<tm.%<td".format(ts)
    }
}

/** 프레임 좌표 헬퍼. content 안에서 Modifier.box(...)·d(..)·fs(..)로 원좌표를 그대로 쓴다. */
class FrameScope(val k: Float) {
    /** 디자인 px → dp */
    fun d(v: Float) = (v * k).dp
    /** 디자인 pt(sp) → 스케일된 sp */
    fun fs(v: Float): TextUnit = (v * k).sp
    /** line-height 1.35 기준 */
    fun lh(sizePt: Float): TextUnit = (sizePt * 1.35f * k).sp

    /** (x,y) 위치에 (w,h) 크기로 절대 배치 */
    fun Modifier.box(x: Float, y: Float, w: Float, h: Float): Modifier =
        this.offset(d(x), d(y)).requiredSize(d(w), d(h))

    /** (x,y) 위치에 폭 w만 고정(높이는 내용에 맞춤) — 텍스트용 */
    fun Modifier.textBox(x: Float, y: Float, w: Float): Modifier =
        this.offset(d(x), d(y)).requiredWidth(d(w))
}

@Composable
fun FaithfulFrame(
    modifier: Modifier = Modifier,
    canvas: Color = MalCanvas,
    content: @Composable FrameScope.() -> Unit,
) {
    Surface(color = canvas, modifier = modifier.fillMaxSize()) {
        // 캔버스(배경색)는 화면 끝까지 채우되, 프레임(디자인 좌표계)은 시스템 바(상태·내비게이션)를
        // 제외한 안전영역에 맞춘다 → 짧은 화면(16:9)에서도 하단 버튼이 내비게이션 바에 가리지 않음.
        BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)) {
            val density = LocalDensity.current
            val availW: Float; val availH: Float
            with(density) { availW = maxWidth.toPx(); availH = maxHeight.toPx() }
            val scale = min(availW / FF_W, availH / FF_H)
            val wDp: Float; val hDp: Float
            with(density) { wDp = (FF_W * scale).toDp().value; hDp = (FF_H * scale).toDp().value }
            val k = wDp / FF_W
            Box(
                Modifier
                    .align(Alignment.Center)
                    .requiredSize(wDp.dp, hDp.dp),
            ) {
                FrameScope(k).content()
            }
        }
    }
}

/**
 * 절대좌표 텍스트(정본 좌표 그대로). 모든 화면 공용.
 * brush를 주면 그라디언트 텍스트, 아니면 color 단색.
 */
@Composable
fun FrameScope.T(
    text: String,
    x: Float,
    y: Float,
    w: Float,
    sizePt: Float,
    weight: FontWeight,
    color: Color = Color.Unspecified,
    brush: Brush? = null,
    align: TextAlign = TextAlign.Start,
    family: FontFamily? = null,
    lineMul: Float = 1.35f,
) {
    var style = TextStyle(
        color = color,
        fontSize = fs(sizePt),
        lineHeight = (sizePt * lineMul * k).sp,
        fontWeight = weight,
        fontFamily = family,
        textAlign = align,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Proportional,
            trim = LineHeightStyle.Trim.None,
        ),
    )
    if (brush != null) style = style.copy(brush = brush)
    Text(text = text, modifier = Modifier.textBox(x, y, w), style = style)
}

/** 아트/이미지 PNG 절대배치 헬퍼(정본 좌표). 모든 화면 공용. */
@Composable
fun FrameScope.art(res: Int, x: Float, y: Float, w: Float, h: Float = w) {
    Image(
        painter = painterResource(res),
        contentDescription = null,
        modifier = Modifier.box(x, y, w, h),
        contentScale = ContentScale.Fit,
    )
}

/** object-cover(꽉 채우고 넘침 크롭) 포즈/이미지 배치. */
@Composable
fun FrameScope.artCrop(res: Int, x: Float, y: Float, w: Float, h: Float) {
    Image(
        painter = painterResource(res),
        contentDescription = null,
        modifier = Modifier.box(x, y, w, h),
        contentScale = ContentScale.Crop,
    )
}
