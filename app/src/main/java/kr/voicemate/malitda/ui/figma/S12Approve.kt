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

private val QUOTE = Color(0xFFD9C4FB)
private val PURPLE = Color(0xFF7546EB)

/**
 * S12 승인 전 / S21 승인 완료 — 구현정본 node 86:201 / 86:255.
 * '문장을 확인했어요' 체크(approve)로 공유 잠금 해제. 같은 화면의 잠김/해제 상태.
 */
@Composable
fun S12Approve(
    sentence: String,
    approved: Boolean,
    onToggleApprove: () -> Unit,
    onEdit: () -> Unit,
    onShareSystem: () -> Unit,
    onShareCopy: () -> Unit,
    onShareMessage: () -> Unit,
    onHome: () -> Unit,
) {
    FaithfulFrame {
        // ── 공통 ──
        Box(Modifier.box(23f, 190f, 344f, 187f).background(Color(0xFFF7F2FF), RoundedCornerShape(d(22f))))
        val card = RoundedCornerShape(d(22f))
        Box(Modifier.box(23f, 392f, 344f, 76f).shadow(d(2f), card, clip = false).background(MalSurface, card))
        art(R.drawable.art_purple, 201f, 474f, 164f)
        art(R.drawable.brand_logo, 126f, 24f, 152f, 43f)
        Image(painterResource(if (approved) R.drawable.ic_check_circle else R.drawable.ic_radio_off), null, Modifier.box(43f, 413f, 35f, 35f))

        T("이 문장이 맞아요", 24f, 86f, 342f, 30f, FontWeight.Black, brush = brandBrush(), align = TextAlign.Center)
        T("“", 38f, 190f, 44f, 32f, FontWeight.Bold, color = QUOTE)
        T("”", 317f, 329f, 40f, 32f, FontWeight.Bold, color = QUOTE)
        T(sentence, 42.5f, 243f, 305f, 28f, FontWeight.Bold, color = MalInk, align = TextAlign.Center)
        T("문장을 확인했어요", 101f, 416f, 250f, 18f, FontWeight.Bold, color = MalInk)
        T("내가 확인한 문장을\n다른 앱으로 전달해요.", 34f, 560f, 180f, 12f, FontWeight.Normal, color = Color(0xFF8A8596))

        if (!approved) {
            // ── 잠김(S12) ──
            Box(Modifier.box(23f, 622f, 344f, 76f).background(Color(0xFFF1EAFB), RoundedCornerShape(d(20f))))
            Box(Modifier.box(23f, 710f, 344f, 63f).background(Color(0xFFD9CDEB), RoundedCornerShape(d(24f))))
            T("확인하면 공유할 수 있어요", 34f, 532f, 190f, 14f, FontWeight.Bold, color = PURPLE)
            T("먼저 문장을 확인해 주세요", 40f, 648f, 310f, 14f, FontWeight.Bold, color = Color(0xFF92849F), align = TextAlign.Center)
            T("공유하기 · 잠김", 38f, 730f, 313f, 20f, FontWeight.Bold, color = Color(0xFF8C7E99), align = TextAlign.Center)
            T("홈으로", 140f, 790f, 110f, 15f, FontWeight.Normal, color = Color(0xFF8C8796), align = TextAlign.Center)
            Box(Modifier.box(137.16f, 780f, 117.05f, 38.41f).clickable(onClickLabel = "홈으로") { onHome() }.semantics { contentDescription = "홈으로" })
        } else {
            // ── 해제(S21) ──
            val sb = RoundedCornerShape(d(24f))
            Box(Modifier.box(22.86f, 622.72f, 106.99f, 75.44f).background(MalSurface, sb).border(BorderStroke(d(1f), Color(0xFFE5DEED)), sb))
            Box(Modifier.box(140.36f, 622.72f, 107.44f, 75.44f).background(MalSurface, sb).border(BorderStroke(d(1f), Color(0xFFE5DEED)), sb))
            Box(Modifier.box(257.41f, 622.72f, 109.73f, 75.44f).background(MalSurface, sb).border(BorderStroke(d(1f), Color(0xFFE5DEED)), sb))
            Box(Modifier.box(22.86f, 710.05f, 343.82f, 61.72f).background(brandBrush(), RoundedCornerShape(d(24f))))
            val hb = RoundedCornerShape(d(19.205f))
            Box(Modifier.box(137.16f, 780f, 117.05f, 38.41f).background(MalSurface, hb).border(BorderStroke(d(1f), Color(0xFFE5DEED)), hb))

            T("공유가 활성화되었어요!", 34f, 532f, 220f, 14f, FontWeight.Bold, color = PURPLE)
            T("시스템 공유", 28.85f, 648.94f, 94.99f, 18f, FontWeight.Bold, color = PURPLE, align = TextAlign.Center)
            T("텍스트 복사", 146.36f, 648.94f, 95.44f, 18f, FontWeight.Bold, color = PURPLE, align = TextAlign.Center)
            T("메시지 공유", 263.41f, 648.94f, 97.73f, 18f, FontWeight.Bold, color = PURPLE, align = TextAlign.Center)
            T("공유하기", 28.9f, 729.41f, 331.82f, 18f, FontWeight.Bold, color = Color.White, align = TextAlign.Center)
            T("홈으로", 143f, 787.71f, 105.05f, 18f, FontWeight.Bold, color = PURPLE, align = TextAlign.Center)

            Box(Modifier.box(22.86f, 622.72f, 106.99f, 75.44f).clickable(onClickLabel = "시스템 공유") { onShareSystem() }.semantics { contentDescription = "시스템 공유" })
            Box(Modifier.box(140.36f, 622.72f, 107.44f, 75.44f).clickable(onClickLabel = "텍스트 복사") { onShareCopy() }.semantics { contentDescription = "텍스트 복사" })
            Box(Modifier.box(257.41f, 622.72f, 109.73f, 75.44f).clickable(onClickLabel = "메시지 공유") { onShareMessage() }.semantics { contentDescription = "메시지 공유" })
            Box(Modifier.box(22.86f, 710.05f, 343.82f, 61.72f).clickable(onClickLabel = "공유하기") { onShareSystem() }.semantics { contentDescription = "공유하기" })
            Box(Modifier.box(137.16f, 780f, 117.05f, 38.41f).clickable(onClickLabel = "홈으로") { onHome() }.semantics { contentDescription = "홈으로" })
        }

        // 승인 토글(공통, 승인 카드 영역)
        Box(Modifier.box(22.86f, 391.37f, 344.28f, 75.9f).clickable(onClickLabel = "문장 확인") { onToggleApprove() }.semantics { contentDescription = "문장을 확인했어요" })
        // 문장 카드 탭 = 다시 수정
        Box(Modifier.box(22.86f, 189.74f, 344.28f, 90f).clickable(onClickLabel = "문장 수정") { onEdit() }.semantics { contentDescription = "문장 다시 수정" })
    }
}
