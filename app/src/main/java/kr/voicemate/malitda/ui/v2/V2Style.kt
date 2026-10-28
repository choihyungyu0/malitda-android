package kr.voicemate.malitda.ui.v2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.voicemate.malitda.R
import kr.voicemate.malitda.BuildConfig
import kr.voicemate.malitda.ui.theme.LocalA11y

/**
 * v2 공통 스타일(디자인 42화면 기준).
 * 서체는 Jua Regular 하나뿐이다 — 가짜 볼드(가상 굵기)를 쓰지 않는다(전달 자료 지침).
 */
val JuaV2 = FontFamily(Font(R.font.jua, FontWeight.Normal))

val LocalPilotComplete = androidx.compose.runtime.staticCompositionLocalOf<(() -> Unit)?> { null }
val LocalPilotStop = androidx.compose.runtime.staticCompositionLocalOf<(() -> Unit)?> { null }
object V2C {
    var highContrast by androidx.compose.runtime.mutableStateOf(false)
    val BgTop get() = if (highContrast) Color(0xFFFFFFFF) else Color(0xFFFDFAF6)
    val BgBottom get() = if (highContrast) Color(0xFFFFFFFF) else Color(0xFFF8F4FA)
    val Title get() = if (highContrast) Color(0xFF321168) else Color(0xFF7B4FF0)
    val Ink get() = if (highContrast) Color(0xFF111111) else Color(0xFF2E2E4A)
    val Muted get() = if (highContrast) Color(0xFF333333) else Color(0xFF625A76)
    val Surface = Color(0xFFFFFFFF)
    val CardInner get() = if (highContrast) Color(0xFFF4F4F4) else Color(0xFFF1EDFA)
    val BannerBg get() = if (highContrast) Color(0xFFF2F2F2) else Color(0xFFEFEBFA)
    val BannerIcon get() = if (highContrast) Color(0xFF321168) else Color(0xFF7B4FF0)
    val GreenBg = Color(0xFFE6F6EC)
    val GreenText get() = if (highContrast) Color(0xFF124127) else Color(0xFF256B46)
    val GradStart get() = if (highContrast) Color(0xFF321168) else Color(0xFF4A6CF8)
    val GradEnd get() = if (highContrast) Color(0xFF321168) else Color(0xFFA64DF3)
    val Shadow = Color(0x337B4FF0)
}

val V2Gradient get() = Brush.horizontalGradient(listOf(V2C.GradStart, V2C.GradEnd))

/** Paint-only porcelain finish: no size, padding, hit target, or state changes. */
internal fun Modifier.v2PorcelainFinish(shape: Shape, enabled: Boolean = true, lavenderBase: Boolean = false): Modifier {
    if (V2C.highContrast || !enabled) return this
    return drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        val glaze = Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.38f), Color.White.copy(alpha = 0.04f), if (lavenderBase) Color(0xFFE8E1F7).copy(alpha = 0.14f) else Color.White.copy(alpha = 0.14f)),
        )
        onDrawWithContent {
            drawOutline(outline, glaze)
            drawOutline(outline, Color.White.copy(alpha = 0.68f), style = Stroke(0.8.dp.toPx()))
            drawContent()
        }
    }
}

/** Low-opacity glaze stays behind button content and is disabled in high contrast. */
internal fun Modifier.v2ButtonGlaze(shape: Shape, enabled: Boolean = true): Modifier {
    if (V2C.highContrast || !enabled) return this
    return drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        val glaze = Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.035f), Color.Transparent, Color(0xFF321168).copy(alpha = 0.045f)),
        )
        onDrawWithContent {
            // Keep the gradient tokens unchanged; dim only this enabled button's reading surface.
            drawOutline(outline, Color.Black.copy(alpha = 0.13f))
            drawOutline(outline, glaze)
            drawOutline(outline, Color.White.copy(alpha = 0.24f), style = Stroke(0.8.dp.toPx()))
            drawContent()
        }
    }
}

