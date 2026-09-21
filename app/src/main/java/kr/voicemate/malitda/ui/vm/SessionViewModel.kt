package kr.voicemate.malitda.ui.vm

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kr.voicemate.malitda.data.db.CounterEntity
import kr.voicemate.malitda.data.settings.Settings
import kr.voicemate.malitda.di.AppContainer
import kr.voicemate.malitda.domain.Approval
import kr.voicemate.malitda.domain.Candidate
import kr.voicemate.malitda.domain.CandidateBuilder
import kr.voicemate.malitda.domain.Friends
import kr.voicemate.malitda.domain.MalFriend
import kr.voicemate.malitda.stt.PrepareState
import kr.voicemate.malitda.stt.SttCallback
import kr.voicemate.malitda.stt.SttResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SentenceSource { STT, EXPRESSION, DIRECT }
enum class ShareKind { SYSTEM, COPY, SMS }

sealed interface ListenState {
    data object Idle : ListenState
    data class Listening(val partial: String = "", val segments: String = "") : ListenState
    data object Processing : ListenState
}

data class SessionUiState(
    val listen: ListenState = ListenState.Idle,
    val source: SentenceSource = SentenceSource.DIRECT,
    /** STT 원문(불변). null이면 음성 입력이 아닌 경로. */
    val raw: String? = null,
    val alternatives: List<String> = emptyList(),
    val candidates: List<Candidate> = emptyList(),
    val selectedIndex: Int? = null,
    /** 사용자가 확인·수정 중인 문장. */
    val draft: String = "",
    val approvalToken: String? = null,
    val lastProcessingMs: Long? = null,
    val lastAudioMs: Long? = null,
    val m1Applied: Boolean = false,
) {
    val isApproved: Boolean get() = Approval.isApproved(approvalToken, draft)
    val selected: Candidate? get() = selectedIndex?.let { candidates.getOrNull(it) }
}

sealed interface SessionEvent {
    data object GoResult : SessionEvent
    data object GoNoResult : SessionEvent
    data class GoSttError(val message: String) : SessionEvent
    data object GoConfirm : SessionEvent
    data object GoApprove : SessionEvent
    data object GoAfterShare : SessionEvent
    data object GoTtsError : SessionEvent
    data class NewFriend(val friend: MalFriend) : SessionEvent
    data class Toast(val message: String) : SessionEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModel(val c: AppContainer) : ViewModel() {
    val settings: StateFlow<Settings> = c.settings.settings.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())
    private val _profileId = MutableStateFlow(0L)
    val profileId: StateFlow<Long> = _profileId.asStateFlow()
    val profileName: StateFlow<String> = _profileId.flatMapLatest { id -> c.profiles.observe(id).map { it?.name ?: "" } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val counters: StateFlow<CounterEntity?> = _profileId.flatMapLatest { c.counters.observe(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val prepareState: StateFlow<PrepareState> = c.stt.prepareState
    val ttsState = c.tts.state
    val ttsProgress = c.tts.progress
    val ttsSpeaking = c.tts.speaking
    val metrics = c.metrics.snapshot

    private val _ui = MutableStateFlow(SessionUiState())
    val ui: StateFlow<SessionUiState> = _ui.asStateFlow()
    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 16)
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            _profileId.value = c.profiles.ensureCurrent()
            c.settings.settings.collect { s -> if (s.currentProfileId != 0L && s.currentProfileId != _profileId.value) _profileId.value = s.currentProfileId }
        }
        viewModelScope.launch { c.stt.prepare(); syncModelMetrics() }
    }

    private fun syncModelMetrics() {
        (prepareState.value as? PrepareState.Ready)?.let { c.metrics.modelLoadMs = it.loadMs; c.metrics.modelBytes = it.modelBytes; c.metrics.refresh() }
    }

    fun retryPrepare() { viewModelScope.launch { c.stt.prepare(); syncModelMetrics() } }

