package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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

private data class Miss(val pose: Int, val title: String, val desc: String, val icon: Int, val bar: Color, val value: Int, val target: Int, val top: Float, val px: Float, val py: Float, val pw: Float, val ph: Float)

/**
 * S19 · 말친구 리워드 — 구현정본 node 86:243. 실제 카운터 기반 미션 진행·다음 친구 안내.
 */
@Composable
fun S19Reward(approvals: Int, expressionsAdded: Int, streakDays: Int, onCatalog: () -> Unit, onMenu: () -> Unit) {
    val cycle = approvals % 5
    val rem = 5 - cycle
    val missions = listOf(
        Miss(R.drawable.pose_s19_water, "표현 등록", "새로운 표현을 등록해요.", R.drawable.ic_s19_edit, Color(0xFF2390FF), expressionsAdded, 5, 333f, 28.35f, 336.05f, 64.924f, 76.811f),
        Miss(R.drawable.pose_s19_yellow, "문장 승인", "내 문장을 직접 승인해요.", R.drawable.ic_s19_check, Color(0xFFFFB610), approvals, 10, 424f, 26.98f, 433.43f, 74.525f, 66.753f),
        Miss(R.drawable.pose_s19_green, "연속 사용", "매일 꾸준히 사용해요.", R.drawable.ic_s19_calendar, Color(0xFF00CB8C), streakDays, 7, 515f, 25.6f, 523.05f, 79.555f, 74.068f),
    )
    FaithfulFrame {
        // 배경
        Box(Modifier.box(20f, 164f, 350f, 125f).background(Color(0xFFF3EDFC), RoundedCornerShape(d(20f))))
        Box(Modifier.box(35f, 255f, 128f, 9f).background(Color(0xFFE4D7FB), RoundedCornerShape(d(4.5f))))
        Box(Modifier.box(35f, 255f, 128f * (cycle / 5f), 9f).background(brandBrush(), RoundedCornerShape(d(4.5f))))
        val card = RoundedCornerShape(d(22f))
        missions.forEach { m ->
            Box(Modifier.box(20f, m.top, 350f, 85f).shadow(d(2f), card, clip = false).background(MalSurface, card))
            Box(Modifier.box(112f, m.top + 67f, 136f, 6f).background(Color(0xFFEEE8FB), RoundedCornerShape(d(3f))))
            Box(Modifier.box(112f, m.top + 67f, 136f * (m.value.coerceAtMost(m.target).toFloat() / m.target), 6f).background(m.bar, RoundedCornerShape(d(3f))))
        }
        Box(Modifier.box(20f, 618f, 350f, 121f).background(Color(0xFFF6EEFF), RoundedCornerShape(d(21f))))
        Box(Modifier.box(47.55f, 753.94f, 294.9f, 41.15f).background(brandBrush(), RoundedCornerShape(d(20.575f))))

        // 아트
        artCrop(R.drawable.pose_s19_purple, 241.41f, 72.7f, 139.906f, 90.985f)
        artCrop(R.drawable.pose_s19_mystery, 196.6f, 172.83f, 166.882f, 111.559f)
        artCrop(R.drawable.pose_s19_robot, 190.2f, 617.69f, 172.368f, 114.76f)
        missions.forEach { m ->
            artCrop(m.pose, m.px, m.py, m.pw, m.ph)
            artCrop(m.icon, 299.47f, m.top + 12.65f, 57.151f, 57.151f)
        }
        art(R.drawable.brand_logo, 18.29f, 23.32f, 107.444f, 30.238f)
        Image(painterResource(R.drawable.ic_bell), null, Modifier.box(341.99f, 21.03f, 31.09f, 31.09f))

        // 텍스트
        T("말친구 리워드", 24f, 78f, 310f, 35f, FontWeight.Normal, brush = juaTitleBrush(), family = JuaFont, lineMul = 1.1f)
        T("미션을 완료하고 새로운 말친구를 만나보세요!", 23f, 124f, 320f, 12f, FontWeight.Normal, color = MalInk)
        T("다음 말친구까지\n${rem}번 남았어요!", 34f, 183f, 246f, 22f, FontWeight.Bold, color = Color(0xFF7546EB))
        T("$cycle / 5", 171f, 248f, 58f, 13f, FontWeight.Normal, color = MalInk)
        T("리워드 미션", 23f, 308f, 219f, 17f, FontWeight.Bold, color = MalInk)
        missions.forEach { m ->
            T(m.title, 111f, m.top + 12f, 198f, 20f, FontWeight.Bold, color = MalInk)
            T(m.desc, 111f, m.top + 43f, 181f, 11f, FontWeight.Normal, color = MalMuted)
            T("${m.value.coerceAtMost(m.target)} / ${m.target}", 261f, m.top + 60f, 40f, 11f, FontWeight.Normal, color = MalInk)
        }
        T("다음 말친구\n로보잇", 39f, 628f, 210f, 22f, FontWeight.Bold, color = Color(0xFF7546EB))
        T("말을 잘 듣고 차분하게\n도와주는 똑똑한 로봇 친구예요.", 40f, 706f, 225f, 10f, FontWeight.Normal, color = MalInk)
        T("리워드 안내", 140f, 812f, 162f, 14f, FontWeight.Normal, color = Color(0xFF7C7788))
        T("말친구 도감 보기", 53.5f, 763.02f, 282.9f, 18f, FontWeight.Bold, color = Color.White, align = TextAlign.Center)

        // 터치영역
        Box(Modifier.box(341.99f, 21.03f, 31.09f, 33.83f).clickable { onMenu() }.semantics { contentDescription = "앱 메뉴" })
        Box(Modifier.box(47.55f, 753.94f, 294.9f, 41.15f).clickable(onClickLabel = "도감") { onCatalog() }.semantics { contentDescription = "말친구 도감 보기" })
    }
}