/** Original microphone face: a shallow upper light and inner rim, without animation. */
internal fun Modifier.v2MicrophoneResin(): Modifier {
    if (V2C.highContrast) return this
    return drawWithCache {
        val outline = CircleShape.createOutline(size, layoutDirection, this)
        // A continuous curved reflection replaces the previous horizontal reading shade.
        // At y >= 20%, the radial falloff contributes less than 3.5% white behind glyphs.
        val upperLight = Brush.radialGradient(
            0f to Color.White.copy(alpha = 0.10f),
            0.15f to Color.White.copy(alpha = 0.035f),
            1f to Color.Transparent,
            center = Offset(size.width * 0.19f, size.height * 0.08f),
            radius = size.minDimension * 0.60f,
        )
        onDrawWithContent {
            drawOutline(outline, upperLight)
            drawOutline(outline, Color.White.copy(alpha = 0.32f), style = Stroke(1.dp.toPx()))
            drawContent()
        }
    }
}

/** Apply only to the decorative image; the parent draws native icon/text siblings afterward. */
internal fun Modifier.v2MicAssetWash(): Modifier {
    if (V2C.highContrast) return this
    return drawWithCache {
        val wash = Brush.radialGradient(
            0f to Color(0xFF7535CC).copy(alpha = 0.40f),
            1f to Color.Transparent,
            center = Offset(size.width * 0.80f, size.height * 0.68f),
            radius = size.minDimension * 0.42f,
        )
        onDrawWithContent {
            drawContent()
            drawRect(wash)
        }
    }
}

fun jua(size: Int, color: Color, lineMul: Float = 1.35f) = TextStyle(
    fontFamily = JuaV2, fontWeight = FontWeight.Normal,
    fontSize = size.sp, lineHeight = (size * lineMul).sp, color = color,
    lineBreak = LineBreak.Paragraph.copy(wordBreak = LineBreak.WordBreak.Phrase),
    localeList = LocaleList("ko-KR"),
)

/** 화면 배경(연보라 그라데이션) + 안전영역. */
@Composable
fun V2Screen(background: Color? = null, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val motion = rememberV2MotionEnabled()
    var entered by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) { entered = true }
    val entranceAlpha by androidx.compose.animation.core.animateFloatAsState(
        if (entered || !motion) 1f else 0.96f,
        androidx.compose.animation.core.tween(if (motion) 140 else 0), label = "screenEntrance",
    )
    // Insets modifiers consume the padding they apply. A child form's imePadding
    // therefore adds only any remaining inset, rather than applying the keyboard twice.
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(if (background == null) listOf(V2C.BgTop, V2C.BgBottom) else listOf(background, background)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        // Use available window width, not a fixed phone aspect ratio. Content remains
        // a readable single pane on wider windows; the background fills the window.
        Column(
            Modifier.align(Alignment.TopCenter).fillMaxHeight().widthIn(max = 600.dp).fillMaxWidth()
                .then(Modifier.graphicsLayerCompatAlpha(entranceAlpha)),
        ) {
            content()
            if (BuildConfig.PILOT_TOOLS_ENABLED) LocalPilotComplete.current?.let { complete ->
                androidx.compose.material3.TextButton(onClick = complete, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("실증 과업 마치기 · 지원자", style = jua(17, V2C.Ink))
                }
            }
            if (BuildConfig.PILOT_TOOLS_ENABLED) LocalPilotStop.current?.let { stop ->
                androidx.compose.material3.TextButton(onClick = stop, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("실증 그만하기", style = jua(17, V2C.Ink))
                }
            }
        }
    }
}

