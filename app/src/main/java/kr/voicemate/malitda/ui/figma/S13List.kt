package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

private fun catColor(c: Category) = when (c) {
    Category.NAME -> Color(0xFF457EFF); Category.PLACE -> Color(0xFF00BFAD)
    Category.TIME -> Color(0xFFFFB523); Category.MESSAGE -> Color(0xFFF35093); Category.OFTEN -> Color(0xFF9656F4)
}
private fun catIcon(c: Category) = when (c) {
    Category.NAME -> R.drawable.ic_person; Category.PLACE -> R.drawable.ic_pin
    Category.TIME -> R.drawable.ic_clock; Category.MESSAGE -> R.drawable.ic_chat; Category.OFTEN -> R.drawable.ic_star
}
private data class Filter(val cat: Category, val label: String, val bg: Color, val x: Float, val w: Float, val labelX: Float, val labelW: Float, val fs: Float)
private val S13_FILTERS = listOf(
    Filter(Category.NAME, "이름", Color(0xFFF0F4FF), 19.2f, 60.35f, 51.21f, 26.342f, 11f),
    Filter(Category.PLACE, "장소", Color(0xFFEFFCF7), 87.78f, 60.81f, 119.79f, 26.801f, 11f),
    Filter(Category.TIME, "시간", Color(0xFFFFFAED), 155.91f, 60.35f, 187.46f, 26.804f, 11f),
    Filter(Category.MESSAGE, "메시지", Color(0xFFFFF0F6), 223.58f, 64.01f, 253.75f, 31.839f, 11f),
    Filter(Category.OFTEN, "자주 쓰는 말", Color(0xFFF7F0FF), 295.36f, 82.75f, 321.88f, 54.234f, 9.5f),
)

/**
 * S13 · 내 핵심표현(홈 목록) — 구현정본 node 86:207. 실제 저장 표현을 필터·즐겨찾기와 함께 표시.
 */
