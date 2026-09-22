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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kr.voicemate.malitda.R
import kr.voicemate.malitda.domain.Category

private data class TplRow(
    val cat: Category, val name: String, val color: Color, val desc: String,
    val icon: Int, val pose: Int, val top: Float,
    val poseX: Float, val poseY: Float, val poseW: Float, val poseH: Float,
)

private val S06_ROWS = listOf(
    TplRow(Category.NAME, "사람 이름", Color(0xFF8454EF), "가족, 친구, 선생님 등\n사람 이름을 등록해요.", R.drawable.ic_person, R.drawable.pose_s06_0, 256f, 212.6f, 258.32f, 112.931f, 82.755f),
    TplRow(Category.PLACE, "장소", Color(0xFF00BDAA), "집, 학교, 병원, 놀이터 등\n자주 가는 장소를 등록해요.", R.drawable.ic_pin, R.drawable.pose_s06_1, 348.3f, 230.89f, 345.19f, 125.275f, 89.156f),
    TplRow(Category.TIME, "시간", Color(0xFFFFAF16), "지금, 아침, 주말, 방학 등\n시간 관련 표현을 등록해요.", R.drawable.ic_clock, R.drawable.pose_s06_2, 440.6f, 240.49f, 450.81f, 114.302f, 75.44f),
    TplRow(Category.MESSAGE, "메시지", Color(0xFFF74C94), "상황별로 필요한 짧은 메시지나\n말을 등록해요.", R.drawable.ic_chat, R.drawable.pose_s06_3, 532.9f, 230.43f, 539.96f, 114.302f, 79.097f),
    TplRow(Category.OFTEN, "자주 쓰는 문장", Color(0xFF8D51FA), "일상에서 자주 쓰는 문장을\n통째로 등록해요.", R.drawable.ic_star, R.drawable.pose_s06_4, 625.2f, 222.66f, 638.26f, 132.591f, 77.726f),
)

/**
 * S06 · 표현 템플릿 선택 — 구현정본 node 86:165.
 * 5개 표현 유형 중 하나 선택(라디오). 다음→폼(선택 카테고리), 직접 만들기→빈 폼.
 */
@Composable
fun S06Template(
    selected: Category,
    onSelect: (Category) -> Unit,
    onNext: () -> Unit,
    onDirect: () -> Unit,
) {
    FaithfulFrame {
        val card = RoundedCornerShape(d(22f))
        // 카드 5장 + 선택 테두리
        S06_ROWS.forEach { r ->
            val sel = selected == r.cat
            Box(
                Modifier.box(24f, r.top, 342f, 85f).shadow(d(2f), card, clip = false).background(MalSurface, card)
                    .then(if (sel) Modifier.border(BorderStroke(d(1.5f), Color(0xFF7546EB)), card) else Modifier),
            )
        }
        // 검색 입력(표시)
        val searchShape = RoundedCornerShape(d(14f))
        Box(Modifier.box(81.38f, 209.86f, 225.4f, 30.63f).background(MalSurface, searchShape).border(BorderStroke(d(1f), Color(0xFFDED9E5)), searchShape))
        // 버튼
        Box(Modifier.box(33.83f, 729.71f, 321.42f, 49.38f).background(brandBrush(), RoundedCornerShape(d(24f))))
        val direct = RoundedCornerShape(d(19.43f))
        Box(Modifier.box(119.79f, 785.94f, 149.51f, 38.86f).background(MalSurface, direct).border(BorderStroke(d(1f), Color(0xFFE5DEED)), direct))

        // 포즈 + 아이콘
        S06_ROWS.forEach { r ->
            artCrop(r.pose, r.poseX, r.poseY, r.poseW, r.poseH)
            Image(painterResource(r.icon), null, Modifier.box(35f, r.top + 24f, 34f, 34f))
        }
        art(R.drawable.brand_logo, 126.65f, 32.92f, 142.192f, 40.017f)

        // 텍스트
        T("표현 템플릿을\n선택해요", 20f, 84f, 350f, 38f, FontWeight.Normal, brush = juaTitleBrush(), align = TextAlign.Center, family = JuaFont, lineMul = 1.1f)
        T("먼저 등록할 표현 유형을 골라 주세요.", 28f, 179f, 334f, 14f, FontWeight.Normal, color = MalMuted, align = TextAlign.Center)
        T("어떤 표현을 등록할까요?", 91.38f, 214.86f, 197.4f, 14f, FontWeight.Normal, color = Color(0xFF8E929C))
        S06_ROWS.forEach { r ->
            T(r.name, 95f, r.top + 12f, 215f, 18f, FontWeight.Bold, color = r.color)
            T(r.desc, 95f, r.top + 40f, 165f, 11f, FontWeight.Normal, color = MalMuted)
        }
        T("다음", 100f, 739.4f, 160f, 22f, FontWeight.Normal, color = Color.White, align = TextAlign.Center, family = JuaFont)
        T("직접 만들기", 125.79f, 793.87f, 137.51f, 18f, FontWeight.Bold, color = Color(0xFF7546EB), align = TextAlign.Center)

        // 터치영역
        S06_ROWS.forEach { r ->
            Box(Modifier.box(24.23f, r.top - 0.42f, 341.08f, 84.58f).clickable(onClickLabel = r.name) { onSelect(r.cat) }.semantics { contentDescription = "${r.name} 선택" })
        }
        Box(Modifier.box(33.83f, 729.71f, 321.42f, 49.38f).clickable(onClickLabel = "다음") { onNext() }.semantics { contentDescription = "다음" })
        Box(Modifier.box(119.79f, 785.94f, 149.51f, 38.86f).clickable(onClickLabel = "직접 만들기") { onDirect() }.semantics { contentDescription = "직접 만들기" })
    }
}
