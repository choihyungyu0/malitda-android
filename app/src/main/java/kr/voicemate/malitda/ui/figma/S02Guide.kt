package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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

private val PURPLE = Color(0xFF7546EB)

/**
 * S02 · 서비스 안내 — 구현정본 node 86:141.
 * 3단계(말해요/고를게요/전달해요) 안내 + 다음/건너뛰기.
 */
@Composable
fun S02Guide(onNext: () -> Unit, onSkip: () -> Unit) {
    FaithfulFrame {
        val card = RoundedCornerShape(d(22f))
        val circle = RoundedCornerShape(d(16f))
        // 안내 카드 3장 + 단계 원
        val cardTops = listOf(245f, 396f, 547f)
        val circleTops = listOf(268f, 419f, 570f)
        cardTops.forEach { t ->
            Box(Modifier.box(22f, t, 346f, 142f).shadow(d(2f), card, clip = false).background(MalSurface, card))
        }
        circleTops.forEach { t ->
            Box(Modifier.box(36f, t, 32f, 32f).background(brandBrush(), circle))
        }
        // 버튼 배경
        Box(Modifier.box(27.43f, 704.56f, 336.05f, 58.52f).background(brandBrush(), RoundedCornerShape(d(24f))))
        val skipShape = RoundedCornerShape(d(17.83f))
        Box(Modifier.box(139.91f, 779.54f, 115.22f, 35.66f).background(MalSurface, skipShape).border(BorderStroke(d(1f), Color(0xFFE5DEED)), skipShape))

        // 아트 3종
        art(R.drawable.art_robot_mint, 245f, 265f, 102f)
        art(R.drawable.art_water, 245f, 416f, 102f)
        art(R.drawable.art_robot_pink, 245f, 567f, 102f)

        // 브랜드 로고 + 워드마크 + 헤더
        art(R.drawable.brand_logo, 126f, 24f, 152f, 43f)
        T("내 말을 더\n정확하게 전달해요", 24f, 86f, 342f, 30f, FontWeight.Black, brush = brandBrush(), align = TextAlign.Center)
        T("듣고, 고르고, 전달하는 쉬운 말 도우미", 28f, 175f, 334f, 13f, FontWeight.Normal, color = MalMuted, align = TextAlign.Center)

        // 단계 번호
        T("1", 36f, 271f, 32f, 19.2f, FontWeight.Bold, color = Color.White, align = TextAlign.Center)
        T("2", 36f, 422f, 32f, 19.2f, FontWeight.Bold, color = Color.White, align = TextAlign.Center)
        T("3", 36f, 573f, 32f, 19.2f, FontWeight.Bold, color = Color.White, align = TextAlign.Center)
        // 단계 제목
        T("말해요", 81f, 263f, 160f, 22f, FontWeight.Bold, color = PURPLE)
        T("고를게요", 81f, 414f, 160f, 22f, FontWeight.Bold, color = PURPLE)
        T("전달해요", 81f, 565f, 160f, 22f, FontWeight.Bold, color = PURPLE)
        // 단계 설명
        T("마이크를 누르고\n하고 싶은 말을 들려주세요.", 80f, 304f, 180f, 13f, FontWeight.Normal, color = MalInk)
        T("인식된 원문과 후보를 보고\n직접 골라 주세요.", 80f, 455f, 180f, 13f, FontWeight.Normal, color = MalInk)
        T("내가 확인하고 승인한 말을\n다른 앱에 전달해요.", 80f, 606f, 180f, 13f, FontWeight.Normal, color = MalInk)

        // 버튼 캡션
        T("다음", 33.4f, 722.32f, 324.05f, 18f, FontWeight.Bold, color = Color.White, align = TextAlign.Center)
        T("건너뛰기", 145.91f, 785.87f, 103.22f, 18f, FontWeight.Bold, color = PURPLE, align = TextAlign.Center)

        // 터치영역
        Box(
            Modifier.box(27.43f, 704.56f, 336.05f, 58.52f)
                .clickable(onClickLabel = "다음") { onNext() }
                .semantics { contentDescription = "다음" },
        )
        Box(
            Modifier.box(139.91f, 779.54f, 115.22f, 35.66f)
                .clickable(onClickLabel = "건너뛰기") { onSkip() }
                .semantics { contentDescription = "건너뛰기" },
        )
    }
}
