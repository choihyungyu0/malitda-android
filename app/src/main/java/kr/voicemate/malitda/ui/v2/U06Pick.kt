package kr.voicemate.malitda.ui.v2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kr.voicemate.malitda.data.db.MeaningEntity

/** U06 · 어떤 뜻인가요?(화면 06) — 다른 뜻 선택 목록. */
@Composable
fun U06Pick(
    meanings: List<MeaningEntity>,
    onPick: (MeaningEntity) -> Unit,
    onNotHere: () -> Unit,
    onRespeak: () -> Unit,
    onCancel: () -> Unit,
    onHelp: () -> Unit,
) {
    V2Screen {
        V2Header(onBack = onCancel, onHelp = onHelp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            V2Title("어떤 뜻인가요?")
            V2SubTitle("그림과 문장을 보고 골라 주세요.")
            Spacer(Modifier.height(16.dp))
            meanings.forEach { m ->
                PickRow(m) { onPick(m) }
                Spacer(Modifier.height(14.dp))
            }
            V2SecondaryButton("여기에 없어요") { onNotHere() }
            Spacer(Modifier.height(16.dp))
        }
        V2PrimaryButton("다시 말하기", icon = "🎙") { onRespeak() }
        V2TextButton("취소") { onCancel() }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PickRow(m: MeaningEntity, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(6.dp, RoundedCornerShape(26.dp), clip = false,
                ambientColor = V2C.Shadow.copy(alpha = 0.08f), spotColor = V2C.Shadow.copy(alpha = 0.16f))
            .background(V2C.Surface, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .v2PorcelainFinish(RoundedCornerShape(26.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MeaningPicture(m.imageKey, Modifier.size(width = 120.dp, height = 84.dp).clip(RoundedCornerShape(16.dp)), glyphSize = 30)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(m.text, style = jua(19, V2C.Ink))
            Spacer(Modifier.height(10.dp))
            Text("→", style = jua(20, V2C.Title), modifier = Modifier.align(Alignment.End))
        }
    }
}