/** 상단 헤더: 뒤로가기(선택) · 로고 · 도움. */
@Composable
fun V2Header(onBack: (() -> Unit)? = null, onHelp: () -> Unit, logoStart: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            Box(
                Modifier
                    .size(48.dp)
                    .shadow(3.dp, CircleShape, clip = false, spotColor = V2C.Shadow)
                    .background(V2C.Surface, CircleShape)
                    .clip(CircleShape)
                    .v2PorcelainFinish(CircleShape)
                    .clickable(role = Role.Button, onClickLabel = "뒤로 가기") { onBack() },
                contentAlignment = Alignment.Center,
            ) { Image(painterResource(R.drawable.ic_back), null, Modifier.size(22.dp)) }
        }
        Box(Modifier.weight(1f).padding(horizontal = 8.dp), contentAlignment = if (logoStart) Alignment.CenterStart else Alignment.Center) {
            Image(painterResource(R.drawable.v2_logo), contentDescription = "말잇다", modifier = Modifier.widthIn(max = 120.dp).height(36.dp), contentScale = ContentScale.Fit)
        }
        Box(
            Modifier
                .heightIn(min = 48.dp)
                .shadow(3.dp, CircleShape, clip = false, spotColor = V2C.Shadow)
                .background(V2C.Surface, CircleShape)
                .clip(CircleShape)
                .v2PorcelainFinish(CircleShape)
                .clickable(role = Role.Button, onClickLabel = "도움말 열기") { onHelp() }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) { Text("도움", style = jua(16, V2C.Title)) }
    }
}

@Composable
fun V2Title(text: String, modifier: Modifier = Modifier) {
    Text(
        text, style = jua(28, V2C.Title, 1.3f), textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).semantics { heading() },
    )
}

@Composable
fun V2SubTitle(text: String) {
    Text(
        text, style = jua(16, V2C.Ink), textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
    )
}

/** 가득 찬 그라데이션 주 버튼(높이 64dp, 알약형). */
@Composable
fun V2PrimaryButton(text: String, modifier: Modifier = Modifier, icon: String? = null, enabled: Boolean = true, onClick: () -> Unit) {
    val feedback = rememberV2Feedback()
    val press = rememberV2Press(enabled)
    val emphasis = LocalA11y.current.visualEmphasis
    Box(
        modifier.then(press.modifier)
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .heightIn(min = 64.dp)
            .shadow(6.dp, RoundedCornerShape(32.dp), clip = false, ambientColor = if (V2C.highContrast) Color.Black else Color(0x1F615188), spotColor = V2C.Shadow)
            .background(if (enabled) V2Gradient else Brush.horizontalGradient(listOf(Color(0xFFDCD8E8), Color(0xFFDCD8E8))), RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .v2ButtonGlaze(RoundedCornerShape(32.dp), enabled)
            .then(if (emphasis) Modifier.border(3.dp, V2C.Ink, RoundedCornerShape(32.dp)) else Modifier)
            .clickable(interactionSource = press.interaction, indication = androidx.compose.material3.ripple(), role = Role.Button, enabled = enabled) { feedback(); onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(if (icon != null) "$icon  $text" else text, style = jua(20, if (enabled) Color.White else V2C.Muted), textAlign = TextAlign.Center)
    }
}

/** 흰 배경 보조 버튼. */
@Composable
fun V2SecondaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    val feedback = rememberV2Feedback()
    val press = rememberV2Press(enabled)
    val emphasis = LocalA11y.current.visualEmphasis
    Box(
        modifier.then(press.modifier)
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .heightIn(min = 60.dp)
            .shadow(4.dp, RoundedCornerShape(30.dp), clip = false, ambientColor = if (V2C.highContrast) Color.Black else Color(0x1A5F5277), spotColor = if (V2C.highContrast) V2C.Shadow else Color(0x2E5F5277))
            .background(if (enabled) V2C.Surface else V2C.CardInner, RoundedCornerShape(30.dp))
            .clip(RoundedCornerShape(30.dp))
            .v2PorcelainFinish(RoundedCornerShape(30.dp), enabled, lavenderBase = true)
            .then(if (emphasis) Modifier.border(3.dp, V2C.Ink, RoundedCornerShape(30.dp)) else Modifier)
            .clickable(interactionSource = press.interaction, indication = androidx.compose.material3.ripple(), enabled = enabled, role = Role.Button) { feedback(); onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = jua(20, if (enabled) V2C.Title else V2C.Muted), textAlign = TextAlign.Center) }
}

/** 맨 아래 글자만 있는 버튼(취소 등). */
@Composable
fun V2TextButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    val feedback = rememberV2Feedback()
    val press = rememberV2Press(enabled)
    Box(
        modifier.then(press.modifier)
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(interactionSource = press.interaction, indication = androidx.compose.material3.ripple(), enabled = enabled, role = Role.Button) { feedback(); onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = jua(18, V2C.Muted.copy(alpha = if (enabled) 1f else 0.55f)), textAlign = TextAlign.Center) }
}

