package kr.voicemate.malitda.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kr.voicemate.malitda.data.db.CounterEntity
import kr.voicemate.malitda.data.db.ProfileEntity
import kr.voicemate.malitda.domain.Friends
import kr.voicemate.malitda.domain.MalFriend
import kr.voicemate.malitda.metrics.DeviceMetrics
import kr.voicemate.malitda.metrics.formatBytes
import kr.voicemate.malitda.stt.PrepareState
import kr.voicemate.malitda.tts.TtsState
import kr.voicemate.malitda.ui.components.CharacterImage
import kr.voicemate.malitda.ui.components.GradientButton
import kr.voicemate.malitda.ui.components.GradientTitle
import kr.voicemate.malitda.ui.components.InfoBox
import kr.voicemate.malitda.ui.components.MCard
import kr.voicemate.malitda.ui.components.ScreenScaffold
import kr.voicemate.malitda.ui.components.SectionLabel
import kr.voicemate.malitda.ui.components.Subtitle
import kr.voicemate.malitda.ui.components.TextAction
import kr.voicemate.malitda.ui.theme.MColors

const val PRIVACY_POLICY_TEXT = """말잇다 개인정보 처리방침(안) 요약

1. 처리 목적: 개인표현 등록·관리, 기기 내 음성인식, 사용자가 승인한 교정(M1) 재사용, 원문·후보의 음성 확인, 승인한 문장의 앱 공유.
2. 처리 항목: 개인표현·등록문장, 승인한 오인식→교정문 쌍, 앱 설정값. 사용자별로 분리해 기기에만 저장합니다. 마이크 음성은 기기 내 음성인식에만 사용하며 원음성 파일은 저장하지 않습니다. 장애유형 등 민감정보·고유식별정보는 수집하지 않습니다.
3. 보유 기간: 앱 안에서 개별 삭제·전체 초기화하거나 앱을 삭제할 때까지.
4. 제3자 제공: 하지 않습니다. 공유 버튼은 사용자가 직접 고른 앱에 문장을 전달하는 기능이며, 공유 이력·상대방 정보는 저장하지 않습니다.
5. 음성인식·TTS: 음성인식 모델과 실행기는 앱 안에 있고 외부 STT API·자동 클라우드 전환을 사용하지 않습니다. 이 앱은 인터넷 권한을 요청하지 않습니다. 한국어 읽어주기는 기기의 TTS 엔진을 사용합니다.
6. 품질개선용 개인 음성 수집: 없습니다. 개인 발화 학습(M2)은 별도 방침·동의 후에만 진행하는 후속 연구입니다.
7. 정보주체 권리: 앱 안에서 등록표현·교정·설정을 열람·수정·삭제할 수 있고, 삭제하면 기본 인식 결과로 돌아갑니다.
8. 안전성: 기기 내 저장 최소화, 사용자별 분리, DB 암호화(SQLCipher, 키는 Android Keystore 보관), 승인 전 외부 전달 차단, 마이크 등 필요 최소 권한.
9. 보호책임자·시행일: 출시 시점에 확정합니다."""

