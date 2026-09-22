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

private val PURPLE = Color(0xFF7546EB)

/**
 * S17 · 인식 결과 없음 — 구현정본 node 86:231.
 * 무음/무결과 시. 다시 말하기·직접 수정·등록문장 선택.
 */
@Composable
fun S17NoResult(
    onAgain: () -> Unit,
    onEdit: () -> Unit,
    onRegistered: () -> Unit,
    onBack: () -> Unit,
) {
    FaithfulFrame {
        // 버튼 배경
        Box(Modifier.box(37.49f, 546.82f, 314.56f, 60.81f).background(brandBrush(), RoundedCornerShape(d(24f))))
        val ob = RoundedCornerShape(d(24f))
        Box(Modifier.box(37.95f, 619.52f, 314.1f, 59.89f).shadow(d(2f), ob, clip = false).background(MalSurface, ob).border(BorderStroke(d(1f), Color(0xFFE5DEED)), ob))
        Box(Modifier.box(37.49f, 690.39f, 314.56f, 60.35f).shadow(d(2f), ob, clip = false).background(MalSurface, ob).border(BorderStroke(d(1f), Color(0xFFE5DEED)), ob))

        // 일러스트 + 로고 + 아이콘
        art(R.drawable.art_s17, 81.38f, 245.52f, 274.326f, 276.612f)
        art(R.drawable.brand_logo, 127.1f, 48.46f, 138.077f, 38.859f)
        Image(painterResource(R.drawable.ic_bell), null, Modifier.box(337.88f, 42.98f, 36.12f, 36.12f))

        // 텍스트
        T("잘 들리지 않았어요.", 20f, 118f, 350f, 37f, FontWeight.Bold, color = Color(0xFF102342), align = TextAlign.Center)
        T("음성인식 결과가 없어요.\n다시 말하거나 다른 방법을 시도해 주세요.", 27f, 173f, 336f, 14f, FontWeight.Normal, color = MalMuted, align = TextAlign.Center)
        T("다시 말하기", 44f, 565.72f, 302.56f, 18f, FontWeight.Bold, color = Color.White, align = TextAlign.Center)
        T("직접 수정", 44f, 637.97f, 302.1f, 18f, FontWeight.Bold, color = PURPLE, align = TextAlign.Center)
        T("등록문장 선택", 44f, 709.07f, 302.56f, 18f, FontWeight.Bold, color = PURPLE, align = TextAlign.Center)
        T("이전 단계로 돌아가기", 128f, 787f, 220f, 15f, FontWeight.Normal, color = Color(0xFF827B8D))

        // 터치영역
        Box(Modifier.box(37.49f, 546.82f, 314.56f, 60.81f).clickable(onClickLabel = "다시 말하기") { onAgain() }.semantics { contentDescription = "다시 말하기" })
        Box(Modifier.box(37.95f, 619.52f, 314.1f, 59.89f).clickable(onClickLabel = "직접 수정") { onEdit() }.semantics { contentDescription = "직접 수정" })
        Box(Modifier.box(37.49f, 690.39f, 314.56f, 60.35f).clickable(onClickLabel = "등록문장 선택") { onRegistered() }.semantics { contentDescription = "등록문장 선택" })
        Box(Modifier.box(114.3f, 768.11f, 186.08f, 47.09f).clickable(onClickLabel = "이전 단계로") { onBack() }.semantics { contentDescription = "이전 단계로 돌아가기" })
    }
}