/** 안내 배너(연보라/연녹색). */
@Composable
fun V2Banner(text: String, icon: ImageVector? = null, green: Boolean = false, leading: (@Composable () -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(if (green) V2C.GreenBg else V2C.BannerBg, RoundedCornerShape(22.dp))
            .v2PorcelainFinish(RoundedCornerShape(22.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            leading != null -> { leading(); Spacer(Modifier.width(14.dp)) }
            icon != null -> {
                Icon(icon, null, tint = if (green) V2C.GreenText else V2C.BannerIcon, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(14.dp))
            }
        }
        Text(text, style = jua(17, if (green) V2C.GreenText else V2C.Ink, 1.5f))
    }
}

/** 큰 흰색 카드 컨테이너. */
@Composable
fun V2Card(modifier: Modifier = Modifier, radius: Int = 32, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(8.dp, RoundedCornerShape(radius.dp), clip = false, ambientColor = if (V2C.highContrast) Color.Black else Color(0x1A5F5277), spotColor = if (V2C.highContrast) V2C.Shadow else Color(0x2E5F5277))
            .background(V2C.Surface, RoundedCornerShape(radius.dp))
            .clip(RoundedCornerShape(radius.dp))
            .v2PorcelainFinish(RoundedCornerShape(radius.dp), lavenderBase = true),
        content = content,
    )
}

/** 겹친 카드 모양 글리프(디자인의 '카드' 아이콘). */
@Composable
fun CardGlyph(size: Int = 22, tint: Color = V2C.BannerIcon) {
    val corner = RoundedCornerShape((size * 0.2f).dp)
    Box(Modifier.size(size.dp)) {
        Box(
            Modifier
                .size((size * 0.72f).dp)
                .align(Alignment.TopEnd)
                .border(2.dp, tint, corner)
        )
        Box(
            Modifier
                .size((size * 0.72f).dp)
                .align(Alignment.BottomStart)
                .background(V2C.BannerBg, corner)
                .border(2.dp, tint, corner)
        )
    }
}

/**
 * 앱에 내장된 의미 그림 목록. 그림 선택 화면(U05-M)이 이 순서대로 보여 준다.
 * 새 그림 추가: res/drawable-nodpi/v2_card_<키>.png를 넣고 여기에 한 줄 추가(docs/meaning-image-guide.md).
 * 키는 DB(meanings.imageKey)에 저장되므로 한 번 쓴 키는 바꾸거나 지우지 않는다.
 */
val BUILTIN_MEANING_IMAGES: List<Pair<String, Int>> = listOf(
    "walk" to R.drawable.v2_card_walk,
    "water" to R.drawable.v2_card_water,
    "rest" to R.drawable.v2_card_rest,
    "cold" to R.drawable.v2_card_cold,
)

private val BUILTIN_IMAGE_MAP = BUILTIN_MEANING_IMAGES.toMap()

/** 내장 의미 그림 키 → 리소스. 모르는 키·null은 그림 없음(U03-I)으로 다룬다. */
fun meaningImageRes(imageKey: String?): Int? = imageKey?.let { BUILTIN_IMAGE_MAP[it] }

