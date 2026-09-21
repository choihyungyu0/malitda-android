package kr.voicemate.malitda.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kr.voicemate.malitda.R
import kr.voicemate.malitda.data.settings.Settings
import kr.voicemate.malitda.ui.components.CharacterImage
import kr.voicemate.malitda.ui.components.GradientButton
import kr.voicemate.malitda.ui.components.GradientTitle
import kr.voicemate.malitda.ui.components.InfoBox
import kr.voicemate.malitda.ui.components.MCard
import kr.voicemate.malitda.ui.components.NumberBadge
import kr.voicemate.malitda.ui.components.ScreenScaffold
import kr.voicemate.malitda.ui.components.Subtitle
import kr.voicemate.malitda.ui.components.TextAction
import kr.voicemate.malitda.ui.theme.MColors

/** S01 시작 */
@Composable
fun SplashScreen(onStart: () -> Unit, onHome: () -> Unit, autoAdvance: Boolean) {
    LaunchedEffect(autoAdvance) { if (autoAdvance) { delay(900); onHome() } }
    Column(
        Modifier.fillMaxSize().background(MColors.Bg).safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Image(painterResource(R.drawable.logo_full), contentDescription = "말잇다 — 당신의 말을, 그대로 잇다", modifier = Modifier.fillMaxWidth(0.8f), contentScale = ContentScale.Fit)
        Spacer(Modifier.height(28.dp))
        Text("내 말을,\n그대로 잇다", style = MaterialTheme.typography.displayLarge, color = MColors.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Subtitle("내가 말한 문장을 확인하고, 내가 승인한 문장만 보내요.")
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CharacterImage(R.drawable.char_purple, 96.dp)
            CharacterImage(R.drawable.char_robot_mint, 96.dp)
            CharacterImage(R.drawable.char_water, 96.dp)
        }
        Spacer(Modifier.weight(1f))
        if (!autoAdvance) {
            GradientButton("시작하기", onClick = onStart)
            TextAction("바로 홈으로", onClick = onHome)
        }
    }
}

/** S02 서비스 안내 */
@Composable
fun IntroScreen(onNext: () -> Unit, onSkip: () -> Unit) {
    ScreenScaffold(bottomBar = { GradientButton("다음", onClick = onNext); TextAction("건너뛰기", onClick = onSkip) }) {
        GradientTitle("내 말을 더\n정확하게 전달해요")
        Spacer(Modifier.height(8.dp))
        Subtitle("세 단계면 충분해요.")
        Spacer(Modifier.height(20.dp))
        IntroStep(1, "말해요", "큰 버튼을 눌러 말해요.\n시간 제한은 없어요.", R.drawable.char_robot_mint)
        Spacer(Modifier.height(12.dp))
        IntroStep(2, "확인하고 고쳐요", "인식된 원문을 보고 직접 골라요.\n그림·쉬운 문장·음성으로 확인해요.", R.drawable.char_purple)
        Spacer(Modifier.height(12.dp))
        IntroStep(3, "맞으면 보내요", "내가 확인한 문장만 전달해요.\n확인하기 전에는 아무것도 나가지 않아요.", R.drawable.char_heart)
    }
}

@Composable
private fun IntroStep(n: Int, title: String, body: String, charRes: Int) {
    MCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberBadge(n, active = true)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MColors.Ink)
                Spacer(Modifier.height(4.dp))
                Text(body, style = MaterialTheme.typography.bodySmall, color = MColors.Ink2)
            }
            Spacer(Modifier.width(8.dp))
            CharacterImage(charRes, 64.dp)
        }
    }
}

/** S03 안내·동의 (최신 기획서 반영) */
@Composable
fun ConsentScreen(onAccept: () -> Unit, onLater: () -> Unit) {
    var read by remember { mutableStateOf(false) }
    var agree by remember { mutableStateOf(false) }
    var policy by remember { mutableStateOf(false) }
    if (policy) {
        AlertDialog(
            onDismissRequest = { policy = false },
            title = { Text("개인정보 처리방침(안)") },
            text = { Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState())) { Text(PRIVACY_POLICY_TEXT, style = MaterialTheme.typography.bodySmall) } },
            confirmButton = { TextButton(onClick = { policy = false }) { Text("확인") } },
        )
    }
    ScreenScaffold(bottomBar = {
        GradientButton("안내를 확인했어요", onClick = onAccept, enabled = read && agree)
        TextAction("나중에 할게요", onClick = onLater)
    }) {
        GradientTitle("안내를 확인하고\n시작해요")
        Spacer(Modifier.height(18.dp))
        ConsentCard(Icons.Rounded.MicOff, "원음성은 저장하지 않아요", "마이크는 말할 때만 사용해요. 목소리 원음은 파일로 저장하지 않아요.")
        Spacer(Modifier.height(10.dp))
        ConsentCard(Icons.Rounded.CloudOff, "음성은 이 기기에서 인식해요", "음성인식은 기기 안에서 처리해요. 외부 STT로 자동 전환하지 않아요. 품질개선용 개인 음성 수집은 없어요.")
        Spacer(Modifier.height(10.dp))
        ConsentCard(Icons.Rounded.Shield, "표현과 승인한 교정은 기기에 저장해요", "앱에서 언제든 지우거나 초기화할 수 있어요. 공유를 누르면 내가 고른 앱에만 전달돼요.")
        Spacer(Modifier.height(18.dp))
        CheckRow("위 내용을 확인했어요", read) { read = it }
        CheckRow("개인정보 처리방침에 동의해요", agree) { agree = it }
        TextAction("개인정보 처리방침 보기", onClick = { policy = true }, color = MColors.Ink2)
    }
}

