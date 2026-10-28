package kr.voicemate.malitda.ui.v2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kr.voicemate.malitda.BuildConfig
import kr.voicemate.malitda.data.settings.ConfirmationMode
import kr.voicemate.malitda.data.settings.ListeningTimeLimit
import kr.voicemate.malitda.data.settings.ProfilePreferences
import kr.voicemate.malitda.data.settings.SettingsStore
import kr.voicemate.malitda.domain.GuardianPin
import kr.voicemate.malitda.domain.GuardianPinResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner

/** Current V2 consent. Microphone, pilot logging and external image generation remain separate choices. */
@Composable
fun V2ConsentScreen(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onBack: (() -> Unit)? = null,
    readOnly: Boolean = false,
    onRevoke: (() -> Unit)? = null,
    onManageData: (() -> Unit)? = null,
) {
    var serviceAccepted by remember { mutableStateOf(false) }
    var storageAccepted by remember { mutableStateOf(false) }
    var revokeConfirm by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    V2Screen {
        V2Header(onBack = onBack, onHelp = { showHelp = true })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            V2Title(if (readOnly) "개인정보와 사용 안내" else "시작 전 함께 확인")
            V2SubTitle("말잇다는 내가 전할 뜻을 확인하고 상대에게 보여 주는 앱입니다.")
            Spacer(Modifier.height(18.dp))
            InfoSection("말잇다의 역할", "평소 쓰는 표현을 나만의 뜻과 연결합니다. 치료나 발음 평가를 제공하지 않습니다. 그림·글·소리를 확인한 뒤 이번에 전할 뜻을 직접 선택합니다. 잘 모르겠으면 전달하지 않고 멈출 수 있습니다.")
            InfoSection("휴대전화에 남는 정보", "이름 또는 별칭, 등록한 표현과 뜻, 선택한 그림, 확인 반응과 설정을 이 기기에 저장합니다. 개인 뜻 사전은 암호화된 데이터베이스에, 직접 고른 사진은 앱 전용 저장공간에 보관합니다. 이전 버전에서 저장한 교정 이력이 있으면 남아 있을 수 있으며 자료 관리에서 확인하고 초기화할 수 있습니다. 시스템 백업은 사용하지 않습니다. 설정에서 개별 기록이나 전체 자료를 지울 수 있습니다.")
            InfoSection("마이크와 음성인식", "말하기를 누를 때 마이크 권한을 요청합니다. 현재 음성인식은 기기 안의 Whisper·Vosk로 처리합니다. 원음 파일은 저장하거나 서버로 보내지 않습니다. 인식한 문장은 현재 확인 과정에만 두며, 취소·새 시도·앱 종료 때 사라집니다. 마이크를 허용하지 않아도 카드와 직접 입력을 사용할 수 있습니다.")
            InfoSection("직접 선택한 수정만 저장", "문장을 고쳐 확인한 뒤 '다음에도 이 수정 사용'을 따로 선택하면, 인식 원문과 확인 문장을 이 기기의 암호화된 저장소에 보관합니다. 다음에 같은 말을 들으면 후보로만 보여 줍니다. 저장하지 않아도 전달할 수 있으며 '내 표현 > 저장한 수정'에서 개별 삭제할 수 있습니다. 다른 앱이나 서버로 자동 전송하지 않습니다.")
            if (BuildConfig.PILOT_TOOLS_ENABLED) InfoSection("실증 기록은 따로 선택", "일반 사용에는 실증 기록을 남기지 않습니다. 실증 참여를 따로 선택한 경우에만 참여자 코드와 시도 결과를 기록합니다. 실증 설정에서 기록 범위·동의·철회·삭제·내보내기를 확인합니다.")
            InfoSection("AI 그림은 만들 때마다 확인", "지원자가 AI 그림 만들기를 선택하면 등록한 뜻 문장, 익명 식별자와 그림체가 연결된 외부 서버로 전송될 수 있습니다. 전송·비용 안내를 확인하고 별도로 동의해야 생성합니다. 취소해도 이미 시작한 서버 작업이나 비용이 취소된다고 보장하지 않습니다.")
            if (onManageData != null) V2TextButton("동의하지 않고 보관 자료 관리", onClick = onManageData)
            InfoSection("사용 범위와 한계", "모든 장애인의 발화를 자동으로 해석하지 않습니다. 발화 특성이 심하거나 말소리가 불명확하면 인식이 어려울 수 있습니다. 등록하지 않은 개인 표현과 자유로운 대화의 의도를 보장하지 않습니다. 휴대전화와 보조기기는 개별 확인이 필요하며 응급 연락 수단으로 사용하지 않습니다.")
            InfoSection("지원자 모드", "지원자용 PIN은 이 기기에서 표현과 설정 관리 화면을 잠급니다. 보호자의 법적 권한이나 서버 계정을 인증하는 기능은 아닙니다. 설정을 도와준다는 이유로 학생의 의사를 대신 확정할 수 없습니다.")
            if (!readOnly) {
                ConsentCheck("앱의 역할과 선택·중단 방법을 확인했습니다.", serviceAccepted) { serviceAccepted = it }
                ConsentCheck("이 기기의 정보 저장·삭제 안내를 확인했습니다.", storageAccepted) { storageAccepted = it }
                Text("AI 그림의 외부 전송에는 별도 동의가 필요합니다.", style = jua(15, V2C.Muted), modifier = Modifier.padding(24.dp))
                V2PrimaryButton("확인하고 시작", enabled = serviceAccepted && storageAccepted, onClick = onAccept)
                V2TextButton("지금은 시작하지 않기", onClick = onDecline)
            } else if (onRevoke != null) {
                V2TextButton("안내 동의 철회", onClick = { revokeConfirm = true })
            }
            Spacer(Modifier.height(20.dp))
        }
    }
    if (showHelp) V2HelpDialog("앱의 역할과 자료 저장 안내를 지원자와 함께 확인합니다. 동의하지 않으면 지금 시작하지 않아도 됩니다. AI 전송은 그림을 만들 때 따로 확인합니다.") { showHelp = false }
    if (revokeConfirm) AlertDialog(
        onDismissRequest = { revokeConfirm = false },
        title = { Text("안내 동의를 철회할까요?", style = jua(22, V2C.Title)) },
        text = { Text("새로운 말하기·저장을 멈추고 시작 안내로 돌아갑니다. 이미 저장한 자료는 자동으로 삭제하지 않습니다. 삭제는 설정의 전체 자료 삭제에서 별도로 진행합니다.", style = jua(17, V2C.Ink)) },
        confirmButton = { TextButton(onClick = { revokeConfirm = false; onRevoke?.invoke() }) { Text("철회") } },
        dismissButton = { TextButton(onClick = { revokeConfirm = false }) { Text("돌아가기") } },
    )
}

