package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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

/**
 * S05 · 핵심표현 등록(인트로) — 구현정본 node 86:159.
 * 4개 카테고리 미리보기. 등록 시작→템플릿 선택, 카드 탭→해당 카테고리로.
 */
@Composable
fun S05Register(onStart: () -> Unit, onLater: () -> Unit, onCategory: (Category) -> Unit) {
    FaithfulFrame {
        val card = RoundedCornerShape(d(22f))
        // 카드 4장
        Box(Modifier.box(22f, 351f, 168f, 167f).shadow(d(2f), card, clip = false).background(MalSurface, card))
        Box(Modifier.box(199f, 351f, 168f, 167f).shadow(d(2f), card, clip = false).background(MalSurface, card))
        Box(Modifier.box(22f, 523f, 168f, 167f).shadow(d(2f), card, clip = false).background(MalSurface, card))
        Box(Modifier.box(199f, 523f, 168f, 167f).shadow(d(2f), card, clip = false).background(MalSurface, card))
        // 버튼 배경
        Box(Modifier.box(22.4f, 708.22f, 344.74f, 50.29f).background(brandBrush(), RoundedCornerShape(d(24f))))
        val later = RoundedCornerShape(d(23.32f))
        Box(Modifier.box(22.86f, 770.4f, 343.82f, 46.64f).shadow(d(2f), later, clip = false).background(MalSurface, later).border(BorderStroke(d(1f), Color(0xFFE5DEED)), later))

        // 포즈(디자이너 PDF 캐릭터)
        artCrop(R.drawable.pose_s05_name, 32f, 386.8f, 81.383f, 111.559f)
        artCrop(R.drawable.pose_s05_place, 213.97f, 398.69f, 146.307f, 105.158f)
        artCrop(R.drawable.pose_s05_time, 33.83f, 573.34f, 144.478f, 102.872f)
        artCrop(R.drawable.pose_s05_often, 202.09f, 571.51f, 164.138f, 108.816f)

        // 로고 + 등록 아이콘
        art(R.drawable.brand_logo, 109.73f, 39.78f, 173.74f, 48.895f)

        // 이름표(회전) — 이름 카드
        Box(Modifier.box(103f, 415f, 80f, 68f), contentAlignment = Alignment.Center) {
            Box(
                Modifier.rotate(10f).requiredSize(d(72f), d(58f)).background(Color(0xFF88BBFF), RoundedCornerShape(d(4f))),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.requiredSize(d(61f), d(46f)).background(Color.White, RoundedCornerShape(d(2f))), contentAlignment = Alignment.Center) {
                    Text("지우", color = Color(0xFF111111), fontSize = fs(19.66f), fontWeight = FontWeight.Normal)
                }
            }
        }

        // 텍스트
        T("핵심표현을\n등록해요", 20f, 105f, 350f, 40f, FontWeight.Normal, brush = juaTitleBrush(), align = TextAlign.Center, family = JuaFont, lineMul = 1.1f)
        T("이름, 장소, 시간, 자주 쓰는 말을\n미리 등록하면 더 빠르고\n정확하게 전달할 수 있어요.", 28f, 212f, 334f, 14f, FontWeight.Normal, color = MalMuted, align = TextAlign.Center, lineMul = 1.45f)
        T("① ─ ② ─ ③ ─ ④ ─ ⑤", 75f, 287f, 250f, 19f, FontWeight.Normal, color = Color(0xFF9870E3), align = TextAlign.Center)
        T("1 / 5", 110f, 321f, 170f, 20f, FontWeight.Bold, color = Color(0xFF7546EB), align = TextAlign.Center)
        T("이름", 32f, 363f, 148f, 25f, FontWeight.Bold, color = Color(0xFF327EFF), align = TextAlign.Center)
        T("장소", 209f, 363f, 148f, 25f, FontWeight.Bold, color = Color(0xFF00BBA9), align = TextAlign.Center)
        T("시간", 32f, 535f, 148f, 25f, FontWeight.Bold, color = Color(0xFFFFB610), align = TextAlign.Center)
        T("자주 쓰는 말", 209f, 535f, 148f, 25f, FontWeight.Bold, color = Color(0xFFF65399), align = TextAlign.Center)
        T("등록 시작", 110f, 718.36f, 140f, 22f, FontWeight.Normal, color = Color.White, align = TextAlign.Center, family = JuaFont)
        T("나중에 할게요", 28.86f, 782.22f, 331.82f, 18f, FontWeight.Bold, color = Color(0xFF7546EB), align = TextAlign.Center)

        // 터치영역
        Box(Modifier.box(22.4f, 708.22f, 344.74f, 50.29f).clickable(onClickLabel = "등록 시작") { onStart() }.semantics { contentDescription = "등록 시작" })
        Box(Modifier.box(22.86f, 770.4f, 343.82f, 46.64f).clickable(onClickLabel = "나중에") { onLater() }.semantics { contentDescription = "나중에 할게요" })
        Box(Modifier.box(21.95f, 350.22f, 167.8f, 164.6f).clickable(onClickLabel = "이름") { onCategory(Category.NAME) }.semantics { contentDescription = "이름 표현 등록" })
        Box(Modifier.box(198.89f, 350.22f, 167.8f, 164.6f).clickable(onClickLabel = "장소") { onCategory(Category.PLACE) }.semantics { contentDescription = "장소 표현 등록" })
        Box(Modifier.box(21.95f, 522.59f, 167.8f, 167.34f).clickable(onClickLabel = "시간") { onCategory(Category.TIME) }.semantics { contentDescription = "시간 표현 등록" })
        Box(Modifier.box(198.89f, 522.59f, 167.8f, 167.34f).clickable(onClickLabel = "자주 쓰는 말") { onCategory(Category.OFTEN) }.semantics { contentDescription = "자주 쓰는 말 등록" })
    }
}