@Composable
private fun ConsentCard(icon: ImageVector, title: String, body: String) {
    MCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(MColors.Lavender), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MColors.Violet, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MColors.Violet)
                Spacer(Modifier.height(4.dp))
                Text(body, style = MaterialTheme.typography.bodySmall, color = MColors.Ink2)
            }
        }
    }
}

@Composable
fun CheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(RoundedCornerShape(14.dp))
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onChange).padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MColors.Ink)
    }
}

/** S04 접근성 설정 (온보딩·설정 공용) */
@Composable
fun AccessibilityScreen(
    settings: Settings,
    onboarding: Boolean,
    onSave: (fontScale: Float, ttsRate: Float, haptics: Boolean, visual: Boolean, reward: Boolean) -> Unit,
    onDefault: () -> Unit,
    onBack: (() -> Unit)?,
) {
    var large by remember(settings.fontScale) { mutableStateOf(settings.fontScale > 1.05f) }
    var slow by remember(settings.ttsRate) { mutableStateOf(settings.ttsRate < 0.95f) }
    var haptics by remember(settings.haptics) { mutableStateOf(settings.haptics) }
    var visual by remember(settings.visualEmphasis) { mutableStateOf(settings.visualEmphasis) }
    var reward by remember(settings.rewardEnabled) { mutableStateOf(settings.rewardEnabled) }
    ScreenScaffold(onBack = onBack, bottomBar = {
        GradientButton(if (onboarding) "저장하고 계속" else "저장", onClick = { onSave(if (large) 1.2f else 1.0f, if (slow) 0.8f else 1.0f, haptics, visual, reward) })
        if (onboarding) TextAction("기본값으로 시작", onClick = onDefault)
    }) {
        GradientTitle("나에게 맞게\n설정해요")
        Spacer(Modifier.height(6.dp))
        Subtitle("설정은 언제든 바꿀 수 있어요.")
        Spacer(Modifier.height(18.dp))
        ChoiceRow("글자 크기", listOf("보통", "크게"), if (large) 1 else 0) { large = it == 1 }
        ChoiceRow("읽어주는 속도", listOf("느림", "보통"), if (slow) 0 else 1) { slow = it == 0 }
        SwitchRow("진동 알림", "승인·오류 때 짧게 진동해요", haptics) { haptics = it }
        SwitchRow("시각 강조", "읽어주는 단어를 색으로 표시하고 제목을 진하게 해요", visual) { visual = it }
        SwitchRow("캐릭터 보상", "말친구 리워드 화면을 켜요(선택 기능)", reward) { reward = it }
    }
}

@Composable
fun ChoiceRow(label: String, options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    MCard(padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = MColors.Ink)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEachIndexed { i, opt ->
                val sel = i == selected
                Box(
                    Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(14.dp))
                        .background(if (sel) MColors.Violet else MColors.Lavender)
                        .selectable(selected = sel, role = Role.RadioButton, onClick = { onSelect(i) }),
                    contentAlignment = Alignment.Center,
                ) { Text(opt, color = if (sel) Color.White else MColors.Ink, style = MaterialTheme.typography.labelLarge) }
            }
        }
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
fun SwitchRow(label: String, desc: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    MCard(padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall, color = MColors.Ink)
                Text(desc, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = MColors.Muted)
            }
            Switch(checked = checked, onCheckedChange = null)
        }
    }
    Spacer(Modifier.height(10.dp))
}

/** S05 핵심표현 등록 안내 */
@Composable
fun RegisterIntroScreen(onStart: () -> Unit, onLater: () -> Unit, onBack: (() -> Unit)?) {
    ScreenScaffold(onBack = onBack, bottomBar = { GradientButton("등록 시작", onClick = onStart); TextAction("나중에 할게요", onClick = onLater) }) {
        GradientTitle("핵심표현을\n등록해요")
        Spacer(Modifier.height(6.dp))
        Subtitle("자주 쓰는 표현으로 시작해요.\n처음에는 10~20개를 권해요.")
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { CharacterImage(R.drawable.char_green, 120.dp) }
        Spacer(Modifier.height(18.dp))
        MCard {
            Text("이름 · 장소 · 시간 · 메시지 · 자주 쓰는 말", style = MaterialTheme.typography.titleSmall, color = MColors.Violet)
            Spacer(Modifier.height(8.dp))
            Text("최대 50개까지 등록할 수 있어요.\n중간에 멈추고 나중에 이어도 돼요.\n보호자·교사는 첫 등록을 도울 수 있지만, 내 메시지를 볼 수는 없어요.", style = MaterialTheme.typography.bodySmall, color = MColors.Ink2)
        }
        Spacer(Modifier.height(12.dp))
        InfoBox("등록하지 않아도 말하기는 바로 쓸 수 있어요.")
    }
}
