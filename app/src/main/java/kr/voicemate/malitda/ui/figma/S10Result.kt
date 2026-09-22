package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kr.voicemate.malitda.R

/**
 * S10 · 인식 결과 — 구현정본 node 86:189.
 * 원문(불변)과 실제 인식 후보를 보여주고 선택. 후보는 인식기 실제 N-best + 저장한 동일 오인식 교정.
 */
@Composable
fun S10Result(
    raw: String,
    candidates: List<String>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    onListen: (String) -> Unit,
    onListenOriginal: () -> Unit,
    onNext: () -> Unit,
    onEdit: () -> Unit,
) {
    val cardTops = listOf(427.03f, 497.44f, 567.85f)
    FaithfulFrame {
        // ── 01 backgrounds ──
        val bigCard = RoundedCornerShape(d(22f))
        Box(Modifier.box(29f, 249f, 331f, 131f).shadow(d(2f), bigCard, clip = false).background(MalSurface, bigCard))
        val candShape = RoundedCornerShape(d(14f))
        candidates.take(3).forEachIndexed { i, _ ->
            val sel = selectedIndex == i
            Box(
                Modifier.box(29f, cardTops[i], 328f, 60f)
                    .shadow(d(2f), candShape, clip = false)
                    .background(MalSurface, candShape)
                    .border(BorderStroke(if (sel) d(1.5f) else d(1f), if (sel) Color(0xFF8C4DF2) else Color(0xFFE5E0ED)), candShape),
            )
        }
        Box(Modifier.box(24f, 642f, 342f, 54f).background(Color(0xFFF4EEFF), RoundedCornerShape(d(16f))))
        Box(Modifier.box(29.72f, 717.36f, 330.56f, 53.95f).background(brandBrush(), RoundedCornerShape(d(24f))))
        val editShape = RoundedCornerShape(d(21.49f))
        Box(Modifier.box(138.08f, 783.2f, 117.05f, 42.98f).background(MalSurface, editShape).border(BorderStroke(d(1f), Color(0xFFE5DEED)), editShape))

        // ── 02 art ──
        art(R.drawable.art_purple, 89f, 171f, 108f)
        art(R.drawable.art_robot_mint, 201f, 164f, 109f)

        // ── 03 icons ──
        art(R.drawable.brand_logo, 126f, 24f, 152f, 43f)
        Image(painterResource(R.drawable.ic_listen_big), null, Modifier.box(303.07f, 298.1f, 44f, 44f))
        candidates.take(3).forEachIndexed { i, _ ->
            Image(painterResource(R.drawable.ic_listen), null, Modifier.box(303f, cardTops[i] + 19f, 24f, 24f))
        }

        // ── 04 text ──
        T("이렇게 인식했어요", 24f, 86f, 342f, 30f, FontWeight.Black, brush = brandBrush(), align = TextAlign.Center)
        T("가장 잘 맞는 표현을 선택해 주세요.", 28f, 146f, 334f, 13f, FontWeight.Normal, color = MalMuted, align = TextAlign.Center)
        T("원문 인식", 56f, 278f, 214f, 16f, FontWeight.Bold, color = Color(0xFF2D7FFF))
        T(raw, 51f, 312f, 245f, 22f, FontWeight.Bold, color = MalInk)
        T("추천 후보", 49f, 400f, 251f, 15f, FontWeight.Bold, color = Color(0xFF3A7DFF))
        candidates.take(3).forEachIndexed { i, c ->
            T(c, 70f, cardTops[i] + 19f, 222f, 14f, FontWeight.Normal, color = MalInk)
        }
        T("인식기의 실제 후보와\n저장한 동일 오인식 교정만 보여요.", 70f, 654f, 280f, 12f, FontWeight.Normal, color = MalMuted)
        T("이 문장으로 할게요", 36f, 732.84f, 318.56f, 18f, FontWeight.Bold, color = Color.White, align = TextAlign.Center)
        T("직접 수정", 144f, 793.19f, 105.05f, 18f, FontWeight.Bold, color = Color(0xFF7546EB), align = TextAlign.Center)

        // 라디오 점(선택 표시)
        candidates.take(3).forEachIndexed { i, _ ->
            val sel = selectedIndex == i
            Box(
                Modifier.box(44f, cardTops[i] + 19f, 22f, 22f).clip(CircleShape)
                    .then(if (sel) Modifier.background(brandBrush()) else Modifier.border(BorderStroke(d(2f), Color(0xFFC9B8F2)), CircleShape)),
            )
        }

        // ── 05 hit areas ──
        Box(Modifier.box(300.39f, 286.67f, 47.09f, 47.55f).clickable(onClickLabel = "원문 듣기") { onListenOriginal() }.semantics { contentDescription = "원문 듣기" })
        candidates.take(3).forEachIndexed { i, c ->
            Box(Modifier.box(29.26f, cardTops[i], 265.64f, 60.35f).clickable(onClickLabel = "후보 선택") { onSelect(i) }.semantics { contentDescription = "후보 ${i + 1}: $c" })
            Box(Modifier.box(301.76f, cardTops[i] + 12f, 39.32f, 40.23f).clickable(onClickLabel = "듣기") { onListen(c) }.semantics { contentDescription = "후보 ${i + 1} 듣기" })
        }
        Box(Modifier.box(29.72f, 717.36f, 330.56f, 53.95f).clickable(onClickLabel = "진행") { onNext() }.semantics { contentDescription = "이 문장으로 할게요" })
        Box(Modifier.box(138.08f, 783.2f, 117.05f, 42.98f).clickable(onClickLabel = "직접 수정") { onEdit() }.semantics { contentDescription = "직접 수정" })
    }
}