@Composable
fun GuardianGateScreen(
    settingsStore: SettingsStore,
    onUnlocked: () -> Unit,
    onCancel: () -> Unit,
    onDeviceRecovery: (() -> Unit)? = null,
) {
    val settings by settingsStore.settings.collectAsState(initial = kr.voicemate.malitda.data.settings.Settings())
    val scope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var pin by remember { mutableStateOf("") }
    var repeatedPin by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var remaining by remember { mutableStateOf(0L) }
    val creating = !settings.guardianConfigured
    LaunchedEffect(settings.guardianLockedUntil) {
        do {
            remaining = ((settings.guardianLockedUntil - System.currentTimeMillis() + 999L) / 1000L).coerceAtLeast(0L)
            if (remaining > 0L) delay(1000L)
        } while (remaining > 0L)
    }
    fun submit() {
        if (submitting || remaining > 0 || !GuardianPin.valid(pin)) return
        if (creating && repeatedPin != pin) { message = "두 PIN이 다릅니다. 다시 확인합니다."; return }
        submitting = true
        scope.launch {
            try {
                if (creating) {
                    if (settingsStore.setGuardianPin(pin)) { pin = ""; repeatedPin = ""; if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) onUnlocked() }
                    else message = "PIN을 설정하지 못했습니다. 다시 확인합니다."
                } else when (val result = settingsStore.verifyGuardianPin(pin)) {
                    GuardianPinResult.Unlocked -> { pin = ""; if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) onUnlocked() }
                    GuardianPinResult.NotConfigured -> { pin = ""; message = "지원자 PIN을 먼저 정합니다." }
                    is GuardianPinResult.Incorrect -> { pin = ""; message = "PIN이 다릅니다. ${result.attemptsUntilDelay}번 더 틀리면 잠시 잠깁니다." }
                    is GuardianPinResult.Locked -> { pin = ""; remaining = result.remainingSeconds; message = "잠시 뒤 다시 확인합니다." }
                }
            } catch (_: Exception) { message = "PIN을 확인하지 못했습니다. 잠시 뒤 다시 시도합니다." }
            finally { submitting = false }
        }
    }
    V2Screen {
        V2Header(onBack = onCancel, onHelp = { message = "처음 설정은 기기를 관리하는 지원자와 함께 진행합니다. PIN을 모르면 학생용 홈으로 돌아갈 수 있습니다." })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding()) {
            V2Title(if (creating) "지원자 PIN 정하기" else "지원자 모드 열기")
            V2SubTitle(if (creating) "학생의 표현과 설정을 관리할 숫자 6자리를 정합니다." else "등록·설정·기록을 관리하려면 PIN을 입력합니다.")
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter { c -> c in '0'..'9' }.take(6); message = "" },
                label = { Text(if (creating) "새 PIN 6자리" else "PIN 6자리") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = if (creating) ImeAction.Next else ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                singleLine = true,
                enabled = !submitting && remaining == 0L,
                textStyle = jua(24, V2C.Ink),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            )
            if (creating) {
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = repeatedPin,
                    onValueChange = { repeatedPin = it.filter { c -> c in '0'..'9' }.take(6); message = "" },
                    label = { Text("같은 PIN 다시 입력") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    singleLine = true,
                    enabled = !submitting,
                    textStyle = jua(24, V2C.Ink),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                )
            }
            Text(
                if (remaining > 0L) "${remaining}초 후 다시 입력할 수 있습니다." else message,
                style = jua(16, V2C.Ink),
                modifier = Modifier.padding(24.dp).semantics { liveRegion = LiveRegionMode.Polite },
            )
            V2PrimaryButton(if (submitting) "확인 중" else if (creating) "PIN 정하고 열기" else "지원자 모드 열기", enabled = !submitting && remaining == 0L && GuardianPin.valid(pin) && (!creating || GuardianPin.valid(repeatedPin)), onClick = ::submit)
            V2TextButton("학생용 홈으로 돌아가기", onClick = onCancel)
            if (!creating && onDeviceRecovery != null) V2TextButton("기기 잠금으로 PIN 다시 정하기", onClick = onDeviceRecovery)
            InfoSection("이 기기에서만 잠금", "법적 보호자나 계정 인증을 대신하지 않습니다. 앱을 벗어나면 다시 잠깁니다. PIN을 잊으면 기기 잠금 확인이 필요하며, 기기 잠금을 사용하지 않는 경우 기존 자료를 보존한 PIN 복구는 제공하지 않습니다.")
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun V2ProfilePreferencesScreen(
    studentName: String,
    preferences: ProfilePreferences,
    onPreferences: (ProfilePreferences) -> Unit,
    fontScale: Float,
    onFontScale: (Float) -> Unit,
    visualEmphasis: Boolean,
    onVisualEmphasis: (Boolean) -> Unit,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onPreviewTts: () -> Unit = {},
) {
    V2Screen {
        V2Header(onBack = onBack, onHelp = onHelp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            V2Title("확인 방법과 접근성")
            V2SubTitle("${studentName.ifBlank { "학생" }}에게 편한 방법을 함께 고릅니다.")
            InfoSection("뜻을 확인할 때", "그림·글·소리를 모두 사용할 수 있습니다. 먼저 표시할 방법만 정하며, 소리는 학생이 누를 때 재생합니다.")
            ConfirmationMode.entries.forEach { mode ->
                PreferenceOption(mode.label, preferences.confirmationMode == mode) { onPreferences(preferences.copy(confirmationMode = mode)) }
            }
            SectionLabel("한 번에 보여 줄 후보")
            listOf(1, 2, 3).forEach { count -> PreferenceOption("최대 ${count}개", preferences.candidateCount == count) { onPreferences(preferences.copy(candidateCount = count)) } }
            PreferenceToggle("비슷하게 들린 표현도 후보로", preferences.allowSimilarExpressions) { onPreferences(preferences.copy(allowSimilarExpressions = it)) }
            Text("꺼 두면 비슷한 발음만으로는 후보를 제안하지 않습니다. 어떤 설정에서도 뜻을 자동으로 선택하거나 전달하지 않습니다.", style = jua(15, V2C.Muted), modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
            SectionLabel("말을 듣는 시간")
            Text("천천히 말할 시간이 더 필요하면 60초를 고릅니다. 직접 끝내기를 고르면 시간 제한 없이 듣고, 말하기 끝내기나 취소를 누를 때 멈춥니다. 다음 말하기부터 적용합니다.", style = jua(15, V2C.Muted), modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
            ListeningTimeLimit.entries.forEach { limit ->
                PreferenceOption(limit.label, preferences.listenTimeLimitSeconds == limit.seconds) {
                    onPreferences(preferences.copy(listenTimeLimitSeconds = limit.seconds))
                }
            }
            SectionLabel("말을 읽는 속도")
            listOf(0.65f to "천천히", 0.8f to "조금 천천히", 1f to "보통").forEach { (rate, label) ->
                PreferenceOption(label, kotlin.math.abs(preferences.ttsRate - rate) < 0.01f) { onPreferences(preferences.copy(ttsRate = rate)) }
            }
            V2TextButton("선택한 속도로 들어 보기", onClick = onPreviewTts)
            PreferenceToggle("진동으로 알려주기", preferences.haptics) { onPreferences(preferences.copy(haptics = it)) }
            SectionLabel("이 기기의 글자 크기")
            Text("기본 크기는 100%입니다. 글자 크기와 선택 강조는 이 기기에 공통으로 적용합니다. 휴대전화의 글자 확대 설정도 함께 적용합니다. 앱 설정을 줄여도 시스템에서 키운 글자를 작게 만들지 않습니다.", style = jua(15, V2C.Muted), modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
            listOf(1f to "기본 크기 · 100%", 1.2f to "확대 · 120%", 1.5f to "확대 · 150%", 2f to "접근성 확대 · 200%").forEach { (scale, label) ->
                PreferenceOption(label, kotlin.math.abs(fontScale - scale) < 0.01f) { onFontScale(scale) }
            }
            PreferenceToggle("테두리와 선택 강조", visualEmphasis, onVisualEmphasis)
            InfoSection("보조기술 사용 안내", "휴대전화 설정의 접근성에서 TalkBack·스위치 제어·음성 액세스를 확인할 수 있습니다. 앱 정보의 보조기술 안내에 조작 방법과 지원 언어 제한을 정리했습니다. 기기와 버전에 따라 메뉴 이름은 다를 수 있습니다.")
            Text("변경한 값은 이 기기에 바로 저장합니다. 후보·확인 방식·듣기 시간·읽는 속도·진동은 현재 학생에게만 적용합니다.", style = jua(15, V2C.Muted), modifier = Modifier.padding(24.dp))
            V2SecondaryButton("설정으로 돌아가기", onClick = onBack)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun V2AppInfoScreen(onBack: () -> Unit, onHelp: () -> Unit, onProject: (() -> Unit)? = null) {
    V2Screen {
        V2Header(onBack = onBack, onHelp = onHelp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            V2Title("말잇다 앱 정보")
            InfoSection("버전", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) · Android 8.0 이상")
            InfoSection("어떤 앱인가", "평소 쓰는 표현을 본인의 뜻과 연결하고, 그림·글·소리로 확인한 뒤 상대에게 전하는 의사소통 지원 앱입니다. 발음 평가나 치료 효과를 보장하지 않습니다.")
            InfoSection("사용 범위", "현재 기기 안에서 개인별 표현과 뜻을 관리합니다. 기기 간 자동 동기화와 기관 서버 계정 기능은 제공하지 않습니다. 실제 이용자가 뜻을 이해하고 선택할 수 있는지는 익숙한 지원자와 확인해야 합니다.")
            InfoSection("음성인식과 생성 그림", "Whisper·Vosk가 기기 안에서 음성을 인식합니다. 인식 결과는 틀릴 수 있으므로 원문과 뜻을 직접 확인합니다. AI 그림은 별도 서버 연결과 생성 때마다의 전송·비용 동의가 필요합니다. 서버 연결 여부와 별개로 카드와 직접 입력을 사용할 수 있습니다.")
            InfoSection("중단과 도움", "뜻이 다르거나 판단하기 어렵다면 다시 선택하거나 멈춥니다. 마이크·소리 문제는 설정의 안내를 확인합니다. 동작 오류나 개선 의견은 아래 개발 프로젝트의 Issues로 남길 수 있습니다. 개인 이름·음성·문장·사진은 공개 문의에 올리지 않습니다.")
            // Official Android accessibility help, checked 2026-10-26:
            // TalkBack: https://support.google.com/accessibility/android/answer/6007100?hl=ko
            // Switch Access: https://support.google.com/accessibility/android/answer/6122836?hl=ko
            // Voice Access languages: https://support.google.com/accessibility/android/answer/6151848?hl=en
            InfoSection("1. TalkBack으로 버튼 듣기", "휴대전화 설정 → 접근성 → TalkBack에서 켭니다. 오른쪽으로 쓸어 다음 항목의 이름을 듣고, 원하는 항목에서 두 번 탭합니다. '말하기 끝내기', '맞아요', '다른 뜻이에요'처럼 앱에 보이는 버튼 이름을 확인합니다.")
            InfoSection("2. 스위치로 항목 고르기", "설정 → 접근성에서 '스위치 제어' 또는 '스위치 액세스(Switch Access)'를 찾습니다. 연결한 스위치나 키보드로 항목을 차례로 강조한 뒤 선택합니다. '홈', '내 표현', '설정'과 '카드로 고르기'를 기준으로 이동할 수 있는지 함께 확인합니다.")
            InfoSection("3. Voice Access의 번호 사용", "설정 → 접근성 → 음성 액세스(Voice Access)에서 지원 언어와 활성화 방법을 확인합니다. 현재 공식 지원 언어 목록에는 한국어가 없습니다. 지원 언어로 사용할 수 있다면 'Show numbers' 같은 번호 표시 명령으로 항목을 찾고 번호로 선택하는 방법을 검토합니다. 앱 버튼 이름이 한국어라는 이유로 한국어 음성 명령이 지원된다고 볼 수는 없습니다.")
            InfoSection("보조기술 확인 범위", "위 내용은 Android의 공식 조작 안내입니다. 메뉴와 설치 상태는 기기·버전에 따라 다릅니다. 말잇다의 모든 화면을 TalkBack·스위치 제어·Voice Access로 실제 휴대전화에서 검증한 상태는 아닙니다. 이용할 기기에서 지원자와 시작·선택·중단 과정을 먼저 확인해야 합니다.")
            if (onProject != null) V2SecondaryButton("개발 프로젝트·문의 열기", onClick = onProject)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoSection(title: String, text: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
        Text(title, style = jua(20, V2C.Title), modifier = Modifier.semantics { heading() })
        Spacer(Modifier.height(8.dp))
        Text(text, style = jua(17, V2C.Ink, 1.5f))
    }
}

@Composable
private fun ConsentCheck(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).heightIn(min = 48.dp).toggleable(value = checked, role = Role.Checkbox, onValueChange = onChange), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(label, style = jua(17, V2C.Ink), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SectionLabel(text: String) { Text(text, style = jua(21, V2C.Title), modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)) }

@Composable
private fun PreferenceOption(label: String, selected: Boolean, onChoose: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(min = 52.dp).selectable(selected = selected, role = Role.RadioButton, onClick = onChoose).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = null)
        Text(label, style = jua(19, V2C.Ink), modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
    }
}

@Composable
private fun PreferenceToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp).heightIn(min = 48.dp).toggleable(value = checked, role = Role.Switch, onValueChange = onChange), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = jua(19, V2C.Ink), modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = null)
    }
}