@Composable
fun meaningPainter(imageKey: String?): Painter? = meaningImageRes(imageKey)?.let { painterResource(it) }

/** 사진 키("file:…")면 저장된 비트맵을 불러온다. */
@Composable
fun meaningFileBitmap(imageKey: String?): ImageBitmap? {
    val ctx = LocalContext.current
    return androidx.compose.runtime.remember(imageKey) {
        kr.voicemate.malitda.data.repo.MeaningImageStore.fileFor(ctx.filesDir, imageKey)
            ?.let { runCatching { android.graphics.BitmapFactory.decodeFile(it.path) }.getOrNull() }
            ?.asImageBitmap()
    }
}

/** 보여줄 수 있는 그림(내장·사진)이 있는가 — 없으면 U03-I 모습을 쓴다. */
@Composable
fun meaningImageAvailable(imageKey: String?): Boolean =
    meaningImageRes(imageKey) != null || meaningFileBitmap(imageKey) != null

/** 의미 그림 하나를 그린다: 내장 리소스 → 저장 사진 → 자리표시(카드 글리프) 순. */
@Composable
fun MeaningPicture(imageKey: String?, modifier: Modifier, contentScale: ContentScale = ContentScale.Crop, glyphSize: Int = 34) {
    val res = meaningImageRes(imageKey)
    if (res != null) {
        Image(painterResource(res), null, modifier, contentScale = contentScale)
        return
    }
    val bmp = meaningFileBitmap(imageKey)
    if (bmp != null) {
        Image(bmp, null, modifier, contentScale = contentScale)
        return
    }
    Box(modifier.background(V2C.CardInner), contentAlignment = Alignment.Center) { CardGlyph(size = glyphSize) }
}

/** 이름 호격: 받침 있으면 '아', 없으면 '야'. */
fun vocative(name: String): String {
    if (name.isBlank()) return ""
    val last = name.last()
    if (last !in '가'..'힣') return name
    return name + if ((last - '가') % 28 > 0) "아" else "야"
}

/** 동반 조사: 받침 있으면 '과', 없으면 '와'. */
fun withJosa(name: String): String {
    if (name.isBlank()) return ""
    val last = name.last()
    if (last !in '가'..'힣') return name + "와"
    return name + if ((last - '가') % 28 > 0) "과" else "와"
}

/** 도구 조사: 받침 없거나 ㄹ 받침이면 '로', 그 외 받침은 '으로'. */
fun roJosa(name: String): String {
    if (name.isBlank()) return ""
    val last = name.last()
    if (last !in '가'..'힣') return name + "로"
    val jong = (last - '가') % 28
    return name + if (jong == 0 || jong == 8) "로" else "으로"
}

/** 주격 조사: 받침 있으면 '이', 없으면 '가'. */
fun iGa(name: String): String {
    if (name.isBlank()) return ""
    val last = name.last()
    if (last !in '가'..'힣') return name + "가"
    return name + if ((last - '가') % 28 > 0) "이" else "가"
}

/** v2 입력 칸(U05). error면 붉은 테두리·분홍 배경(U05-E). */
@Composable
fun V2TextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier
            .fillMaxWidth()
            .background(if (error) Color(0xFFFDF1F3) else V2C.Surface, shape)
            .border(1.5.dp, if (error) Color(0xFFE2708A) else Color(0xFFE4DEF2), shape)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            Text(placeholder, style = jua(18, if (error) Color(0xFFA52D4C) else V2C.Muted))
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = jua(18, V2C.Ink),
            singleLine = true,
            cursorBrush = androidx.compose.ui.graphics.SolidColor(V2C.Title),
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = placeholder },
        )
    }
}