    /** 음성인식 엔진 전환(사전 비교용). 진행 중인 세션은 정리한다. */
    val selectedEngine: StateFlow<String> get() = c.sttRouter.selected
    fun switchEngine(id: String) {
        viewModelScope.launch {
            resetSession()
            c.settings.setSttEngine(id)
            c.sttRouter.switchTo(id)
            syncModelMetrics()
        }
    }

    /** 평가·개발용: filesDir/testaudio 안의 WAV를 마이크 대신 인식기에 넣는다(같은 후보·M1·승인 흐름을 탄다). */
    fun testAudioFiles(): List<String> =
        java.io.File(c.filesDir, "testaudio").listFiles()?.filter { it.isFile && it.name.endsWith(".wav", true) }?.map { it.name }?.sorted() ?: emptyList()

    // ---------- 평가 모드: 같은 음원을 같은 엔진·정리 규칙으로 일괄 인식해 기술지표를 낸다 ----------
    sealed interface EvalState {
        data class Running(val done: Int, val total: Int) : EvalState
        data class Done(val summary: kr.voicemate.malitda.domain.EvalSummary) : EvalState
        data class Failed(val message: String) : EvalState
    }
    private val _eval = MutableStateFlow<EvalState?>(null)
    val eval: StateFlow<EvalState?> = _eval.asStateFlow()
    fun clearEval() { _eval.value = null }

    /** files/testaudio/refs.txt: `파일명<TAB>참조문` 한 줄씩. 없으면 CER/WER 없이 무응답·처리시간만 낸다. */
    fun runEvaluation() {
        if (_eval.value is EvalState.Running) return
        val files = testAudioFiles()
        if (files.isEmpty()) { _eval.value = EvalState.Failed("files/testaudio 에 WAV가 없어요"); return }
        viewModelScope.launch {
            try {
                val dir = java.io.File(c.filesDir, "testaudio")
                val refs = java.io.File(dir, "refs.txt").takeIf { it.exists() }?.readLines()
                    ?.mapNotNull { l -> l.split('\t', limit = 2).takeIf { it.size == 2 }?.let { it[0].trim() to it[1].trim() } }?.toMap() ?: emptyMap()
                val rows = ArrayList<kr.voicemate.malitda.domain.EvalRow>()
                files.forEachIndexed { i, f ->
                    _eval.value = EvalState.Running(i, files.size)
                    val r = java.io.File(dir, f).inputStream().use { c.stt.recognizeWav(it) }
                    c.metrics.onRecognition(r.processingMs, r.audioMs)
                    val ref = refs[f]
                    val top3 = ref != null && r.alternatives.take(3).any { kr.voicemate.malitda.domain.EvalMetrics.exact(ref, it) }
                    rows += kr.voicemate.malitda.domain.EvalRow(
                        file = f, ref = ref, hyp = r.raw, alternatives = r.alternatives,
                        cer = ref?.let { kr.voicemate.malitda.domain.EvalMetrics.cer(it, r.raw) },
                        wer = ref?.let { kr.voicemate.malitda.domain.EvalMetrics.wer(it, r.raw) },
                        exact = ref != null && kr.voicemate.malitda.domain.EvalMetrics.exact(ref, r.raw),
                        top3 = top3, noResult = r.raw.isBlank(), processingMs = r.processingMs, audioMs = r.audioMs,
                    )
                }
                val outDir = java.io.File(c.filesDir, "eval").apply { mkdirs() }
                val stamp = java.text.SimpleDateFormat("yyyyMMdd-HHmmss", java.util.Locale.US).format(java.util.Date())
                val out = java.io.File(outDir, "eval-$stamp.csv")
                val summary = kr.voicemate.malitda.domain.EvalSummary(rows, c.stt.name, out.absolutePath)
                out.writeText(summary.toCsv())
                _eval.value = EvalState.Done(summary)
            } catch (e: Throwable) {
                _eval.value = EvalState.Failed(e.message ?: e.javaClass.simpleName)
            }
        }
    }

