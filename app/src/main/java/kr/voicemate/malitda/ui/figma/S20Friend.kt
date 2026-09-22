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
 * S20 · 새로운 말친구 — 구현정본 node 86:249. 캐릭터 획득 축하(리워드).
 */
@Composable
fun S20Friend(
    name: String,
    desc: String,
    onSet: () -> Unit,
    onCatalog: () -> Unit,
    onLater: () -> Unit,
) {
    val PURPLE = Color(0xFF7546EB)
    FaithfulFrame {
        // 배경
        val card = RoundedCornerShape(d(22f))
        Box(Modifier.box(40f, 566f, 310f, 68f).shadow(d(2f), card, clip = false).background(MalSurface, card))
        Box(Modifier.box(39.78f, 649.24f, 309.99f, 51.66f).background(brandBrush(), RoundedCornerShape(d(24f))))
        val cat = RoundedCornerShape(d(24f))
        Box(Modifier.box(40.69f, 712.33f, 309.99f, 52.12f).background(Color.White, cat).border(BorderStroke(d(1f), Color(0xFFEDE8EF)), cat))

        // 아트 + 아이콘
        artCrop(R.drawable.scene_s20_heart, 31.09f, 231.81f, 336.506f, 249.637f)
        art(R.drawable.brand_logo, 126.19f, 47.55f, 138.077f, 38.859f)
        Image(painterResource(R.drawable.ic_heart), null, Modifier.box(57f, 586f, 32f, 32f))

        // 텍스트
        T("캐릭터 획득 축하", 101f, 92f, 216f, 14f, FontWeight.Normal, color = Color(0xFF9560ED), align = TextAlign.Center)
        T("새로운 말친구를\n만났어요", 20f, 146f, 350f, 40f, FontWeight.Normal, brush = juaTitleBrush(), align = TextAlign.Center, family = JuaFont, lineMul = 1.05f)
        T(name, 139f, 493f, 140f, 31f, FontWeight.Black, color = Color(0xFF8A4BEF), align = TextAlign.Center)
        T(desc, 61f, 540f, 280f, 18f, FontWeight.Normal, color = MalMuted, align = TextAlign.Center)
        T("${name}를 획득했어요!", 110f, 579f, 227f, 17f, FontWeight.Bold, color = MalInk)
        T("하트 포인트 50P 지급", 110f, 607f, 228f, 13f, FontWeight.Normal, color = Color(0xFF8F8A97))
        T("대표 캐릭터로 설정", 20f, 660.07f, 350f, 22f, FontWeight.Normal, color = Color.White, align = TextAlign.Center, family = JuaFont)
        T("도감에서 보기", 20f, 723.39f, 350f, 22f, FontWeight.Normal, color = PURPLE, align = TextAlign.Center, family = JuaFont)
        T("나중에 할게요", 140.36f, 785.5f, 133.96f, 16f, FontWeight.Normal, color = Color(0xFF854BFF), align = TextAlign.Center)

        // 터치영역
        Box(Modifier.box(39.78f, 649.24f, 309.99f, 51.66f).clickable(onClickLabel = "대표 설정") { onSet() }.semantics { contentDescription = "대표 캐릭터로 설정" })
        Box(Modifier.box(40.69f, 712.33f, 309.99f, 52.12f).clickable(onClickLabel = "도감") { onCatalog() }.semantics { contentDescription = "도감에서 보기" })
        Box(Modifier.box(140.36f, 781.83f, 133.96f, 29.26f).clickable(onClickLabel = "나중에") { onLater() }.semantics { contentDescription = "나중에 할게요" })
    }
}