/** 하단 탭. selected: "home" | "library" | "register" | "settings". */
@Composable
fun V2BottomNav(selected: String, onHome: () -> Unit, onLibrary: () -> Unit, onSettings: () -> Unit, onRegister: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(6.dp, RoundedCornerShape(28.dp), clip = false, ambientColor = if (V2C.highContrast) Color.Black else Color(0x1A5F5277), spotColor = if (V2C.highContrast) V2C.Shadow else Color(0x2E5F5277))
            .background(V2C.Surface, RoundedCornerShape(28.dp))
            .v2PorcelainFinish(RoundedCornerShape(28.dp), lavenderBase = true)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        NavItem(Icons.Rounded.Home, "홈", selected = selected == "home", modifier = Modifier.weight(1f), onClick = onHome)
        NavItem(Icons.Rounded.Apps, "내 표현", selected = selected == "library", modifier = Modifier.weight(1f), onClick = onLibrary)
        NavItem(Icons.Rounded.AddCircle, "함께 관리", selected = selected == "register", modifier = Modifier.weight(1f), onClick = onRegister)
        NavItem(Icons.Rounded.Settings, "설정", selected = selected == "settings", modifier = Modifier.weight(1f), onClick = onSettings)
    }
}

/** 화면별 도움말 창. '도움' 버튼으로 연다. */
@Composable
fun V2HelpDialog(text: String, onClose: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onClose) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .background(V2C.Surface, RoundedCornerShape(28.dp))
                .padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("도움", style = jua(24, V2C.Title))
            Spacer(Modifier.height(14.dp))
            Text(text, style = jua(18, V2C.Ink, 1.6f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .background(V2Gradient, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .clickable(role = Role.Button) { onClose() }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) { Text("알겠어요", style = jua(20, Color.White)) }
        }
    }
}

/** 화면 ID별 도움말 문구. */
fun helpTextFor(route: String): String = when (route) {
    "U00" -> "말잇다는 학생이 평소 쓰는 말을\n상대가 알아들을 수 있게 이어 줘요.\n처음에는 지원자와 함께 뜻을 등록해요."
    "U01" -> "'말하기'를 누르고 천천히 말해요.\n말하기 어려우면 아래 카드에서\n전하고 싶은 뜻을 골라도 돼요."
    "U02", "U02D" -> "말이 끝나면 '말하기 끝내기'를 눌러요.\n잘 안 되면 '카드로 고르기'를 눌러요."
    "U03" -> "그림·문장·소리로 뜻을 확인합니다.\n맞는 뜻, 다른 뜻, 잘 모르겠음 중에서 선택합니다.\n확인하지 않은 뜻은 전달하지 않습니다."
    "U03H" -> "아직 뜻을 확정하지 않은 상태입니다.\n익숙한 사람과 다시 확인하거나 다른 카드를 고릅니다.\n판단이 어려우면 그만해도 됩니다."
    "U03E", "U03M" -> "다시 말하거나, 카드에서 뜻을 골라요.\n자주 쓰는 말이면 지원자와 함께 관리해요."
    "U04", "U04F", "U04S", "U13" -> "이 화면을 상대에게 보여 주거나\n'소리로 들려주기'를 눌러 전해요."
    "U06" -> "그림을 보고 전하고 싶은 뜻을 골라요.\n없으면 '여기에 없어요'를 눌러요."
    "U05", "U05M" -> "학생이 평소 쓰는 말과 전할 뜻을 적어요.\n그림을 고르면 글을 읽기 어려운 학생도\n뜻을 확인할 수 있어요."
    "U07", "U07S" -> "누구의 표현인지 먼저 확인해요.\n뜻은 지원자 혼자 정하지 않고\n학생과 함께 확인해요."
    "U10" -> "학생에게 그림·문장·소리를 보여 줍니다.\n학생의 반응과 답한 방법을 따로 선택합니다.\n맞는 뜻이라는 응답을 확인한 경우에만 사용합니다.\n다름·모름·무응답이면 미확인으로 둡니다."
    "U08", "U09", "U09D", "U11" -> "등록한 뜻을 고치거나 잠시 쉬게 할 수 있어요.\n바꾼 뜻은 학생과 다시 확인해야 쓰여요."
    "U14" -> "실증 기록은 참여 동의를 확인한 경우에만 남깁니다.\n참여자 코드와 시도 결과를 확인하고 선택해 내보냅니다.\n음성·인식 원문·뜻 문장은 내보내지 않습니다."
    "U16" -> "들은 원문과 후보를 비교합니다.\n원하는 문장이 없으면 직접 고칠 수 있습니다.\n고르기 전에는 전달하지 않습니다."
    "U17" -> "글이나 소리로 이번 문장을 확인합니다.\n쉬운 말은 비교한 뒤 직접 적용합니다.\n잘 모르겠으면 멈추고 다시 확인할 수 있습니다."
    "U17H" -> "아직 문장을 확정하지 않았습니다.\n다시 확인하거나 다른 카드를 고릅니다.\n지금 그만해도 됩니다."
    "U18" -> "확인한 문장을 소리·복사·앱 공유로 전합니다.\n공유할 앱을 고른 뒤 그 앱에서 마지막 행동을 확인합니다.\n다음에도 쓸 수정은 따로 선택해 저장합니다."
    "U19" -> "직접 저장한 수정만 보입니다.\n다시 쓸 때도 문장을 확인해야 합니다.\n필요 없는 기록은 하나씩 지울 수 있습니다."
    "U15" -> "학생이 보기 편한 그림체를 골라요.\n'그림 만들기'로 새 그림을 만들 때 쓰여요.\n고르는 것만으로 뜻이 확정되지는 않아요."
    else -> "궁금한 점은 지원자와 함께 확인해요."
}