@Composable
fun S13List(
    expressions: List<ExpressionEntity>,
    count: Int,
    filter: Category?,
    query: String = "",
    onFilter: (Category) -> Unit,
    onSearch: () -> Unit = {},
    onDetail: (Long) -> Unit,
    onToggleFav: (Long) -> Unit,
    onAdd: () -> Unit,
    onMenu: () -> Unit,
) {
    val shown = expressions.filter {
        (filter == null || it.category == filter.key) &&
            (query.isBlank() || it.text.contains(query, true) || it.label.contains(query, true))
    }
    FaithfulFrame {
        // 헤더 배경(검색 + 필터 탭)
        val searchShape = RoundedCornerShape(d(14f))
        Box(Modifier.box(20.12f, 161.85f, 349.77f, 38.41f).background(MalSurface, searchShape).border(BorderStroke(d(1f), Color(0xFFDED9E5)), searchShape))
        S13_FILTERS.forEach { f ->
            val sel = filter == f.cat
            val tab = RoundedCornerShape(d(15.545f))
            Box(
                Modifier.box(f.x, 212.6f, f.w, 31.09f).background(f.bg, tab)
                    .border(BorderStroke(if (sel) d(1.5f) else d(1f), if (sel) Color(0xFF7546EB) else Color(0xFFE5DEED)), tab),
            )
        }
        // 추가 버튼 배경
        Box(Modifier.box(19.66f, 770.4f, 276.15f, 43.89f).background(brandBrush(), RoundedCornerShape(d(21.945f))))

        // 아트 + 로고 + 아이콘
        artCrop(R.drawable.pose_s05_place, 228.6f, 57.15f, 161.395f, 105.158f)
        art(R.drawable.brand_logo, 18.29f, 25.15f, 107.444f, 30.238f)
        Image(painterResource(R.drawable.ic_bell), null, Modifier.box(343.82f, 26.98f, 29.26f, 29.26f))

        // 헤더 텍스트
        T("내 핵심표현", 20f, 82f, 238f, 27f, FontWeight.Black, color = MalInk)
        T("자주 쓰는 표현을 모아두고\n필요할 때 쉽게 사용해 보세요.", 22f, 120f, 227f, 14f, FontWeight.Normal, color = MalMuted)
        T("$count / 50", 21f, 263f, 87f, 16f, FontWeight.Bold, color = Color(0xFF8F4AF1))
        T("저장한 표현", 112f, 267f, 192f, 12f, FontWeight.Normal, color = Color(0xFF8E8998))
        T(query.ifBlank { "표현을 검색해 보세요" }, 30.12f, 166.85f, 321.77f, 14f, FontWeight.Normal, color = if (query.isBlank()) Color(0xFF8E929C) else MalInk)
        S13_FILTERS.forEach { f -> T(f.label, f.labelX, 216.65f, f.labelW, f.fs, FontWeight.Bold, color = Color(0xFF7546EB), align = TextAlign.Center) }
        T("새 표현 추가", 143.56f, 784.11f, 128.019f, 17f, FontWeight.Bold, color = Color.White)

        // 목록(실데이터, 스크롤)
        if (shown.isEmpty()) {
            T("아직 등록한 표현이 없어요.\n아래 ‘새 표현 추가’로 시작해 보세요.", 20f, 430f, 351f, 14f, FontWeight.Normal, color = MalMuted, align = TextAlign.Center)
        } else {
            LazyColumn(
                modifier = Modifier.box(20f, 285f, 351f, 470f),
                verticalArrangement = Arrangement.spacedBy(d(4.44f)),
            ) {
                items(shown, key = { it.id }) { e -> ExprRow(e, onDetail, onToggleFav) }
            }
        }

        // ── 터치영역(헤더/필터/추가) ──
        Box(Modifier.box(341.99f, 21.03f, 31.09f, 33.83f).clickable(onClickLabel = "메뉴") { onMenu() }.semantics { contentDescription = "앱 메뉴" })
        Box(Modifier.box(20.12f, 161.85f, 349.77f, 38.41f).clickable(onClickLabel = "검색") { onSearch() }.semantics { contentDescription = "표현 검색" })
        S13_FILTERS.forEach { f ->
            Box(Modifier.box(f.x, 212.6f, f.w, 31.09f).clickable(onClickLabel = f.label) { onFilter(f.cat) }.semantics { contentDescription = "${f.label} 필터" })
        }
        Box(Modifier.box(19.66f, 770.4f, 276.15f, 43.89f).clickable(onClickLabel = "새 표현 추가") { onAdd() }.semantics { contentDescription = "새 표현 추가" })
    }
}

/** 표현 카드 한 줄. */
@Composable
private fun FrameScope.ExprRow(e: ExpressionEntity, onDetail: (Long) -> Unit, onToggleFav: (Long) -> Unit) {
    val cat = Category.fromKey(e.category)
    val shape = RoundedCornerShape(d(22f))
    Box(
        Modifier.fillMaxWidth().requiredHeight(d(55f)).shadow(d(2f), shape, clip = false).clip(shape).background(MalSurface)
            .clickable(onClickLabel = "상세") { onDetail(e.id) }
            .semantics { contentDescription = "${cat.label} 표현: ${e.text}" },
    ) {
        Image(painterResource(catIcon(cat)), null, Modifier.box(11f, 10.5f, 34f, 34f), colorFilter = ColorFilter.tint(catColor(cat)))
        T(cat.label, 61f, 5f, 171f, 11f, FontWeight.Bold, color = catColor(cat))
        T(e.text, 61f, 25f, 213f, 13f, FontWeight.Normal, color = MalInk)
        T(relTime(e.updatedAt), 278f, 21f, 56f, 10f, FontWeight.Normal, color = Color(0xFF96919C))
        Image(
            painterResource(if (e.favorite) R.drawable.ic_reward_heart else R.drawable.ic_heart_outline),
            null,
            Modifier.box(238.78f, 17f, 22f, 22f)
                .clickable(onClickLabel = "즐겨찾기") { onToggleFav(e.id) }
                .semantics { contentDescription = if (e.favorite) "즐겨찾기 해제" else "즐겨찾기" },
            colorFilter = if (e.favorite) ColorFilter.tint(Color(0xFFFF5D8F)) else null,
        )
        T("›", 325f, 15f, 16f, 14f, FontWeight.Normal, color = Color(0xFF96919C))
    }
}
