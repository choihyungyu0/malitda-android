package kr.voicemate.malitda.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.voicemate.malitda.R
import kr.voicemate.malitda.ui.theme.LocalA11y
import kr.voicemate.malitda.ui.theme.MColors
import kr.voicemate.malitda.ui.theme.PrimaryGradient
import kr.voicemate.malitda.ui.theme.TitleGradient

/** 화면 공통 골격: 상단 로고(또는 뒤로가기) + 스크롤 본문 + 하단 고정 액션. */
@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    showLogo: Boolean = true,
    onBack: (() -> Unit)? = null,
    topRight: (@Composable RowScope.() -> Unit)? = null,
    scrollable: Boolean = true,
    bottomBar: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(MColors.Bg)
            .safeDrawingPadding()
            .imePadding()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "뒤로 가기", tint = MColors.Ink)
                }
            } else {
                Spacer(Modifier.width(48.dp))
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (showLogo) {
                    Image(
                        painterResource(R.drawable.logo_header),
                        contentDescription = "말잇다",
                        modifier = Modifier.height(30.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
            Row(Modifier.width(96.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                if (topRight != null) topRight() else Spacer(Modifier.width(48.dp))
            }
        }
        val bodyMod = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp)
        Column(if (scrollable) bodyMod.verticalScroll(rememberScrollState()) else bodyMod) {
            Spacer(Modifier.height(8.dp))
            content()
            Spacer(Modifier.height(16.dp))
        }
        if (bottomBar != null) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) { bottomBar() }
        }
    }
}

/** 그라데이션 제목(원안의 보라 그라데이션 헤드라인). */
@Composable
fun GradientTitle(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Center, size: Int = 28) {
    val a11y = LocalA11y.current
    Text(
        text,
        modifier = modifier.fillMaxWidth().semantics { heading() },
        style = MaterialTheme.typography.headlineLarge.copy(
            brush = if (a11y.visualEmphasis) SolidColor(MColors.Indigo) else TitleGradient,
            fontSize = size.sp, lineHeight = (size * 1.35f).sp, fontWeight = FontWeight.ExtraBold,
        ),
        textAlign = textAlign,
    )
}

@Composable
fun Subtitle(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Center) {
    Text(text, modifier = modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium, color = MColors.Ink2, textAlign = textAlign)
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color = MColors.Violet) {
    Text(text, modifier = modifier.semantics { heading() }, style = MaterialTheme.typography.labelMedium, color = color)
}

/** 흰 카드(그림자 약하게). */
@Composable
fun MCard(
    modifier: Modifier = Modifier,
    color: Color = MColors.Surface,
    border: Color? = null,
    padding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    var m = modifier.fillMaxWidth().shadow(if (color == MColors.Surface) 3.dp else 0.dp, shape, spotColor = Color(0x22000000)).clip(shape).background(color)
    if (border != null) m = m.border(1.5.dp, border, shape)
    if (onClick != null) m = m.clickable(role = Role.Button, onClick = onClick)
    if (contentDescription != null) m = m.semantics { this.contentDescription = contentDescription }
    Column(m.padding(padding), content = content)
}

/** 주 액션: 그라데이션 알약 버튼. 비활성이면 잠김 색. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    brush: Brush = PrimaryGradient,
    contentDescription: String? = null,
) {
    val shape = RoundedCornerShape(30.dp)
    Box(
        modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(if (enabled) 6.dp else 0.dp, shape, spotColor = MColors.Violet.copy(alpha = 0.35f))
            .clip(shape)
            .background(if (enabled) brush else SolidColor(MColors.Locked))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { if (!enabled) disabled(); if (contentDescription != null) this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icon != null) { Icon(icon, null, tint = if (enabled) Color.White else MColors.LockedText, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(8.dp)) }
            Text(text, color = if (enabled) Color.White else MColors.LockedText, style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold))
        }
    }
}

/** 보조 액션: 흰 배경 알약(테두리). */
@Composable
fun OutlineButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true, color: Color = MColors.Violet) {
    val shape = RoundedCornerShape(30.dp)
    Box(
        modifier.fillMaxWidth().height(54.dp).clip(shape).background(Color.White).border(1.5.dp, if (enabled) color.copy(alpha = 0.5f) else MColors.Line, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick).semantics { if (!enabled) disabled() },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) { Icon(icon, null, tint = if (enabled) color else MColors.Muted, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)) }
            Text(text, color = if (enabled) color else MColors.Muted, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** 텍스트 링크형 보조 액션(48dp 터치 영역 확보). */
@Composable
fun TextAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = MColors.Violet) {
    Box(
        modifier.fillMaxWidth().heightIn(min = 48.dp).clip(RoundedCornerShape(24.dp)).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = color, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun InfoBox(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null, color: Color = MColors.Lavender, textColor: Color = MColors.Ink2) {
    Row(modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(color).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) { Icon(icon, null, tint = MColors.Violet, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(10.dp)) }
        Text(text, style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp), color = textColor)
    }
}

@Composable
fun CharacterImage(resId: Int, size: Dp, modifier: Modifier = Modifier, contentDescription: String? = null) {
    Image(painterResource(resId), contentDescription = contentDescription, modifier = modifier.size(size), contentScale = ContentScale.Fit)
}

/** 원안의 체크 원형 아이콘(선택 표시). */
@Composable
fun CheckCircle(checked: Boolean, modifier: Modifier = Modifier, size: Dp = 26.dp) {
    Box(
        modifier.size(size).clip(CircleShape).background(if (checked) PrimaryGradient else SolidColor(Color.Transparent)).border(2.dp, if (checked) Color.Transparent else MColors.Line, CircleShape),
        contentAlignment = Alignment.Center,
    ) { if (checked) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(size * 0.65f)) }
}

@Composable
fun NumberBadge(n: Int, active: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier.size(30.dp).clip(CircleShape).background(if (active) PrimaryGradient else SolidColor(MColors.LavenderDeep)),
        contentAlignment = Alignment.Center,
    ) { Text("$n", color = if (active) Color.White else MColors.Violet, style = MaterialTheme.typography.labelMedium) }
}

@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier, textColor: Color = Color.White) {
    Surface(modifier, shape = RoundedCornerShape(50), color = color) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = textColor)
    }
}
