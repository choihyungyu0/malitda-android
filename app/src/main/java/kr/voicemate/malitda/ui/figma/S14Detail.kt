package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kr.voicemate.malitda.R
import kr.voicemate.malitda.data.db.ExpressionEntity
import kr.voicemate.malitda.domain.Category

private fun s14CatColor(c: Category) = when (c) {
    Category.NAME -> Color(0xFF3487FF); Category.PLACE -> Color(0xFF00B7A7)
    Category.TIME -> Color(0xFFE9A118); Category.MESSAGE -> Color(0xFFF35093); Category.OFTEN -> Color(0xFF9451F3)
}
private fun s14CatIcon(c: Category) = when (c) {
    Category.NAME -> R.drawable.ic_person; Category.PLACE -> R.drawable.ic_pin
    Category.TIME -> R.drawable.ic_clock; Category.MESSAGE -> R.drawable.ic_chat; Category.OFTEN -> R.drawable.ic_star
}
private fun fmtDate(ts: Long): String =
    java.text.SimpleDateFormat("yyyy.MM.dd (E) a h:mm", java.util.Locale.KOREAN).format(java.util.Date(ts))

/**
 * S14 · 표현 상세 — 구현정본 node 86:213. 저장한 표현 한 건의 상세·관리.
 */
@Composable
fun S14Detail(
    e: ExpressionEntity,
    onBack: () -> Unit,
    onToggleFav: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onListen: () -> Unit,
) {
    val cat = Category.fromKey(e.category)
    val col = s14CatColor(cat)
    FaithfulFrame {
        // ── 01 backgrounds ──
        val card = RoundedCornerShape(d(22f))
        Box(Modifier.box(22f, 172f, 346f, 457f).shadow(d(2f), card, clip = false).background(MalSurface, card))
        Box(Modifier.box(40f, 370f, 310f, 87f).background(Color(0xFFF7F3FE), RoundedCornerShape(d(17f))))
        val favShape = RoundedCornerShape(d(16f))
        Box(Modifier.box(264.27f, 189.74f, 85.96f, 32f).background(if (e.favorite) Color(0xFFF7ECFF) else MalSurface, favShape).border(BorderStroke(d(1f), Color(0xFFE5DEED)), favShape))
        Box(Modifier.box(21.95f, 647.87f, 346.11f, 46.64f).background(Brush.horizontalGradient(listOf(Color(0xFF545BFF), Color(0xFFB34DFB))), RoundedCornerShape(d(23.32f))))
        Box(Modifier.box(22.4f, 701.82f, 344.74f, 46.18f).background(Brush.horizontalGradient(listOf(Color(0xFFF989C1), Color(0xFFF054A6))), RoundedCornerShape(d(23.09f))))
        val listenShape = RoundedCornerShape(d(24f))
        Box(Modifier.box(22.4f, 755.31f, 344.74f, 48.01f).shadow(d(2f), listenShape, clip = false).background(MalSurface, listenShape).border(BorderStroke(d(1f), Color(0xFFE5DEED)), listenShape))

        // ── 02 art ──
        artCrop(R.drawable.pose_s05_time, 235.92f, 412.4f, 130.762f, 92.814f)

        // ── 03 icons ──
        Image(painterResource(s14CatIcon(cat)), null, Modifier.box(40f, 190f, 34f, 34f), colorFilter = ColorFilter.tint(col))
        Image(painterResource(R.drawable.ic_back), null, Modifier.box(19.66f, 23.77f, 35.21f, 35.21f))
        art(R.drawable.brand_logo, 134.88f, 34.75f, 123.447f, 34.741f)

        // ── 04 text ──
        T("표현 상세", 20f, 90f, 350f, 41f, FontWeight.Normal, brush = juaTitleBrush(), align = TextAlign.Center, family = JuaFont, lineMul = 1.2f)
        T("저장한 표현을 확인하고 관리해요.", 28f, 139f, 334f, 14f, FontWeight.Normal, color = MalMuted, align = TextAlign.Center)
        T(cat.label, 93f, 195f, 90f, 23f, FontWeight.Bold, color = col)
        T("대표 표현", 143f, 201f, 111f, 13f, FontWeight.Normal, color = col)
        T("표현 제목", 40f, 262f, 277f, 14f, FontWeight.Bold, color = MalMuted)
        T(e.label.ifBlank { e.text }, 40f, 287f, 306f, 24f, FontWeight.Bold, color = MalInk)
        T("표현 내용", 40f, 349f, 200f, 14f, FontWeight.Bold, color = MalMuted)
        T(e.text, 54f, 386f, 272f, 17f, FontWeight.Normal, color = MalInk)
        T("상태", 92f, 512f, 224f, 15f, FontWeight.Normal, color = MalMuted)
        T(if (e.favorite) "즐겨찾기한 표현이에요." else "등록한 표현이에요.", 92f, 537f, 247f, 17f, FontWeight.Normal, color = MalInk)
        T("등록·수정", 92f, 574f, 239f, 15f, FontWeight.Normal, color = MalMuted)
        T(fmtDate(e.updatedAt), 92f, 599f, 260f, 13f, FontWeight.Normal, color = MalInk)
        T("즐겨찾기", 270.27f, 194.24f, 73.96f, 11f, FontWeight.Bold, color = Color(0xFF7546EB), align = TextAlign.Center)
        T("수정", 149f, 656.19f, 120f, 22f, FontWeight.Normal, color = Color.White, align = TextAlign.Center, family = JuaFont)
        T("삭제", 148.77f, 709.91f, 120f, 22f, FontWeight.Normal, color = Color.White, align = TextAlign.Center, family = JuaFont)
        T("등록 표현 듣기", 138.77f, 764.32f, 140f, 22f, FontWeight.Normal, color = Color(0xFF7546EB), align = TextAlign.Center, family = JuaFont)

        // ── 05 hit areas ──
        Box(Modifier.box(19.66f, 23.77f, 35.21f, 37.03f).clickable(onClickLabel = "뒤로") { onBack() }.semantics { contentDescription = "내 핵심표현으로" })
        Box(Modifier.box(264.27f, 189.74f, 85.96f, 32f).clickable(onClickLabel = "즐겨찾기") { onToggleFav() }.semantics { contentDescription = "즐겨찾기" })
        Box(Modifier.box(21.95f, 647.87f, 346.11f, 46.64f).clickable(onClickLabel = "수정") { onEdit() }.semantics { contentDescription = "표현 수정" })
        Box(Modifier.box(22.4f, 701.82f, 344.74f, 46.18f).clickable(onClickLabel = "삭제") { onDelete() }.semantics { contentDescription = "표현 삭제" })
        Box(Modifier.box(22.4f, 755.31f, 344.74f, 48.01f).clickable(onClickLabel = "듣기") { onListen() }.semantics { contentDescription = "등록 표현 듣기" })
    }
}
