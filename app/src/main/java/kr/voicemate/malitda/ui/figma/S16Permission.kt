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

/**
 * S16 · 마이크 권한 — 구현정본 node 86:225. 권한 안내 + 설정 이동/등록문장/직접 입력.
 */
@Composable
fun S16Permission(onSettings: () -> Unit, onRegistered: () -> Unit, onType: () -> Unit) {
    val PURPLE = Color(0xFF7546EB)
    FaithfulFrame {
        // 배경
        val card = RoundedCornerShape(d(22f))
        Box(Modifier.box(47f, 531f, 310f, 61f).shadow(d(2f), card, clip = false).background(MalSurface, card))
        Box(Modifier.box(33.83f, 614.49f, 322.33f, 60.35f).background(brandBrush(), RoundedCornerShape(d(24f))))
        val ob = RoundedCornerShape(d(24f))
        Box(Modifier.box(34.75f, 685.81f, 320.96f, 55.32f).shadow(d(2f), ob, clip = false).background(MalSurface, ob).border(BorderStroke(d(1f), Color(0xFFE5DEED)), ob))
        Box(Modifier.box(34.75f, 749.37f, 320.96f, 55.32f).shadow(d(2f), ob, clip = false).background(MalSurface, ob).border(BorderStroke(d(1f), Color(0xFFE5DEED)), ob))

        // 일러스트 + 아이콘
        artCrop(R.drawable.art_s16, 33.38f, 277.07f, 327.819f, 245.522f)
        art(R.drawable.brand_logo, 131.22f, 50.29f, 142.192f, 40.017f)
        Image(painterResource(R.drawable.ic_info), null, Modifier.box(70f, 549f, 25f, 25f))

        // 텍스트
        T("마이크 권한이\n필요해요", 20f, 115f, 350f, 40f, FontWeight.Normal, brush = juaTitleBrush(), align = TextAlign.Center, family = JuaFont, lineMul = 1.1f)
        T("음성으로 말하려면\n마이크 권한이 필요해요.", 28f, 223f, 334f, 14f, FontWeight.Normal, color = MalMuted, align = TextAlign.Center)
        T("설정에서 마이크를 허용하면\n말하기 기능을 사용할 수 있어요.", 110f, 546f, 226f, 13f, FontWeight.Normal, color = MalInk)
        T("설정으로 이동", 20f, 629.66f, 350f, 22f, FontWeight.Normal, color = Color.White, align = TextAlign.Center, family = JuaFont)
        T("등록문장 선택", 20f, 698.47f, 350f, 22f, FontWeight.Normal, color = PURPLE, align = TextAlign.Center, family = JuaFont)
        T("직접 입력", 20f, 762.03f, 350f, 22f, FontWeight.Normal, color = PURPLE, align = TextAlign.Center, family = JuaFont)

        // 터치영역
        Box(Modifier.box(33.83f, 614.49f, 322.33f, 60.35f).clickable(onClickLabel = "설정으로 이동") { onSettings() }.semantics { contentDescription = "기기 마이크 설정으로 이동" })
        Box(Modifier.box(34.75f, 685.81f, 320.96f, 55.32f).clickable(onClickLabel = "등록문장 선택") { onRegistered() }.semantics { contentDescription = "등록문장 선택" })
        Box(Modifier.box(34.75f, 749.37f, 320.96f, 55.32f).clickable(onClickLabel = "직접 입력") { onType() }.semantics { contentDescription = "직접 입력" })
    }
}
