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
import kr.voicemate.malitda.R

private data class SetRow(val icon: Int, val title: String, val desc: String, val hitTop: Float, val iconTop: Float, val action: () -> Unit)

/**
 * S18 · 설정·도움말 — 구현정본 node 86:237. 설정/정보 항목 허브.
 */
@Composable
fun S18Settings(
    onAccess: () -> Unit,
    onTts: () -> Unit,
    onDeleteData: () -> Unit,
    onPrivacy: () -> Unit,
    onHelp: () -> Unit,
    onContact: () -> Unit,
    onMenu: () -> Unit,
) {
    val rows = listOf(
        SetRow(R.drawable.ic_s18_access, "접근성 설정", "앱의 접근성 설정을 조정해요.", 241.86f, 256.04f, onAccess),
        SetRow(R.drawable.ic_s18_tts, "TTS 설정", "음성 읽기 설정을 조정해요.", 311.82f, 324.16f, onTts),
        SetRow(R.drawable.ic_s18_delete, "데이터 삭제", "앱에 저장된 데이터를 삭제해요.", 381.77f, 394.57f, onDeleteData),
        SetRow(R.drawable.ic_s18_privacy, "개인정보 처리방침", "말잇다의 개인정보 안내를 확인해요.", 506.13f, 518.93f, onPrivacy),
        SetRow(R.drawable.ic_s18_guide, "서비스 사용법", "주요 기능과 사용 방법을 안내해요.", 575.17f, 588.43f, onHelp),
        SetRow(R.drawable.ic_s18_help, "문의하기", "궁금한 점은 도움말에서 확인해요.", 645.12f, 658.38f, onContact),
    )
    FaithfulFrame {
        val card = RoundedCornerShape(d(22f))
        Box(Modifier.box(22f, 243f, 346f, 211f).shadow(d(2f), card, clip = false).background(MalSurface, card))
        Box(Modifier.box(22f, 508f, 346f, 207f).shadow(d(2f), card, clip = false).background(MalSurface, card))
        Box(Modifier.box(22f, 734f, 346f, 88f).shadow(d(2f), card, clip = false).background(MalSurface, card))

        // 아트 + 로고
        artCrop(R.drawable.pose_s18_purple, 245.52f, 104.24f, 136.249f, 105.615f)
        artCrop(R.drawable.pose_s18_robot, 251.47f, 737.48f, 108.816f, 82.755f)
        art(R.drawable.brand_logo, 18.75f, 24.69f, 107.444f, 30.238f)
        art(R.drawable.brand_logo, 35.66f, 744.79f, 100.586f, 28.308f)
        Image(painterResource(R.drawable.ic_bell), null, Modifier.box(341.99f, 21.03f, 31.09f, 31.09f))

        // 헤더
        T("설정 · 도움말", 24f, 99f, 310f, 38f, FontWeight.Normal, color = Color(0xFF102342), family = JuaFont, lineMul = 1.2f)
        T("앱 사용 환경을 설정하고,\n도움이 필요할 때 확인해 보세요.", 23f, 153f, 286f, 14f, FontWeight.Normal, color = MalMuted)
        T("설정", 23f, 216f, 242f, 18f, FontWeight.Bold, color = MalInk)
        T("도움말 & 정보", 22f, 481f, 292f, 18f, FontWeight.Bold, color = MalInk)
        T("버전 0.1.1", 37f, 789f, 223f, 12f, FontWeight.Normal, color = Color(0xFF8B8795))

        // 행
        rows.forEach { r ->
            artCrop(r.icon, 33.83f, r.iconTop, 42.521f, 42.978f)
            T(r.title, 90f, r.hitTop + 21f, 240f, 18f, FontWeight.Bold, color = MalInk)
            T(r.desc, 90f, r.hitTop + 53f, 252f, 11f, FontWeight.Normal, color = Color(0xFF7A7788))
            T("›", 340f, r.hitTop + 30f, 17f, 17f, FontWeight.Normal, color = Color(0xFF9EA0AA))
        }

        // 터치영역
        Box(Modifier.box(341.99f, 21.03f, 31.09f, 33.83f).clickable { onMenu() }.semantics { contentDescription = "앱 메뉴" })
        rows.forEach { r ->
            Box(Modifier.box(21.03f, r.hitTop, 347.48f, 68.58f).clickable(onClickLabel = r.title) { r.action() }.semantics { contentDescription = "${r.title}: ${r.desc}" })
        }
    }
}