    fun recognizeTestFile(name: String) {
        if (_ui.value.listen !is ListenState.Idle) return
        _ui.update { it.copy(listen = ListenState.Processing) }
        viewModelScope.launch {
            val r = runCatching { java.io.File(c.filesDir, "testaudio/$name").inputStream().use { c.stt.recognizeWav(it) } }
            r.onSuccess { onSttFinal(it) }.onFailure { e ->
                _ui.update { it.copy(listen = ListenState.Idle) }
                _events.tryEmit(SessionEvent.GoSttError(e.message ?: "파일 인식 실패"))
            }
        }
    }

    // ---------- 말하기(S09) ----------
    fun startListening() {
        if (_ui.value.listen !is ListenState.Idle) return
        _ui.update { it.copy(listen = ListenState.Listening()) }
        val ok = c.stt.startListening(object : SttCallback {
            override fun onPartial(text: String) { _ui.update { s -> (s.listen as? ListenState.Listening)?.let { l -> s.copy(listen = l.copy(partial = text)) } ?: s } }
            override fun onSegment(text: String) { _ui.update { s -> (s.listen as? ListenState.Listening)?.let { l -> s.copy(listen = l.copy(segments = text, partial = "")) } ?: s } }
            override fun onFinal(result: SttResult) { viewModelScope.launch { onSttFinal(result) } }
            override fun onError(error: Throwable) {
                _ui.update { it.copy(listen = ListenState.Idle) }
                _events.tryEmit(SessionEvent.GoSttError(error.message ?: "음성인식 오류"))
            }
        })
        if (!ok) {
            _ui.update { it.copy(listen = ListenState.Idle) }
            _events.tryEmit(SessionEvent.GoSttError("음성인식 엔진이 준비되지 않았어요"))
        }
    }

    fun stopListening() {
        if (_ui.value.listen !is ListenState.Listening) return
        _ui.update { it.copy(listen = ListenState.Processing) }
        c.stt.stop()
    }

    fun cancelListening() {
        c.stt.cancel()
        _ui.update { it.copy(listen = ListenState.Idle) }
    }

    private suspend fun onSttFinal(result: SttResult) {
        c.metrics.onRecognition(result.processingMs, result.audioMs)
        if (result.raw.isBlank()) {
            _ui.update { it.copy(listen = ListenState.Idle, lastProcessingMs = result.processingMs, lastAudioMs = result.audioMs) }
            _events.tryEmit(SessionEvent.GoNoResult)
            return
        }
        val pid = _profileId.value
        val exact = c.corrections.exactFor(pid, result.raw)?.approvedText
        val approved = c.corrections.approvedTextsNormalized(pid)
        val candidates = CandidateBuilder.build(result.raw, result.alternatives, exact, approved)
        _ui.update {
            it.copy(
                listen = ListenState.Idle, source = SentenceSource.STT, raw = result.raw, alternatives = result.alternatives,
                candidates = candidates, selectedIndex = null, draft = "", approvalToken = null,
                lastProcessingMs = result.processingMs, lastAudioMs = result.audioMs, m1Applied = candidates.any { c -> c.isM1 },
            )
        }
        _events.tryEmit(SessionEvent.GoResult)
    }

    // ---------- 후보 선택(S10) ----------
    fun selectCandidate(index: Int) { _ui.update { it.copy(selectedIndex = index) } }

    fun proceedWithSelection() {
        val s = _ui.value; val sel = s.selected ?: return
        _ui.update { it.copy(draft = sel.text, approvalToken = null) }
        _events.tryEmit(SessionEvent.GoConfirm)
    }

    /** '직접 수정': 선택이 없으면 원문으로 시작. */
    fun startDirectEdit() {
        val s = _ui.value
        val base = s.selected?.text ?: s.raw ?: s.draft
        _ui.update { it.copy(draft = base, approvalToken = null) }
        _events.tryEmit(SessionEvent.GoConfirm)
    }