/** 붉은 위험 버튼(U09-D 삭제하기). */
@Composable
fun V2DangerButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val press = rememberV2Press()
    Box(
        modifier.then(press.modifier)
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .heightIn(min = 64.dp)
            .shadow(6.dp, RoundedCornerShape(32.dp), clip = false, spotColor = Color(0x33E2566E))
            .background(
                Brush.horizontalGradient(listOf(Color(0xFFB52B4E), Color(0xFFAD2441))),
                RoundedCornerShape(32.dp),
            )
            .clip(RoundedCornerShape(32.dp))
            .clickable(interactionSource = press.interaction, indication = androidx.compose.material3.ripple(), role = Role.Button) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = jua(20, Color.White), textAlign = TextAlign.Center) }
}

@Composable
private fun NavItem(icon: ImageVector, label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val tint = if (selected) V2C.Title else V2C.Muted
    val emphasis = LocalA11y.current.visualEmphasis
    val feedback = rememberV2Feedback()
    val press = rememberV2Press()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.then(press.modifier)
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(18.dp))
            .then(if (emphasis && selected) Modifier.border(3.dp, V2C.Ink, RoundedCornerShape(18.dp)) else Modifier)
            .selectable(selected = selected, role = Role.Tab, interactionSource = press.interaction, indication = androidx.compose.material3.ripple(), onClick = { feedback(); onClick() })
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .semantics { contentDescription = label },
    ) {
        Box(
            Modifier
                .size(width = 56.dp, height = 34.dp)
                .background(if (selected) V2C.BannerBg else Color.Transparent, RoundedCornerShape(17.dp))
                .then(if (selected) Modifier.v2PorcelainFinish(RoundedCornerShape(17.dp)) else Modifier),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp)) }
        Spacer(Modifier.height(4.dp))
        Text(label, style = jua(13, tint), textAlign = TextAlign.Center, softWrap = true, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun rememberNoRipple(): MutableInteractionSource =
    androidx.compose.runtime.remember { MutableInteractionSource() }

/** Uses native feedback so the device's vibration preference still applies. */
@Composable
private fun rememberV2Feedback(): () -> Unit {
    val view = LocalView.current
    val enabled = LocalA11y.current.haptics
    return { if (enabled) view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP) }
}
