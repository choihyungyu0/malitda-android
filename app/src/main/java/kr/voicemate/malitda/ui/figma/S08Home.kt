package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kr.voicemate.malitda.R

private val S08_ACCENT = Color(0xFF7142E8)

private data class QuickCat(val key: String, val label: String, val color: Color, val bg: Color, val art: Int, val x: Float)

/**
 * S08 · 홈 — 구현정본 node 86:177. 마이크 허브.
 * 화면 문장·개수는 예시 데이터(디자이너 규칙). 마이크·최근문장 탭은 실제 흐름으로 연결.
 */
@Composable
fun S08Home(
    name: String,
    recent: List<kr.voicemate.malitda.data.db.ExpressionEntity>,
    approvals: Int,
    streakDays: Int,
    onMic: () -> Unit,
    onRecent: (String) -> Unit,
    onCategory: (String) -> Unit,
    onMenu: () -> Unit,
    onReward: () -> Unit = {},
) {
    // 말친구 진행도(승인 횟수 기준). null = 전체 달성.
    val remaining = kr.voicemate.malitda.domain.Friends.nextUnlockIn(approvals)
    val step = kr.voicemate.malitda.domain.Friends.UNLOCK_STEP
    val within = if (remaining != null) (step - remaining).coerceIn(0, step) else step
    val rewardFrac = within.toFloat() / step
    val recentTops = listOf(503f, 532.7f, 562.4f)
    val catRowIcon = mapOf(
        "name" to R.drawable.ic_person, "place" to R.drawable.ic_pin, "time" to R.drawable.ic_clock,
        "message" to R.drawable.ic_chat, "often" to R.drawable.ic_star,
    )
    val recents = recent.take(3).mapIndexed { i, e -> Triple(catRowIcon[e.category] ?: R.drawable.ic_chat, e.text, recentTops[i]) }
    val cats = listOf(
        QuickCat("name", "이름", Color(0xFF3487FF), Color(0xFFEDF7FF), R.drawable.art_water, 21f),
        QuickCat("place", "장소", Color(0xFF00B7A7), Color(0xFFEDFFF8), R.drawable.art_green, 109f),
        QuickCat("time", "시간", Color(0xFFF8A910), Color(0xFFFFF8E5), R.drawable.art_yellow, 197f),
        QuickCat("often", "자주 쓰는 말", Color(0xFF9451F3), Color(0xFFF7EFFF), R.drawable.art_purple, 285f),
    )

    FaithfulFrame {
        // ── 01 backgrounds ──
        Box(Modifier.box(22f, 151f, 346f, 131f).background(Color(0xFFF5EEFF), RoundedCornerShape(d(19f))))
        Box(Modifier.box(35f, 255f, 128f, 7f).background(Color(0xFFE5DAFA), RoundedCornerShape(d(3.5f))))
        if (rewardFrac > 0f) Box(Modifier.box(35f, 255f, 128f * rewardFrac, 7f).background(brandBrush(), RoundedCornerShape(d(3.5f))))
        Box(Modifier.box(118f, 299f, 159f, 159f).background(Color(0xFFEEE7FE), CircleShape))
        Box(Modifier.box(132f, 313f, 130f, 130f).background(brandBrush(), CircleShape))
        val cardShape = RoundedCornerShape(d(22f))
        Box(Modifier.box(23f, 462f, 343f, 134f).shadow(d(2f), cardShape, clip = false).background(MalSurface, cardShape))
        cats.forEach { Box(Modifier.box(it.x, 637f, 81f, 92f).background(it.bg, RoundedCornerShape(d(16f)))) }
        Box(Modifier.box(23f, 751f, 343f, 59f).shadow(d(2f), cardShape, clip = false).background(MalSurface, cardShape))

        // ── 02 art ──
        art(R.drawable.art_purple, 278f, 64f, 109f)
        art(R.drawable.art_robot_mint, 236f, 156f, 106f)
        art(R.drawable.art_green, 13f, 358f, 110f)
        art(R.drawable.art_water, 275f, 355f, 103f)
        cats.forEach { art(it.art, it.x + 9f, 668f, 68f) }
        art(R.drawable.art_heart, 23f, 750f, 64f)

        // ── 03 icons ──
        art(R.drawable.brand_logo, 20f, 25f, 149f, 42f)
        Image(painterResource(R.drawable.ic_mic_white), null, Modifier.box(171f, 341f, 52f, 52f))
        recents.forEach { (icon, _, y) -> Image(painterResource(icon), null, Modifier.box(32f, y, 15f, 15f)) }
        Image(painterResource(R.drawable.ic_bell), null, Modifier.box(343.82f, 26.98f, 29.26f, 29.26f))
        // 리워드 하트 6개(연속 기록이 있을 때만 표시)
        if (streakDays > 0) for (i in 0..5) Image(painterResource(R.drawable.ic_reward_heart), null, Modifier.box(188f + i * 11f, 788f, 8f, 8f))

        // ── 04 text ──
        T("안녕하세요,\n${name.ifBlank { "친구" }}님", 20f, 76f, 222f, 24f, FontWeight.Bold, color = MalInk)
        T(if (remaining != null) "다음 말친구까지\n${remaining}번 남았어요!" else "모든 말친구를\n만났어요!", 35f, 166f, 199f, 16f, FontWeight.Bold, color = S08_ACCENT)
        T("표현을 등록하고\n새로운 친구를 만나 보세요.", 35f, 209f, 181f, 11f, FontWeight.Normal, color = MalMuted)
        T("$within / $step", 170f, 248f, 44f, 11f, FontWeight.Normal, color = S08_ACCENT)
        T("눌러서 말하기", 135f, 410f, 124f, 14f, FontWeight.Bold, color = Color.White, align = TextAlign.Center)
        T("내 말을\n들려줘!", 34f, 318f, 74f, 11f, FontWeight.Normal, color = MalMuted, align = TextAlign.Center)
        T("잘 듣고\n전달할게!", 292f, 320f, 70f, 11f, FontWeight.Normal, color = MalMuted, align = TextAlign.Center)
        T("최근 등록 문장", 34f, 473f, 190f, 13f, FontWeight.Bold, color = MalInk)
        if (recents.isNotEmpty()) T("더보기 ›", 309f, 474f, 50f, 10f, FontWeight.Normal, color = Color(0xFF8D929C))
        if (recents.isEmpty()) T("아직 등록한 문장이 없어요.\n마이크를 눌러 말하거나 표현을 등록해 보세요.", 34f, 508f, 320f, 11f, FontWeight.Normal, color = MalMuted, lineMul = 1.3f)
        recents.forEach { (_, text, y) ->
            T(text, 50f, y + 3f, 264f, 11f, FontWeight.Normal, color = MalInk)
            T("›", 343f, y + 2f, 12f, 13f, FontWeight.Normal, color = Color(0xFF96909C))
        }
        T("핵심표현 바로가기", 21f, 614f, 200f, 13f, FontWeight.Bold, color = MalInk)
        cats.forEach { T(it.label, it.x + 2f, 646f, 77f, 12f, FontWeight.Bold, color = it.color, align = TextAlign.Center) }
        T(if (streakDays > 0) "연속 말하기 ${streakDays}일째!" else "오늘 첫 말하기를 시작해요!", 95f, 763f, 260f, 13f, FontWeight.Bold, color = Color(0xFFF7519A))
        T(if (streakDays > 0) "오늘도 멋져요!" else "마이크를 눌러 시작해요.", 95f, 785f, 180f, 10f, FontWeight.Normal, color = Color(0xFF8F879C))

        // ── 05 hit areas ──
        Box(Modifier.box(343.82f, 24f, 32f, 34f).clickable(onClickLabel = "메뉴") { onMenu() }.semantics { contentDescription = "알림·설정 메뉴" })
        Box(Modifier.box(21.03f, 151.79f, 347.48f, 128.93f).clickable(onClickLabel = "말친구 리워드") { onReward() }.semantics { contentDescription = "말친구 리워드" })
        Box(
            Modifier.box(118f, 299f, 159f, 159f)
                .clickable(onClickLabel = "말하기") { onMic() }
                .semantics { contentDescription = "눌러서 말하기" },
        )
        recents.forEach { (_, text, y) ->
            Box(Modifier.box(28f, y - 11f, 332f, 26f).clickable(onClickLabel = "문장 사용") { onRecent(text) }.semantics { contentDescription = text })
        }
        cats.forEach { c ->
            Box(Modifier.box(c.x, 637f, 81f, 92f).clickable(onClickLabel = c.label) { onCategory(c.key) }.semantics { contentDescription = "${c.label} 표현" })
        }
    }
}