/** S18 설정·도움말 */
@Composable
fun SettingsScreen(
    profiles: List<ProfileEntity>,
    currentProfileId: Long,
    prepare: PrepareState,
    sttName: String,
    tts: TtsState,
    metrics: DeviceMetrics,
    onAccessibility: () -> Unit,
    onExpressions: () -> Unit,
    onCorrections: () -> Unit,
    onReplayIntro: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onSwitchProfile: (Long) -> Unit,
    onAddProfile: (String) -> Unit,
    onRenameProfile: (Long, String) -> Unit,
    onDeleteAll: () -> Unit,
    onBack: () -> Unit,
    testFiles: List<String> = emptyList(),
    onTestFile: (String) -> Unit = {},
    evalState: kr.voicemate.malitda.ui.vm.SessionViewModel.EvalState? = null,
    onRunEval: () -> Unit = {},
    onClearEval: () -> Unit = {},
) {
    var policy by remember { mutableStateOf(false) }
    var aac by remember { mutableStateOf(false) }
    var addProfile by remember { mutableStateOf(false) }
    var rename by remember { mutableStateOf<ProfileEntity?>(null) }
    var deleteAll by remember { mutableStateOf(false) }

    ScreenScaffold(onBack = onBack) {
        GradientTitle("설정 · 도움말")
        Spacer(Modifier.height(14.dp))
        SectionLabel("접근성")
        Spacer(Modifier.height(6.dp))
        MCard(padding = PaddingValues(vertical = 4.dp)) {
            SettingRow(Icons.Rounded.AccessibilityNew, "접근성 설정", "글자 크기 · 읽어주는 속도 · 진동 · 시각 강조 · 캐릭터 보상", onAccessibility)
        }
        Spacer(Modifier.height(14.dp))
        SectionLabel("내 자료 관리")
        Spacer(Modifier.height(6.dp))
        MCard(padding = PaddingValues(vertical = 4.dp)) {
            SettingRow(Icons.Rounded.List, "개인표현 관리", "등록·수정·개별 삭제", onExpressions)
            HorizontalDivider(color = MColors.Line)
            SettingRow(Icons.Rounded.History, "교정 이력 관리", "개별 삭제 · 전체 초기화 · 기본 결과 복원", onCorrections)
        }
        Spacer(Modifier.height(14.dp))
        SectionLabel("사용자")
        Spacer(Modifier.height(6.dp))
        MCard(padding = PaddingValues(vertical = 4.dp)) {
            profiles.forEach { p ->
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(role = Role.Button) { if (p.id != currentProfileId) onSwitchProfile(p.id) }.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(if (p.id == currentProfileId) Icons.Rounded.Shield else Icons.Rounded.SwapHoriz, null, tint = if (p.id == currentProfileId) MColors.Violet else MColors.Muted)
                    Spacer(Modifier.width(12.dp))
                    Text(p.name, style = MaterialTheme.typography.bodyLarge, color = MColors.Ink, modifier = Modifier.weight(1f))
                    if (p.id == currentProfileId) Text("사용 중", style = MaterialTheme.typography.labelSmall, color = MColors.Violet)
                    TextButton(onClick = { rename = p }) { Text("이름 변경") }
                }
            }
            HorizontalDivider(color = MColors.Line)
            SettingRow(Icons.Rounded.PersonAdd, "사용자 추가", "표현·교정·설정을 사용자별로 분리해 저장해요") { addProfile = true }
        }
        Spacer(Modifier.height(14.dp))
        SectionLabel("안내")
        Spacer(Modifier.height(6.dp))
        MCard(padding = PaddingValues(vertical = 4.dp)) {
            SettingRow(Icons.Rounded.Shield, "개인정보 안내", "무엇을 저장하고 무엇을 저장하지 않는지") { policy = true }
            HorizontalDivider(color = MColors.Line)
            SettingRow(Icons.Rounded.QuestionAnswer, "서비스 사용법", "말해요 → 확인하고 고쳐요 → 맞으면 보내요", onReplayIntro)
            HorizontalDivider(color = MColors.Line)
            SettingRow(Icons.Rounded.Info, "AAC 대체 입력 안내", "말로 입력하기 어려울 때") { aac = true }
            HorizontalDivider(color = MColors.Line)
            SettingRow(Icons.Rounded.Mic, "마이크 권한", "기기 설정에서 바꿔요", onOpenAppSettings)
        }
        Spacer(Modifier.height(14.dp))
        SectionLabel("진단 정보 (기기 지표)")
        Spacer(Modifier.height(6.dp))
        MCard(color = MColors.SurfaceSoft) {
            MetricRow("앱 송수신량", "받음 ${formatBytes(metrics.rxBytes)} · 보냄 ${formatBytes(metrics.txBytes)}")
            MetricRow("음성인식 엔진", sttName)
            MetricRow("모델 상태", when (prepare) { is PrepareState.Ready -> "준비됨 · ${formatBytes(prepare.modelBytes)} · 불러오기 ${prepare.loadMs}ms"; is PrepareState.Failed -> "실패: ${prepare.message}"; is PrepareState.Installing -> "설치 중 ${prepare.filesDone}/${prepare.filesTotal}"; PrepareState.Loading -> "불러오는 중"; PrepareState.NotStarted -> "대기" })
            MetricRow("마지막 인식", if (metrics.lastProcessingMs != null) "처리 ${metrics.lastProcessingMs}ms / 발화 ${metrics.lastAudioMs}ms" else "아직 없음")
            MetricRow("발화 길이 이내 처리", if (metrics.sttRuns > 0) "${metrics.withinAudioLength}/${metrics.sttRuns}회" else "-")
            MetricRow("메모리(PSS)", "현재 ${formatBytes(metrics.currentPssKb * 1024)} · 최대 ${formatBytes(metrics.peakPssKb * 1024)}")
            MetricRow("TTS", when (tts) { is TtsState.Ready -> "${tts.engine} · ${tts.voice ?: "기본 음성"} · ${if (tts.offline) "오프라인 음성" else "오프라인 음성 미확인"}"; is TtsState.Unavailable -> "사용 불가: ${tts.reason}"; TtsState.Initializing -> "초기화 중" })
            Spacer(Modifier.height(6.dp))
            Text("이 앱은 인터넷 권한이 없어요. 비행기 모드에서도 같은 값을 확인할 수 있어요.", style = MaterialTheme.typography.bodySmall, color = MColors.Muted)
        }
        if (testFiles.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            SectionLabel("평가용 음원으로 인식 (files/testaudio)")
            Spacer(Modifier.height(6.dp))
            MCard(padding = PaddingValues(vertical = 4.dp)) {
                val running = evalState is kr.voicemate.malitda.ui.vm.SessionViewModel.EvalState.Running
                SettingRow(Icons.Rounded.History, if (running) "평가 실행 중… ${(evalState as kr.voicemate.malitda.ui.vm.SessionViewModel.EvalState.Running).done}/${evalState.total}" else "전체 음원 평가 실행", "CER·WER·완전일치·무응답·처리시간을 계산해 files/eval/*.csv 로 저장") { if (!running) onRunEval() }
                HorizontalDivider(color = MColors.Line)
                testFiles.forEach { f ->
                    SettingRow(Icons.Rounded.Mic, f, "마이크 대신 이 음원을 인식기에 넣어요") { onTestFile(f) }
                }
            }
        }
        when (val es = evalState) {
            is kr.voicemate.malitda.ui.vm.SessionViewModel.EvalState.Done -> EvalResultDialog(es.summary, onClearEval)
            is kr.voicemate.malitda.ui.vm.SessionViewModel.EvalState.Failed -> AlertDialog(onDismissRequest = onClearEval, title = { Text("평가 실패") }, text = { Text(es.message) }, confirmButton = { TextButton(onClick = onClearEval) { Text("확인") } })
            else -> {}
        }
        Spacer(Modifier.height(14.dp))
        TextAction("이 사용자 자료 전체 삭제", onClick = { deleteAll = true }, color = MColors.Danger)
        Spacer(Modifier.height(8.dp))
        Text("말잇다 · VOICE MATE · 2026 장애인 분야 해커톤", style = MaterialTheme.typography.labelSmall, color = MColors.Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }

    if (policy) AlertDialog(onDismissRequest = { policy = false }, title = { Text("개인정보 안내") }, text = { Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) { Text(PRIVACY_POLICY_TEXT, style = MaterialTheme.typography.bodySmall) } }, confirmButton = { TextButton(onClick = { policy = false }) { Text("확인") } })
    if (aac) AacDialog { aac = false }
    if (addProfile) NameDialog("사용자 추가", "", onDismiss = { addProfile = false }) { addProfile = false; onAddProfile(it) }
    rename?.let { p -> NameDialog("이름 변경", p.name, onDismiss = { rename = null }) { rename = null; onRenameProfile(p.id, it) } }
    if (deleteAll) AlertDialog(
        onDismissRequest = { deleteAll = false },
        title = { Text("이 사용자 자료를 모두 지울까요?") },
        text = { Text("현재 사용자의 개인표현·교정 이력·리워드 기록을 모두 지워요. 되돌릴 수 없어요.") },
        confirmButton = { TextButton(onClick = { deleteAll = false; onDeleteAll() }) { Text("모두 지우기", color = MColors.Danger) } },
        dismissButton = { TextButton(onClick = { deleteAll = false }) { Text("취소") } },
    )
}

