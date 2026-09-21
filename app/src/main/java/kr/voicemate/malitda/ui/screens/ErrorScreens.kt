package kr.voicemate.malitda.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.voicemate.malitda.R
import kr.voicemate.malitda.ui.components.CharacterImage
import kr.voicemate.malitda.ui.components.GradientButton
import kr.voicemate.malitda.ui.components.GradientTitle
import kr.voicemate.malitda.ui.components.InfoBox
import kr.voicemate.malitda.ui.components.MCard
import kr.voicemate.malitda.ui.components.OutlineButton
import kr.voicemate.malitda.ui.components.ScreenScaffold
import kr.voicemate.malitda.ui.components.Subtitle
import kr.voicemate.malitda.ui.components.TextAction
import kr.voicemate.malitda.ui.theme.MColors

/** S16 마이크 권한이 필요해요 */
@Composable
fun MicPermissionScreen(onRetry: () -> Unit, onOpenSettings: () -> Unit, onExpressions: () -> Unit, onDirect: () -> Unit, onBack: () -> Unit) {
    ScreenScaffold(onBack = onBack, bottomBar = {
        GradientButton("마이크 권한 다시 요청", onClick = onRetry, icon = Icons.Rounded.Mic)
        OutlineButton("등록문장 선택", onClick = onExpressions, icon = Icons.Rounded.List)
        TextAction("직접 입력", onClick = onDirect)
    }) {
        GradientTitle("마이크 권한이\n필요해요")
        Spacer(Modifier.height(6.dp))
        Subtitle("말하려면 마이크 권한이 필요해요.")
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { CharacterImage(R.drawable.char_robot_pink, 130.dp) }
        Spacer(Modifier.height(18.dp))
        MCard {
            Text("지금 켜지 않아도 등록한 문장을 선택하거나 직접 글을 입력할 수 있어요.\n\n자동으로 설정을 바꾸지 않아요. 기기 설정에서 마이크를 켤 수 있어요.", style = MaterialTheme.typography.bodyMedium, color = MColors.Ink2)
        }
        Spacer(Modifier.height(10.dp))
        TextAction("기기 설정 열기", onClick = onOpenSettings, color = MColors.Ink2)
    }
}

/** S17 잘 들리지 않았어요 */
@Composable
fun NoResultScreen(onRespeak: () -> Unit, onDirect: () -> Unit, onExpressions: () -> Unit, onBack: () -> Unit) {
    var aac by remember { mutableStateOf(false) }
    ScreenScaffold(onBack = onBack, bottomBar = {
        GradientButton("다시 말하기", onClick = onRespeak, icon = Icons.Rounded.Refresh)
        OutlineButton("직접 입력", onClick = onDirect, icon = Icons.Rounded.Keyboard)
        TextAction("등록문장 선택", onClick = onExpressions)
    }) {
        GradientTitle("잘 들리지 않았어요")
        Spacer(Modifier.height(6.dp))
        Subtitle("다시 말하거나 다른 방법을 골라요.")
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { CharacterImage(R.drawable.char_water, 130.dp) }
        Spacer(Modifier.height(18.dp))
        InfoBox("작성 중이던 문장은 그대로 있어요.\n반복해서 말하도록 강요하지 않아요.")
        Spacer(Modifier.height(10.dp))
        TextAction("기존 AAC 이용 안내", onClick = { aac = true }, color = MColors.Ink2)
    }
    if (aac) AacDialog { aac = false }
}

@Composable
fun AacDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("기존 AAC 이용 안내") },
        text = { Text("말잇다는 AAC를 대체하지 않아요. 말로 입력하기 어려우면 평소 쓰던 AAC 앱(예: 나의AAC)의 상징·문장 선택으로 의사를 표현하고, 준비되면 다시 말잇다로 돌아와도 돼요.\n\n작성 중인 문장은 말잇다에 그대로 남아 있어요.") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("확인") } },
    )
}

/** S23 지금은 음성을 인식할 수 없어요 */
@Composable
fun SttErrorScreen(message: String?, onRetry: () -> Unit, onExpressions: () -> Unit, onDirect: () -> Unit, onBack: () -> Unit) {
    var aac by remember { mutableStateOf(false) }
    ScreenScaffold(onBack = onBack, bottomBar = {
        GradientButton("등록문장 선택", onClick = onExpressions, icon = Icons.Rounded.List)
        OutlineButton("직접 입력", onClick = onDirect, icon = Icons.Rounded.Keyboard)
        TextAction("한 번 다시 시도", onClick = onRetry)
    }) {
        GradientTitle("지금은 음성을\n인식할 수 없어요")
        Spacer(Modifier.height(6.dp))
        Subtitle("기기의 음성인식 준비를 확인해 주세요.")
        Spacer(Modifier.height(18.dp))
        MCard(color = MColors.SurfaceSoft) {
            Text("다른 방법으로 계속할 수 있어요.\n등록문장 선택 / 직접 입력 / 기존 AAC\n\n작성 중인 문장은 유지해요.\n외부 음성인식으로 자동 전환하지 않아요.\n무한 자동 재시도는 하지 않아요.", style = MaterialTheme.typography.bodyMedium, color = MColors.Ink2)
            if (!message.isNullOrBlank()) { Spacer(Modifier.height(10.dp)); Text("상세: $message", style = MaterialTheme.typography.bodySmall, color = MColors.Muted) }
        }
        Spacer(Modifier.height(10.dp))
        TextAction("기존 AAC 이용 안내", onClick = { aac = true }, color = MColors.Ink2)
    }
    if (aac) AacDialog { aac = false }
}

/** S24 지금은 소리로 읽을 수 없어요 */
@Composable
fun TtsErrorScreen(text: String, reason: String?, onReadText: () -> Unit, onSettings: () -> Unit, onBack: () -> Unit) {
    ScreenScaffold(onBack = onBack, bottomBar = {
        GradientButton("글로 확인하기", onClick = onReadText)
        TextAction("설정 확인", onClick = onSettings)
    }) {
        GradientTitle("지금은 소리로\n읽을 수 없어요")
        Spacer(Modifier.height(6.dp))
        Subtitle("글과 그림으로 문장을 확인해 주세요.")
        Spacer(Modifier.height(18.dp))
        MCard(color = MColors.SurfaceSoft) {
            Text("선택한 문장", style = MaterialTheme.typography.labelMedium, color = MColors.Violet)
            Spacer(Modifier.height(6.dp))
            Text(text.ifBlank { "(아직 문장이 없어요)" }, style = MaterialTheme.typography.headlineSmall, color = MColors.Ink)
        }
        Spacer(Modifier.height(12.dp))
        InfoBox("읽어주는 속도·한국어 음성은 설정에서 확인할 수 있어요.\n음성이 없어도 직접 승인할 수 있어요." + (if (reason != null) "\n($reason)" else ""))
    }
}
