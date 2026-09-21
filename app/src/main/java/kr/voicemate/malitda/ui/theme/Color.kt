package kr.voicemate.malitda.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** 디자인 원안(PDF·Figma 목업)에서 샘플링한 팔레트. */
object MColors {
    val Bg = Color(0xFFFCFAF6)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceSoft = Color(0xFFF8F5FC)
    val Lavender = Color(0xFFF3F1FD)
    val LavenderDeep = Color(0xFFEDE7FB)
    val Ink = Color(0xFF272A34)
    val Ink2 = Color(0xFF5A5A63)
    val Muted = Color(0xFF7A708F)
    val Line = Color(0xFFE8E4F2)
    val Blue = Color(0xFF4671FA)
    val Indigo = Color(0xFF5B5BF6)
    val Violet = Color(0xFF7C5CFA)
    val Purple = Color(0xFF975CFB)
    val Magenta = Color(0xFFB45CF8)
    val Pink = Color(0xFFE54FD9)
    val Success = Color(0xFF22B573)
    val SuccessSoft = Color(0xFFE6F7EE)
    val Danger = Color(0xFFE5484D)
    val DangerSoft = Color(0xFFFDECEC)
    val Warn = Color(0xFFF5A623)
    val Locked = Color(0xFFD6CCEB)
    val LockedText = Color(0xFF7A708F)
    val ChipBlue = Color(0xFF41AFFC)
    val ChipGreen = Color(0xFF7CC418)
    val ChipYellow = Color(0xFFF2B300)
    val ChipPurple = Color(0xFF8B5CF6)
    val ChipPink = Color(0xFFFC7195)
    val Highlight = Color(0xFFFFE58F)
}

val PrimaryGradient = Brush.horizontalGradient(listOf(MColors.Blue, MColors.Purple))
val TitleGradient = Brush.horizontalGradient(listOf(Color(0xFF4B5BF5), Color(0xFF8B4EF6)))
val MicGradient = Brush.linearGradient(listOf(Color(0xFF666FFC), Color(0xFFB45CF8), Color(0xFFE54FD9)))
val SuccessGradient = Brush.horizontalGradient(listOf(Color(0xFF22B573), Color(0xFF3ACB8A)))