@Composable
private fun EvalResultDialog(s: kr.voicemate.malitda.domain.EvalSummary, onDismiss: () -> Unit) {
    fun pct(d: Double?) = if (d == null) "-" else "%.1f%%".format(d * 100)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("평가 결과 · ${s.n}개 음원") },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                Text("엔진: ${s.engine}", style = MaterialTheme.typography.bodySmall, color = MColors.Muted)
                Spacer(Modifier.height(6.dp))
                MetricRow("평균 CER", pct(s.meanCer)); MetricRow("평균 WER", pct(s.meanWer))
                MetricRow("완전일치율", pct(s.exactRate)); MetricRow("Top-3 포함률", pct(s.top3Rate))
                MetricRow("무응답률", pct(s.noResultRate)); MetricRow("발화길이 내 처리", pct(s.withinAudioRate))
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MColors.Line)
                s.rows.forEach { r ->
                    Spacer(Modifier.height(6.dp))
                    Text(r.file, style = MaterialTheme.typography.labelMedium, color = MColors.Violet)
                    if (r.ref != null) Text("참조: ${r.ref}", style = MaterialTheme.typography.bodySmall, color = MColors.Ink2)
                    Text("인식: ${r.hyp.ifBlank { "(무응답)" }}", style = MaterialTheme.typography.bodySmall, color = MColors.Ink)
                    Text("CER ${pct(r.cer)} · WER ${pct(r.wer)} · ${if (r.exact) "일치" else if (r.top3) "Top-3" else "불일치"} · ${r.processingMs}ms/${r.audioMs}ms", style = MaterialTheme.typography.labelSmall, color = MColors.Muted)
                }
                if (s.csvPath != null) { Spacer(Modifier.height(8.dp)); Text("CSV: ${s.csvPath}", style = MaterialTheme.typography.labelSmall, color = MColors.Muted) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("확인") } },
    )
}