    // ---------- 확인·수정(S11) ----------
    fun updateDraft(text: String) { _ui.update { it.copy(draft = text) } }

    fun useExpression(text: String) {
        _ui.update { it.copy(source = SentenceSource.EXPRESSION, raw = null, alternatives = emptyList(), candidates = emptyList(), selectedIndex = null, draft = text, approvalToken = null, m1Applied = false) }
        _events.tryEmit(SessionEvent.GoConfirm)
    }

    /** 직접 입력 경로(S17·S23 등): 작성 중이던 문장은 유지. */
    fun startDirectInput() {
        _ui.update { if (it.raw == null) it.copy(source = SentenceSource.DIRECT) else it }
        _events.tryEmit(SessionEvent.GoConfirm)
    }

    fun proceedToApprove() {
        if (_ui.value.draft.isBlank()) return
        _events.tryEmit(SessionEvent.GoApprove)
    }

    // ---------- 승인(S12→S21) ----------
    fun approve() {
        val s = _ui.value
        val text = s.draft.trim()
        if (text.isEmpty()) return
        _ui.update { it.copy(approvalToken = Approval.token(text)) }
        viewModelScope.launch {
            val pid = _profileId.value
            if (s.source == SentenceSource.STT && s.raw != null) {
                c.corrections.recordApproval(pid, s.raw, text)
                if (s.selected?.isM1 == true) c.corrections.markUsed(pid, s.raw)
            }
            val counters = c.counters.onApproval(pid)
            val unlocked = Friends.unlockedCount(counters.approvals)
            if (settings.value.rewardEnabled && unlocked > counters.friendsSeen) {
                c.counters.setFriendsSeen(pid, unlocked)
                _events.tryEmit(SessionEvent.NewFriend(Friends.all[unlocked - 1]))
            }
        }
    }

    fun unapprove() { _ui.update { it.copy(approvalToken = null) } }

    // ---------- 공유(S21) ----------
    fun share(kind: ShareKind, context: Context) {
        val s = _ui.value
        if (!s.isApproved) { _events.tryEmit(SessionEvent.Toast("먼저 문장을 확인해 주세요")); return }
        val text = s.draft.trim()
        val launched = runCatching {
            when (kind) {
                ShareKind.SYSTEM -> {
                    val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
                    context.startActivity(Intent.createChooser(send, "공유할 앱 선택").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
                ShareKind.COPY -> {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("말잇다", text))
                    if (Build.VERSION.SDK_INT < 33) Toast.makeText(context, "복사했어요", Toast.LENGTH_SHORT).show()
                }
                ShareKind.SMS -> {
                    val sms = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")).apply { putExtra("sms_body", text); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    if (sms.resolveActivity(context.packageManager) != null) context.startActivity(sms)
                    else {
                        val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
                        context.startActivity(Intent.createChooser(send, "메시지 앱 선택").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                }
            }
        }.isSuccess
        if (!launched) { _events.tryEmit(SessionEvent.Toast("공유할 수 있는 앱이 없어요")); return }
        viewModelScope.launch { c.counters.onShare(_profileId.value) }
        _events.tryEmit(SessionEvent.GoAfterShare)
    }

    // ---------- TTS ----------
    fun speak(text: String) {
        c.tts.stop()
        if (!c.tts.speak(text, settings.value.ttsRate)) _events.tryEmit(SessionEvent.GoTtsError)
    }
    fun stopSpeaking() = c.tts.stop()

    // ---------- 세션 정리 ----------
    fun resetSession() {
        c.stt.cancel(); c.tts.stop()
        _ui.value = SessionUiState()
    }

    /** 문장은 남기되 인식 결과만 정리(다시 말하기 진입 시). */
    fun prepareRespeak() { c.tts.stop(); _ui.update { it.copy(listen = ListenState.Idle, approvalToken = null) } }

    override fun onCleared() { c.stt.cancel(); c.tts.stop() }

    companion object {
        fun factory(c: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T = SessionViewModel(c) as T
        }
    }
}
