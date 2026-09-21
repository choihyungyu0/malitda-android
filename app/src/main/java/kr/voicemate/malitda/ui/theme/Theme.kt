package kr.voicemate.malitda.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

/** 접근성 설정: 시각 강조·햅틱을 화면 어디서나 읽을 수 있게 한다. */
data class A11yPrefs(val visualEmphasis: Boolean = false, val haptics: Boolean = true, val ttsRate: Float = 1.0f)
val LocalA11y = staticCompositionLocalOf { A11yPrefs() }

private val LightScheme = lightColorScheme(
    primary = MColors.Violet,
    onPrimary = Color.White,
    primaryContainer = MColors.Lavender,
    onPrimaryContainer = MColors.Ink,
    secondary = MColors.Blue,
    onSecondary = Color.White,
    background = MColors.Bg,
    onBackground = MColors.Ink,
    surface = MColors.Surface,
    onSurface = MColors.Ink,
    surfaceVariant = MColors.Lavender,
    onSurfaceVariant = MColors.Ink2,
    outline = MColors.Line,
    error = MColors.Danger,
    onError = Color.White,
)

val MalitdaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun MalitdaTheme(
    fontScale: Float = 1.0f,
    a11y: A11yPrefs = A11yPrefs(),
    content: @Composable () -> Unit,
) {
    val base = LocalDensity.current
    val density = Density(base.density, base.fontScale * fontScale)
    CompositionLocalProvider(LocalDensity provides density, LocalA11y provides a11y) {
        MaterialTheme(colorScheme = LightScheme, typography = MalitdaTypography, shapes = MalitdaShapes, content = content)
    }
}