@Composable
private fun NameDialog(title: String, initial: String, onDismiss: () -> Unit, onOk: (String) -> Unit) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text(title) },
        text = { OutlinedTextField(value = name, onValueChange = { if (it.length <= 20) name = it }, singleLine = true, label = { Text("이름") }) },
        confirmButton = { TextButton(onClick = { onOk(name) }, enabled = name.isNotBlank()) { Text("확인") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, desc: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MColors.Violet, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MColors.Ink)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MColors.Muted)
        }
        Text("›", color = MColors.Muted, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun MetricRow(k: String, v: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(k, style = MaterialTheme.typography.labelMedium, color = MColors.Ink2, modifier = Modifier.width(120.dp))
        Text(v, style = MaterialTheme.typography.bodySmall, color = MColors.Ink, modifier = Modifier.weight(1f))
    }
}

/** S19 말친구 리워드 */
@Composable
fun RewardScreen(counters: CounterEntity?, expressionCount: Int, onFriend: (MalFriend) -> Unit, onHome: () -> Unit, onBack: () -> Unit) {
    val approvals = counters?.approvals ?: 0
    val unlocked = Friends.unlockedCount(approvals)
    ScreenScaffold(onBack = onBack, bottomBar = { GradientButton("홈으로", onClick = onHome) }) {
        GradientTitle("말친구 리워드")
        Spacer(Modifier.height(4.dp))
        Subtitle("보상 없이도 핵심 기능은 그대로 쓸 수 있어요.")
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("표현 등록", "${expressionCount}개", Modifier.weight(1f))
            StatCard("문장 승인", "${approvals}회", Modifier.weight(1f))
            StatCard("연속 사용", "${counters?.streakDays ?: 0}일", Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        SectionLabel("말친구 도감 · ${unlocked}/${Friends.all.size}")
        Spacer(Modifier.height(8.dp))
        Friends.all.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { f ->
                    val open = f.unlockAt <= approvals
                    MCard(modifier = Modifier.weight(1f), color = if (open) MColors.Surface else MColors.Lavender, onClick = { if (open) onFriend(f) }, contentDescription = if (open) "${f.name}, 대표 캐릭터로 설정" else "${f.name}, 승인 ${f.unlockAt}회에 만나요", padding = PaddingValues(12.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CharacterImage(f.drawable, 84.dp, modifier = Modifier.alpha(if (open) 1f else 0.25f))
                            Spacer(Modifier.height(6.dp))
                            Text(if (open) f.name else "?", style = MaterialTheme.typography.titleSmall, color = MColors.Ink)
                            Text(if (open) (if (counters?.mainCharacter == f.id) "대표 캐릭터" else "만났어요") else "승인 ${f.unlockAt}회", style = MaterialTheme.typography.labelSmall, color = MColors.Muted)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }
        InfoBox("진행 수치는 문장 내용이 아니라 횟수만 세요. 보상은 공유 승인과 별개예요.")
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    MCard(modifier = modifier, padding = PaddingValues(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MColors.Violet)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MColors.Muted)
        }
    }
}

/** S20 새로운 말친구를 만났어요 */
@Composable
fun NewFriendScreen(friend: MalFriend, onSetMain: () -> Unit, onLater: () -> Unit) {
    ScreenScaffold(bottomBar = { GradientButton("대표 캐릭터로 설정", onClick = onSetMain); TextAction("나중에 할게요", onClick = onLater) }) {
        GradientTitle("새로운 말친구를\n만났어요")
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { CharacterImage(friend.drawable, 220.dp, contentDescription = friend.name) }
        Spacer(Modifier.height(12.dp))
        Text(friend.name, style = MaterialTheme.typography.headlineMedium, color = MColors.Ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        InfoBox("보상·캐릭터 선택은 공유 승인과 별개의 기능이에요. 대표 캐릭터는 홈 화면에 표시돼요.")
    }
}
