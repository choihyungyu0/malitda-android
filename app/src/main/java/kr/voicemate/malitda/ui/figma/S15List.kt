package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import kr.voicemate.malitda.data.db.CorrectionEntity

/**
 * S15 · 교정 이력 — 구현정본 node 86:219. 승인한 교정(원래 말→교정된 말)을 실제 저장 데이터로 관리.
 */
@Composable
fun S15History(
    corrections: List<CorrectionEntity>,
    onDelete: (Long) -> Unit,
    onReset: () -> Unit,
    onHome: () -> Unit,
) {
    FaithfulFrame {
        // 배경
        Box(Modifier.box(23f, 208f, 344f, 109f).background(Color(0xFFF7F1FE), RoundedCornerShape(d(21f))))
        val resetShape = RoundedCornerShape(d(14.175f))
        Box(Modifier.box(276.61f, 34.75f, 92.81f, 28.35f).background(MalSurface, resetShape).border(BorderStroke(d(1f), Color(0xFFE5DEED)), resetShape))

        // 아트 + 아이콘
        artCrop(R.drawable.pose_s15_purple, 237.75f, 92.36f, 139.906f, 113.845f)
        art(R.drawable.brand_logo, 26.52f, 33.83f, 123.447f, 34.741f)
        Image(painterResource(R.drawable.ic_star), null, Modifier.box(45f, 240f, 35f, 35f))

        // 헤더 텍스트
        T("교정 이력", 24f, 99f, 310f, 42f, FontWeight.Normal, color = Color(0xFF102342), family = JuaFont, lineMul = 1.2f)
        T("내가 승인한 교정을 관리해요.", 26f, 158f, 308f, 15f, FontWeight.Normal, color = MalMuted)
        T("승인한 동일 오인식의 교정을\n다시 제안해 드려요.", 95f, 225f, 254f, 17f, FontWeight.Bold, color = Color(0xFF8A4CEB))
        T("유사성만으로 새 문장을 만들지 않아요.", 95f, 280f, 249f, 11f, FontWeight.Normal, color = MalMuted)
        T("교정 이력 전체 초기화", 282.6f, 40.42f, 80.81f, 9.5f, FontWeight.Bold, color = Color(0xFF7546EB), align = TextAlign.Center)
        T("교정 이력은 기기에 저장되며 비공개로 관리돼요.", 56f, 801f, 304f, 11f, FontWeight.Normal, color = Color(0xFF85808D))

        // 목록(실 교정 데이터)
        if (corrections.isEmpty()) {
            T("아직 승인한 교정이 없어요.\n말하기에서 문장을 수정·승인하면 여기 모여요.", 22f, 470f, 346f, 14f, FontWeight.Normal, color = MalMuted, align = TextAlign.Center)
        } else {
            LazyColumn(
                modifier = Modifier.box(22f, 330f, 346f, 460f),
                verticalArrangement = Arrangement.spacedBy(d(8f)),
            ) {
                items(corrections, key = { it.id }) { e -> CorrRow(e, onDelete) }
            }
        }

        // 터치영역
        Box(Modifier.box(276.61f, 34.75f, 92.81f, 28.35f).clickable(onClickLabel = "초기화") { onReset() }.semantics { contentDescription = "교정 이력 전체 초기화" })
        Box(Modifier.box(26.98f, 30.63f, 121.62f, 38.41f).clickable(onClickLabel = "홈") { onHome() }.semantics { contentDescription = "홈으로" })
    }
}

@Composable
private fun FrameScope.CorrRow(e: CorrectionEntity, onDelete: (Long) -> Unit) {
    val shape = RoundedCornerShape(d(22f))
    Box(Modifier.fillMaxWidth().requiredHeight(d(88f)).shadow(d(2f), shape, clip = false).clip(shape).background(MalSurface)) {
        T("원래 말", 18f, 14f, 127f, 11f, FontWeight.Bold, color = Color(0xFF8352EF))
        T(e.sourceRaw, 18f, 36f, 122f, 15f, FontWeight.Normal, color = MalInk)
        Image(painterResource(R.drawable.ic_arrow), null, Modifier.box(126f, 40f, 25f, 25f))
        T("교정된 말", 164f, 14f, 132f, 11f, FontWeight.Bold, color = Color(0xFF00BAA6))
        T(e.approvedText, 163f, 36f, 130f, 14f, FontWeight.Normal, color = MalInk)
        T(relTime(e.lastUsedAt), 300f, 14f, 40f, 10f, FontWeight.Normal, color = Color(0xFF97909C))
        Image(
            painterResource(R.drawable.ic_s15_delete), null,
            Modifier.box(298f, 40f, 34f, 34f).clickable(onClickLabel = "삭제") { onDelete(e.id) }.semantics { contentDescription = "이 교정 삭제" },
        )
    }
}
