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
import kr.voicemate.malitda.BuildConfig
import kr.voicemate.malitda.data.db.CounterEntity
import kr.voicemate.malitda.data.db.ExpressionEntity
import kr.voicemate.malitda.data.repo.ExpressionRepository
import kr.voicemate.malitda.data.settings.Settings
import kr.voicemate.malitda.data.settings.ListeningTimeLimit
import kr.voicemate.malitda.di.AppContainer
import kr.voicemate.malitda.domain.Approval
import kr.voicemate.malitda.domain.Candidate
import kr.voicemate.malitda.domain.CandidateBuilder
import kr.voicemate.malitda.domain.Category
import kr.voicemate.malitda.domain.Friends
import kr.voicemate.malitda.domain.MalFriend
import kr.voicemate.malitda.domain.MeaningResolver
import kr.voicemate.malitda.domain.MeaningDeliverySnapshot
import kr.voicemate.malitda.domain.StudentMeaningConfirmation
import kr.voicemate.malitda.stt.PrepareState
import kr.voicemate.malitda.stt.SttCallback
import kr.voicemate.malitda.stt.SttResult
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock

enum class SentenceSource { STT, EXPRESSION, DIRECT }
enum class ShareKind { SYSTEM, COPY, SMS }

sealed interface ListenState {
    data object Idle : ListenState
    data class Listening(
        val partial: String = "",
        val segments: String = "",
        val timeLimit: ListeningTimeLimit = ListeningTimeLimit.TWENTY_SECONDS,
        val isPreparing: Boolean = false,
    ) : ListenState
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
class SessionViewModel(val c: AppContainer, prepareSpeechOnInit: Boolean = true) : ViewModel() {
    /** A stored old pilot preference cannot opt a public QA/release build into collection. */
    val pilotToolsEnabled: Boolean = BuildConfig.DEBUG && BuildConfig.PILOT_TOOLS_ENABLED
    // Null means the first persisted snapshot has not arrived. Do not interpret
    // the initial in-memory defaults as a fresh consent decision on a cold start.
    val loadedSettings: StateFlow<Settings?> = c.settings.settings.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val settings: StateFlow<Settings> = loadedSettings.filterNotNull().stateIn(viewModelScope, SharingStarted.Eagerly, Settings())
    private val _profileId = MutableStateFlow(0L)
    val profileId: StateFlow<Long> = _profileId.asStateFlow()
    val profileName: StateFlow<String> = _profileId.flatMapLatest { id -> c.profiles.observe(id).map { it?.name ?: "" } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val counters: StateFlow<CounterEntity?> = _profileId.flatMapLatest { c.counters.observe(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val expressions: StateFlow<List<ExpressionEntity>> = _profileId.flatMapLatest { c.expressions.observeAll(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val recentExpressions: StateFlow<List<ExpressionEntity>> = _profileId.flatMapLatest { c.expressions.observeRecent(it, 3) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val expressionCount: StateFlow<Int> = _profileId.flatMapLatest { c.expressions.observeCount(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val corrections: StateFlow<List<kr.voicemate.malitda.data.db.CorrectionEntity>> = _profileId.flatMapLatest { c.corrections.observeAll(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
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
            // v2 기본 의미 4종(최초 1회). 이후 모두 지우면 U01-E(등록 없는 홈) 경로가 그대로 남는다.
            if (!c.settings.current().v2Seeded) { c.meanings.seedDefaults(_profileId.value); c.settings.setV2Seeded(true) }
            c.settings.settings.collect { s ->
                if (s.currentProfileId != 0L && s.currentProfileId != _profileId.value) {
                    // Clear the previous person's transient content before publishing the new owner.
                    dataEpoch++; resetSession()
                    _profileId.value = s.currentProfileId
                }
            }
        }
        if (prepareSpeechOnInit) viewModelScope.launch { c.stt.prepare(); syncModelMetrics() }
    }

    private fun syncModelMetrics() {
        (prepareState.value as? PrepareState.Ready)?.let { c.metrics.modelLoadMs = it.loadMs; c.metrics.modelBytes = it.modelBytes; c.metrics.refresh() }
    }

    private fun pulse(kind: kr.voicemate.malitda.data.feedback.FeedbackKind) { if (profilePreferences.value.haptics) c.feedback.pulse(kind) }

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
            refreshSttHints()
            val r = runCatching { java.io.File(c.filesDir, "testaudio/$name").inputStream().use { c.stt.recognizeWav(it) } }
            r.onSuccess { onSttFinal(it) }.onFailure { e ->
                _ui.update { it.copy(listen = ListenState.Idle) }
                _events.tryEmit(SessionEvent.GoSttError(e.message ?: "파일 인식 실패"))
            }
        }
    }

    // ---------- 말하기(S09) ----------
    private val mutationMutex = kotlinx.coroutines.sync.Mutex()
    private var dataEpoch = 0L
    private var listenGeneration = 0L
    private var listeningStarted = false
    private var literalRevision = 0L
    private var literalApproval: kr.voicemate.malitda.domain.LiteralApproval? = null
    private val _rememberedCorrection = MutableStateFlow<kr.voicemate.malitda.domain.CorrectionLearningSnapshot?>(null)
    val rememberedCorrection = _rememberedCorrection.asStateFlow()

    /** 인식기에 이 학생의 등록 표현(자주 쓰는 뜻 먼저)을 힌트로 준다. 듣기 시작할 때마다 새로 읽어 방금 연결한 표현도 들어간다. */
    private suspend fun refreshSttHints() {
        if (settings.value.trialLogging && collectionConfig().canCollect && collectionConfig().condition == "A") {
            c.stt.setHints(emptyList()); return
        }
        val hints = c.meanings.usableWithExpressions(_profileId.value)
            .filter { !collectionConfig().let { config -> config.canCollect && !config.practice } || !it.meaning.isSample }
            .sortedByDescending { it.meaning.useCount }
            .flatMap { e -> e.expressions.map { it.text } }
        c.stt.setHints(hints)
    }

    fun startListening() {
        if (_ui.value.listen !is ListenState.Idle) return
        val generation = ++listenGeneration
        val listenerProfile = _profileId.value
        listeningStarted = false
        _ui.update { it.copy(listen = ListenState.Listening(
            timeLimit = ListeningTimeLimit.fromSeconds(profilePreferences.value.listenTimeLimitSeconds),
            isPreparing = true,
        )) }
        viewModelScope.launch {
            // Read the actual owner's settings: the UI cache may still contain the previous profile.
            val preferences = try { c.settings.profilePreferences(listenerProfile).first() }
            catch (e: Exception) {
                ensureActive()
                if (generation == listenGeneration && listenerProfile == _profileId.value && _ui.value.listen is ListenState.Listening) {
                    _ui.update { it.copy(listen = ListenState.Idle) }
                    _events.tryEmit(SessionEvent.GoSttError("듣기 시간 설정을 불러오지 못했어요. 다시 시도해 주세요."))
                }
                return@launch
            }
            // An early stop/cancel, profile switch or session reset must not start the microphone later.
            if (generation != listenGeneration || listenerProfile != _profileId.value || _ui.value.listen !is ListenState.Listening) return@launch
            val timeLimit = ListeningTimeLimit.fromSeconds(preferences.listenTimeLimitSeconds)
            _ui.update { it.copy(listen = ListenState.Listening(timeLimit = timeLimit, isPreparing = true)) }
            beginListening(timeLimit, generation, listenerProfile)
        }
    }

    private fun beginListening(timeLimit: ListeningTimeLimit, generation: Long, listenerProfile: Long) {
        // 녹음이 끝나야 인식하므로, 녹음하는 동안 힌트를 채우면 된다.
        viewModelScope.launch { refreshSttHints() }
        pulse(kr.voicemate.malitda.data.feedback.FeedbackKind.START)
        val ok = c.stt.startListening(object : SttCallback {
            override fun onPartial(text: String) { if (generation != listenGeneration) return; _ui.update { s -> (s.listen as? ListenState.Listening)?.let { l -> s.copy(listen = l.copy(partial = text)) } ?: s } }
            override fun onSegment(text: String) { if (generation != listenGeneration) return; _ui.update { s -> (s.listen as? ListenState.Listening)?.let { l -> s.copy(listen = l.copy(segments = text, partial = "")) } ?: s } }
            override fun onFinal(result: SttResult) {
                if (generation != listenGeneration || listenerProfile != _profileId.value) return
                listeningStarted = false
                autoStopJob?.cancel(); autoStopJob = null
                _ui.update { state -> if (state.listen is ListenState.Listening) state.copy(listen = ListenState.Processing) else state }
                viewModelScope.launch { if (generation == listenGeneration && listenerProfile == _profileId.value) onSttFinal(result, generation) }
            }
            override fun onError(error: Throwable) {
                if (generation != listenGeneration || listenerProfile != _profileId.value) return
                listeningStarted = false
                autoStopJob?.cancel(); autoStopJob = null
                pulse(kr.voicemate.malitda.data.feedback.FeedbackKind.ERROR)
                _ui.update { it.copy(listen = ListenState.Idle) }
                _events.tryEmit(SessionEvent.GoSttError(error.message ?: "음성인식 오류"))
            }
        })
        if (!ok) {
            listeningStarted = false
            _ui.update { it.copy(listen = ListenState.Idle) }
            _events.tryEmit(SessionEvent.GoSttError("음성인식 엔진이 준비되지 않았어요"))
            return
        }
        // A synchronous callback can already have ended this attempt. Never restore its listening screen.
        if (generation != listenGeneration || listenerProfile != _profileId.value || _ui.value.listen !is ListenState.Listening) return
        listeningStarted = true
        _ui.update { state ->
            val listening = state.listen as? ListenState.Listening
            if (generation == listenGeneration && listenerProfile == _profileId.value && listening != null) {
                state.copy(listen = listening.copy(isPreparing = false))
            } else state
        }
        autoStopJob?.cancel()
        autoStopJob = null
        // Manual mode has no automatic deadline. Stop and cancel remain available on the listening screen.
        val autoStopDelayMillis = timeLimit.autoStopDelayMillis
        if (autoStopDelayMillis != null && generation == listenGeneration && _ui.value.listen is ListenState.Listening) {
            autoStopJob = viewModelScope.launch {
                kotlinx.coroutines.delay(autoStopDelayMillis)
                if (generation == listenGeneration && listenerProfile == _profileId.value) stopListening()
            }
        }
    }

    private var autoStopJob: kotlinx.coroutines.Job? = null

    fun stopListening() {
        autoStopJob?.cancel(); autoStopJob = null
        if (_ui.value.listen !is ListenState.Listening) return
        if (!listeningStarted) {
            // A settings read may still be pending. End this attempt without waiting for a nonexistent recording.
            listenGeneration++
            _ui.update { it.copy(listen = ListenState.Idle) }
            _events.tryEmit(SessionEvent.GoNoResult)
            return
        }
        listeningStarted = false
        _ui.update { it.copy(listen = ListenState.Processing) }
        c.stt.stop()
    }

    fun cancelListening() {
        autoStopJob?.cancel(); autoStopJob = null
        listenGeneration++
        listeningStarted = false
        c.stt.cancel()
        _ui.update { it.copy(listen = ListenState.Idle) }
    }

    internal suspend fun onSttFinal(result: SttResult, generation: Long = listenGeneration) {
        if (generation != listenGeneration) return
        autoStopJob?.cancel(); autoStopJob = null
        listeningStarted = false
        pulse(if (result.raw.isBlank()) kr.voicemate.malitda.data.feedback.FeedbackKind.ERROR else kr.voicemate.malitda.data.feedback.FeedbackKind.SUCCESS)
        c.metrics.onRecognition(result.processingMs, result.audioMs)
        if (result.raw.isBlank()) {
            _ui.update { it.copy(listen = ListenState.Idle, lastProcessingMs = result.processingMs, lastAudioMs = result.audioMs) }
            _events.tryEmit(SessionEvent.GoNoResult)
            return
        }
        val pid = _profileId.value
        val basicCondition = settings.value.trialLogging && collectionConfig().canCollect && collectionConfig().condition == "A"
        val exact = if (basicCondition) null else c.corrections.exactFor(pid, result.raw)?.approvedText
        val approved = if (basicCondition) emptySet() else c.corrections.approvedTextsNormalized(pid)
        val candidates = CandidateBuilder.build(result.raw, result.alternatives, exact, approved)
        if (generation != listenGeneration || pid != _profileId.value) return
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
    fun updateDraft(text: String) {
        literalRevision++; literalApproval = null
        _rememberedCorrection.value = null
        trial.onEdited(); syncTrial()
        _ui.update { it.copy(draft = text, approvalToken = null) }
    }

    fun beginLiteral(text: String = "") {
        cancelListening(); c.tts.stop(); v2ClearApproval()
        literalRevision++; literalApproval = null
        _rememberedCorrection.value = null
        _ui.update { it.copy(source = SentenceSource.DIRECT, draft = text, approvalToken = null) }
    }
    fun approveLiteral(): Boolean {
        val text = _ui.value.draft.trim()
        if (text.isBlank() || text.length > 200) return false
        _ui.update { it.copy(draft = text) }
        literalApproval = kr.voicemate.malitda.domain.LiteralApproval(_profileId.value, text, literalRevision)
        trial.onDelivered(-1L); syncTrial()
        return true
    }
    fun canDeliverLiteral(): Boolean = literalApproval?.permits(_profileId.value, _ui.value.draft, literalRevision) == true
    fun approvedLiteralText(): String? {
        val text = _ui.value.draft
        return text.takeIf { literalApproval?.permits(_profileId.value, it, literalRevision) == true }
    }
    fun revokeLiteral() { literalApproval = null; literalRevision++; _rememberedCorrection.value = null }

    /** Approval alone never persists a private utterance. The separate save dialog supplies this snapshot. */
    fun correctionLearningOffer(): kr.voicemate.malitda.domain.CorrectionLearningSnapshot? {
        val s = _ui.value; val settings = loadedSettings.value
        return kr.voicemate.malitda.domain.CorrectionLearning.prepare(literalApproval, _profileId.value,
            s.raw, s.draft, literalRevision,
            settings?.consentAccepted == true && settings.consentVersion == kr.voicemate.malitda.data.settings.SettingsStore.CURRENT_CONSENT_VERSION)
    }

    suspend fun rememberCorrection(request: kr.voicemate.malitda.domain.CorrectionLearningSnapshot): Boolean {
        val epoch = dataEpoch
        return mutationMutex.withLock {
            val current = c.settings.current()
            check(epoch == dataEpoch && current.currentProfileId == request.profileId &&
                kr.voicemate.malitda.domain.CorrectionLearning.isCurrent(request, literalApproval,
                    _profileId.value, _ui.value.raw, _ui.value.draft, literalRevision,
                    current.consentAccepted && current.consentVersion == kr.voicemate.malitda.data.settings.SettingsStore.CURRENT_CONSENT_VERSION)) {
                "확인한 문장이나 사용자가 바뀌었습니다. 다시 확인해야 합니다."
            }
            val saved = c.corrections.recordApproval(request.profileId, request.raw, request.approved)
            if (saved && correctionLearningOffer() == request) _rememberedCorrection.value = request
            saved
        }
    }

    suspend fun deleteSavedCorrection(profileId: Long, id: Long): Boolean = mutationMutex.withLock {
        val current = c.settings.current()
        check(profileId == _profileId.value && profileId == current.currentProfileId && current.consentAccepted) {
            "사용자가 바뀌었습니다. 다시 확인해야 합니다."
        }
        c.corrections.deleteForProfile(profileId, id)
    }

    suspend fun useSavedCorrection(shown: kr.voicemate.malitda.data.db.CorrectionEntity): Boolean {
        val pid = _profileId.value; val epoch = dataEpoch
        if (pid != shown.profileId) return false
        val fresh = c.corrections.exactFor(pid, shown.sourceRaw) ?: return false
        val current = c.settings.current()
        if (epoch != dataEpoch || pid != _profileId.value || current.currentProfileId != pid ||
            !current.consentAccepted || fresh.id != shown.id || fresh.approvedText != shown.approvedText) return false
        resetSession(); beginLiteral(fresh.approvedText)
        return true
    }
    fun pauseForBackground() { cancelListening(); stopSpeaking(); v2ClearApproval(); revokeLiteral(); endTrial(completed = false, reason = kr.voicemate.malitda.domain.PilotStopReason.BACKGROUND) }


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

    // ---------- 핵심표현(등록·목록) ----------
    fun saveExpression(id: Long?, category: Category, label: String, text: String, onResult: (ExpressionRepository.SaveResult) -> Unit = {}) {
        viewModelScope.launch {
            val r = c.expressions.save(_profileId.value, id, category, label, text)
            if (r is ExpressionRepository.SaveResult.Ok && (id == null || id == 0L)) c.counters.onExpressionAdded(_profileId.value)
            onResult(r)
        }
    }
    fun deleteExpression(id: Long) { viewModelScope.launch { c.expressions.delete(id) } }
    fun toggleFavorite(id: Long) { viewModelScope.launch { c.expressions.toggleFavorite(id) } }

    // ---------- 접근성 설정 ----------
    fun setFontScale(v: Float) { viewModelScope.launch { c.settings.setFontScale(v) } }
    fun setTtsRate(v: Float) { viewModelScope.launch { c.settings.setTtsRate(v) } }
    fun setHaptics(v: Boolean) { viewModelScope.launch { c.settings.setHaptics(v) } }
    fun setVisualEmphasis(v: Boolean) { viewModelScope.launch { c.settings.setVisualEmphasis(v) } }
    fun resetAccessibility() { viewModelScope.launch { c.settings.setFontScale(1f); c.settings.setTtsRate(1f); c.settings.setHaptics(true); c.settings.setVisualEmphasis(false) } }

    // ---------- 교정 이력 ----------
    fun deleteCorrection(id: Long) { viewModelScope.launch { c.corrections.delete(id) } }
    fun resetCorrections() { viewModelScope.launch { c.corrections.deleteAll(_profileId.value) } }

    // ---------- TTS ----------
    fun speak(text: String) {
        c.tts.stop()
        if (!c.tts.speak(text, profilePreferences.value.ttsRate)) _events.tryEmit(SessionEvent.GoTtsError)
    }
    fun stopSpeaking() = c.tts.stop()

    // ---------- v2 의미 흐름(U01~U06) ----------
    enum class V2Origin { VOICE, CARD }

    /** 선택된 뜻과 출처. snapshotUpdatedAt은 선택 당시 updatedAt — 달라지면 U03-V(등록 내용 변경). */
    data class V2State(
        val origin: V2Origin = V2Origin.CARD,
        val meaningId: Long? = null,
        val snapshotUpdatedAt: Long? = null,
        /** 음성 인식에서 나온 의미 후보 id 순위(상위가 먼저). */
        val matchIds: List<Long> = emptyList(),
        /** 이번 시도에서 앱이 들은 말. 학생이 카드로 뜻을 고치면 오인식 학습 제안에 쓴다. */
        val heard: String? = null,
        val deliveryApproval: MeaningDeliverySnapshot? = null,
        val selectionRevision: Long = 0,
        val context: String = "",
    )

    data class MeaningSnapshot(val profileId: Long, val items: List<kr.voicemate.malitda.data.db.MeaningEntity>?)

    /** null means loading, not an empty personal dictionary. The owner travels with the data. */
    val meaningSnapshot = _profileId.flatMapLatest { id ->
        c.meanings.observeUsable(id).map { MeaningSnapshot(id, it) }
            .onStart { emit(MeaningSnapshot(id, null)) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MeaningSnapshot(0L, null))
    val meanings: StateFlow<List<kr.voicemate.malitda.data.db.MeaningEntity>> = meaningSnapshot
        .map { it.items.orEmpty() }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _v2 = MutableStateFlow(V2State())
    val v2: StateFlow<V2State> = _v2.asStateFlow()

    val profilePreferences = _profileId.flatMapLatest { c.settings.profilePreferences(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, kr.voicemate.malitda.data.settings.ProfilePreferences())
    fun setProfilePreferences(p: kr.voicemate.malitda.data.settings.ProfilePreferences) {
        val pid = _profileId.value
        viewModelScope.launch { c.settings.setProfilePreferences(pid, p) }
    }
    fun setSituationContext(value: String) { _v2.update { it.copy(context = value, deliveryApproval = null, selectionRevision = it.selectionRevision + 1) } }
    fun v2ObserveMeaning(id: Long) = c.meanings.observe(id)

    /** 카드·목록에서 뜻을 고르면 호출. 선택 시점 updatedAt을 기억해 둔다. */
    fun v2SelectMeaning(m: kr.voicemate.malitda.data.db.MeaningEntity, origin: V2Origin) {
        _v2.update { it.copy(origin = origin, meaningId = m.id, snapshotUpdatedAt = m.updatedAt, deliveryApproval = null, selectionRevision = it.selectionRevision + 1) }
    }

    /** U03-V에서 '다시 뜻 확인하기': 최신 내용으로 스냅숏을 갱신한다. 뜻이 지워졌으면 false. */
    suspend fun v2RefreshSnapshot(): Boolean {
        val requested = _v2.value
        val pid = _profileId.value
        val id = requested.meaningId ?: return false
        val fresh = c.meanings.byId(id) ?: return false
        if (_v2.value != requested || _profileId.value != pid || fresh.profileId != pid) return false
        _v2.update { it.copy(snapshotUpdatedAt = fresh.updatedAt, deliveryApproval = null, selectionRevision = it.selectionRevision + 1) }
        return (fresh.confirmed || fresh.isSample) && fresh.active && fresh.profileId == _profileId.value
    }

    /** Suggest registered meanings, preserving raw speech; the person explicitly selects every candidate. */
    suspend fun v2ResolveFromStt(): List<MeaningResolver.Match> {
        val s = _ui.value
        val raw = s.raw ?: return emptyList()
        val before = _v2.value
        val pid = _profileId.value
        val registered = c.meanings.usableWithExpressions(pid).filter { entry ->
            !collectionConfig().let { it.canCollect && !it.practice } || !entry.meaning.isSample
        }
        val priorCorrections = c.corrections.approvedTextsNormalized(pid)
        val matches = MeaningResolver.resolve(raw, s.alternatives, registered,
            context = before.context.ifBlank { null }, maxCandidates = profilePreferences.value.candidateCount,
            fuzzyThreshold = if (profilePreferences.value.allowSimilarExpressions) 30 else 100,
            approvedTextsNormalized = priorCorrections)
        if (pid != _profileId.value || before != _v2.value || _ui.value.raw != raw) return emptyList()
        _v2.value = before.copy(origin = V2Origin.VOICE, meaningId = null, snapshotUpdatedAt = null,
            deliveryApproval = null, matchIds = matches.map { it.meaning.id }, heard = raw,
            selectionRevision = before.selectionRevision + 1)
        trial.onVoiceResult(matches.firstOrNull()?.meaning?.id); syncTrial()
        return matches
    }

    private fun deliverySnapshot(m: kr.voicemate.malitda.data.db.MeaningEntity) =
        MeaningDeliverySnapshot(m.profileId, m.id, m.updatedAt, m.text, m.imageKey)

    fun v2ClearApproval() { _v2.update { it.copy(deliveryApproval = null, selectionRevision = it.selectionRevision + 1) } }

    fun v2CanDeliver(m: kr.voicemate.malitda.data.db.MeaningEntity): Boolean =
        m.profileId == _profileId.value && m.active && (m.confirmed || m.isSample) &&
            _v2.value.deliveryApproval?.matches(deliverySnapshot(m)) == true

    /** 이번 선택만 승인하며 등록 내용이 바뀌면 다시 확인한다. */
    suspend fun v2ConfirmUse(shown: kr.voicemate.malitda.data.db.MeaningEntity): Boolean {
        val requested = _v2.value
        val fresh = c.meanings.byId(shown.id) ?: return false
        val state = _v2.value
        if (state != requested || fresh.profileId != _profileId.value || state.meaningId != fresh.id ||
            !fresh.active || !(fresh.confirmed || fresh.isSample) ||
            deliverySnapshot(fresh) != deliverySnapshot(shown) || state.snapshotUpdatedAt != fresh.updatedAt) return false
        _v2.update { it.copy(deliveryApproval = deliverySnapshot(fresh)) }
        trial.onDelivered(fresh.id, fresh.text, fresh.isSample); syncTrial()
        viewModelScope.launch { runCatching { c.meanings.recordUse(fresh.id) } }
        return true
    }

    /** 지원자 등록(U05): 미확인 상태로 저장하고 id를 돌려준다. 학생 확인(U10-C) 전에는 전달 흐름에 나오지 않는다. */
    suspend fun v2RegisterMeaning(expressions: List<String>, meaning: String, imageKey: String?, situation: String?, category: String = "often"): Long {
        val pid = _profileId.value; val epoch = dataEpoch
        return mutationMutex.withLock {
            check(pid == _profileId.value && epoch == dataEpoch) { "자료 상태가 바뀌었습니다. 다시 등록해야 합니다." }
            c.meanings.save(profileId = pid, text = meaning, imageKey = imageKey,
                situation = situation, expressions = expressions, confirmed = false, category = category)
        }
    }

    /** 등록·수정 전 중복 검사: 다른 뜻에 이미 붙은 표현 목록. */
    suspend fun v2FindConflicts(expressions: List<String>, excludeMeaningId: Long = 0L) =
        c.meanings.findConflicts(_profileId.value, expressions, excludeMeaningId)

    /** 학생 확인 완료(U10-C) → 확인·활성 모두 켠다(사용 중지 뜻의 재확인 포함). */
    suspend fun v2ActivateMeaning(id: Long, expectedUpdatedAt: Long, confirmation: StudentMeaningConfirmation): Boolean =
        confirmation.canActivate && c.meanings.activateConfirmed(id, _profileId.value, expectedUpdatedAt, confirmation.method!!.storageKey)

    suspend fun v2MeaningById(id: Long) = c.meanings.byId(id)

    /** 보관함(U11): 미확인·사용 중지 뜻. */
    val meaningsArchived: StateFlow<List<kr.voicemate.malitda.data.db.MeaningEntity>> =
        _profileId.flatMapLatest { c.meanings.observeArchived(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    suspend fun v2FirstExpression(id: Long): String =
        c.meanings.expressionsFor(id).firstOrNull()?.text.orEmpty()

    /** 연결된 표현 전체를 ", "로 이어 돌려준다(수정 프리필·상세 표시용). */
    suspend fun v2ExpressionsText(id: Long): String =
        c.meanings.expressionsFor(id).joinToString(", ") { it.text }

    /** 수정(U09-E): 표현 전체를 받은 목록으로 교체하고 미확인으로 되돌린다 — U10 재확인 필요. */
    suspend fun v2UpdateMeaning(id: Long, expressions: List<String>, meaning: String, imageKey: String?, situation: String?, category: String? = null) {
        val pid = _profileId.value; val epoch = dataEpoch
        mutationMutex.withLock {
            check(pid == _profileId.value && epoch == dataEpoch && c.meanings.byId(id)?.profileId == pid) { "학생이나 자료가 바뀌었습니다." }
            c.meanings.update(id, meaning, imageKey, situation, expressions, category)
            v2CleanupImages(setOfNotNull(imageKey))
        }
    }

    // ---------- v2 학생(프로필) 전환 ----------
    val profiles: StateFlow<List<kr.voicemate.malitda.data.db.ProfileEntity>> =
        c.profiles.all.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** 학생 전환: 진행 중이던 말·선택은 지운다(U07-S 안내문과 동일). */
    fun v2SwitchStudent(id: Long) {
        viewModelScope.launch { resetSession(); c.profiles.switchTo(id) }
    }

    // ---------- 학생별 그림체·R4 그림 만들기 ----------
    val artStyle: StateFlow<String> = _profileId.flatMapLatest { c.settings.artStyle(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, kr.voicemate.malitda.domain.ArtStyles.DEFAULT)

    fun setArtStyle(styleId: String) { viewModelScope.launch { c.settings.setArtStyle(_profileId.value, styleId) } }

    /**
     * 지금 학생 그림체로 뜻 그림을 만들어 앱 안에 저장하고 그림 키를 돌려준다.
     * 키가 든 지원자용 빌드는 앱이 직접 만들고(USB·PC 서버 불필요), 아니면 PC의 R4 서버를 거친다.
     */
    suspend fun r4GenerateMeaningImage(context: android.content.Context, meaning: String): String {
        val pid = _profileId.value
        val anon = c.settings.r4AnonId(pid)
        val requestedStyle = artStyle.value
        val bytes = if (kr.voicemate.malitda.data.ai.DirectImageClient.isAvailable) {
            kr.voicemate.malitda.data.ai.DirectImageClient.generate(context, requestedStyle, meaning)
        } else {
            kr.voicemate.malitda.data.ai.R4Client.generate(anon, requestedStyle, meaning)
        }
        kotlinx.coroutines.currentCoroutineContext().ensureActive()
        check(pid == _profileId.value) { "학생이 바뀌어 그림을 적용하지 않습니다." }
        return kr.voicemate.malitda.data.repo.MeaningImageStore.importBytes(context, bytes)
    }

    fun v2RenameStudent(name: String) { viewModelScope.launch { c.profiles.rename(_profileId.value, name) } }

    suspend fun v2RemoveSamples(): Int {
        val n = c.meanings.removeSamples(_profileId.value)
        v2CleanupImages()
        return n
    }

    /** 새 학생 추가 후 바로 전환. 새 학생의 의미 사전은 비어 있다(U01-E부터 시작). */
    fun v2AddStudent(name: String) {
        viewModelScope.launch { val id = c.profiles.add(name); resetSession(); c.profiles.switchTo(id) }
    }

    fun v2SetActive(id: Long, active: Boolean) { viewModelScope.launch { c.meanings.setActive(id, active) } }
    suspend fun v2DeleteMeaning(id: Long) {
        val pid = _profileId.value
        mutationMutex.withLock {
            check(c.meanings.byId(id)?.profileId == pid) { "현재 학생의 뜻을 확인해야 합니다." }
            c.meanings.delete(id); v2CleanupImages()
        }
    }

    /** 어떤 뜻도 참조하지 않는 사진 파일을 지운다(삭제·수정 뒤). */
    suspend fun v2CleanupImages(protectedKeys: Set<String> = emptySet()) =
        kr.voicemate.malitda.data.repo.MeaningImageStore.removeUnreferenced(c.filesDir, c.meanings.allImageKeys(), protectedKeys)

    fun v2Reset() { _v2.value = V2State(selectionRevision = _v2.value.selectionRevision + 1) }
    suspend fun v2RecordResponse(id: Long, version: Long, response: String, method: String? = null): Boolean =
        c.meanings.recordConfirmationResponse(id, _profileId.value, version, response, method)
    fun toggleMeaningFavorite(id: Long) { viewModelScope.launch { c.meanings.toggleFavorite(id) } }


    /** 새 말하기 시도를 시작할 때 이전에 들은 말을 지운다. */
    fun v2ClearHeard() { _v2.update { it.copy(heard = null) } }

    /** 오인식 학습: 방금 들은 말을 지금 뜻의 표현으로 붙인다. 성공하면 들은 말을 지워 제안을 닫는다. */
    suspend fun v2LearnHeard(): kr.voicemate.malitda.data.repo.MeaningRepository.AddExpressionResult? {
        val s = _v2.value
        val heard = s.heard ?: return null
        val id = s.meaningId ?: return null
        val r = c.meanings.addExpression(_profileId.value, id, heard)
        _v2.update { it.copy(heard = null) }
        return r
    }

    // ---------- 실증 기록 ----------
    private val trial = kr.voicemate.malitda.domain.TrialTracker()
    /** 전달 화면의 기록 입력(상대 이해·지원자 도움) 표시용. null이면 진행 중인 시도 없음. */
    private val _trialDraft = MutableStateFlow<kr.voicemate.malitda.domain.TrialTracker.Draft?>(null)
    val trialDraft: StateFlow<kr.voicemate.malitda.domain.TrialTracker.Draft?> = _trialDraft.asStateFlow()
    private fun syncTrial() { _trialDraft.value = trial.draft }

    val trialLogs: StateFlow<List<kr.voicemate.malitda.data.db.TrialLogEntity>> =
        _profileId.flatMapLatest { c.trialLogs.observeAll(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val pilotState = _profileId.flatMapLatest { pid -> c.pilot.observe(pid).map { pid to it } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L to kr.voicemate.malitda.domain.PilotConfig())
    val pilotConfig = pilotState.map { it.second }
        .stateIn(viewModelScope, SharingStarted.Eagerly, kr.voicemate.malitda.domain.PilotConfig())
    private val trialMutex = kotlinx.coroutines.sync.Mutex()

    fun trialStartVoice() {
        if (!pilotToolsEnabled) { trial.discard(); syncTrial(); return }
        if (trial.draft?.origin == kr.voicemate.malitda.domain.TrialTracker.Origin.VOICE && trial.draft?.delivered == false) trial.onRetry()
        else {
            endTrial(false)
            trial.startNew(kr.voicemate.malitda.domain.TrialTracker.Origin.VOICE, System.currentTimeMillis(), collectionConfig())
        }
        syncTrial()
    }
    fun trialStartCard() {
        if (!pilotToolsEnabled) { trial.discard(); syncTrial(); return }
        endTrial(false)
        trial.startNew(kr.voicemate.malitda.domain.TrialTracker.Origin.CARD, System.currentTimeMillis(), collectionConfig())
        syncTrial()
    }
    fun trialStartText() {
        if (!pilotToolsEnabled) { trial.discard(); syncTrial(); return }
        if (trial.draft == null) trial.startNew(kr.voicemate.malitda.domain.TrialTracker.Origin.TEXT, System.currentTimeMillis(), collectionConfig())
        syncTrial()
    }
    private fun collectionConfig() = pilotState.value.let { (pid, config) ->
        val details = config.deviceContext.substringBefore('|') + "|" + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL +
            "|Android " + android.os.Build.VERSION.RELEASE + " (" + android.os.Build.VERSION.SDK_INT + ")|ASR=" + settings.value.sttEngine + "|network=not_required"
        if (pilotToolsEnabled && settings.value.trialLogging && pid == _profileId.value) config.copy(deviceContext = details) else config.copy(consented = false)
    }
    fun trialCardPicked() { trial.onCardPicked(); syncTrial() }
    fun trialOtherMeaning() { trial.onOtherMeaning(); syncTrial() }
    fun trialSetJudgedCorrect(v: Boolean?) { trial.setJudgedCorrect(v); syncTrial() }
    fun trialSetUnderstood(v: Boolean?) { trial.setUnderstood(v); syncTrial() }
    fun trialSetHelped(v: Boolean) { trial.setSupporterHelped(v); syncTrial() }
    fun trialSetFeedback(v: kr.voicemate.malitda.domain.PilotFeedback) {
        trial.setFeedback(v); syncTrial()
        if (v.fatigue >= 4) pausePilot(kr.voicemate.malitda.domain.PilotStopReason.FATIGUE)
    }
    fun trialAccidentalSendObserved() { trial.onAccidentalSend(); syncTrial(); pausePilot(kr.voicemate.malitda.domain.PilotStopReason.SAFETY) }

    private fun endTrial(completed: Boolean, reason: kr.voicemate.malitda.domain.PilotStopReason = kr.voicemate.malitda.domain.PilotStopReason.CANCELLED) {
        val consentedAt = trial.draft?.config?.consentedAt
        val log = if (completed) trial.finish(_profileId.value, System.currentTimeMillis())
                  else trial.cancel(_profileId.value, System.currentTimeMillis(), reason)
        syncTrial()
        if (!pilotToolsEnabled || log == null || !log.consented || !settings.value.trialLogging) return
        viewModelScope.launch { trialMutex.lock(); try {
            val current = c.pilot.get(log.profileId)
            if (current.canCollect && current.participantCode == log.participantCode && current.consentedAt == consentedAt && settings.value.trialLogging) {
                if (!log.practice && log.outcome == "COMPLETED") {
                    val success = kr.voicemate.malitda.domain.PilotPlan.observedSuccess(log.understood, log.judgedCorrect)
                    val updated = if (success != null) kr.voicemate.malitda.domain.PilotPlan.afterCompleted(current, success)
                        else current.copy(ordinal = current.ordinal + 1)
                    c.trialLogs.insert(if (updated.paused && !current.paused)
                        log.copy(stopReason = kr.voicemate.malitda.domain.PilotStopReason.THREE_FAILURES.name) else log)
                    c.pilot.update(log.profileId, updated)
                    if (updated.paused) _events.tryEmit(SessionEvent.Toast("실증을 잠시 멈춥니다. 피로와 어려움을 확인한 뒤 지원자가 재개합니다."))
                } else c.trialLogs.insert(log)
            }
        } finally { trialMutex.unlock() } }
    }
    private fun finishTrial() { endTrial(false) }
    fun completePilotTask() { endTrial(true) }
    suspend fun configurePilot(config: kr.voicemate.malitda.domain.PilotConfig) {
        check(pilotToolsEnabled) { "평가 도구는 별도 비공개 개발 빌드에서만 사용합니다." }
        val pid = _profileId.value
        config.targetMeaningId?.let { id ->
            val m = c.meanings.byId(id)
            require(m?.profileId == pid && m.active && (m.confirmed || m.isSample)) { "현재 학생의 사용할 수 있는 뜻을 선택해야 합니다." }
            require(config.practice || (m != null && !m.isSample && m.confirmed)) { "본 실증에는 예시가 아닌 본인과 확인한 개인 뜻을 사용해야 합니다." }
        }
        trial.discard(); syncTrial()
        c.pilot.update(pid, config)
        c.settings.setTrialLogging(config.canCollect)
    }
    fun pausePilot(reason: kr.voicemate.malitda.domain.PilotStopReason = kr.voicemate.malitda.domain.PilotStopReason.PARTICIPANT_REQUEST) {
        cancelListening(); stopSpeaking(); v2ClearApproval(); revokeLiteral()
        val pid = _profileId.value
        endTrial(false, reason)
        viewModelScope.launch { trialMutex.withLock {
            c.pilot.update(pid, c.pilot.get(pid).copy(paused = true))
        } }
    }
    fun resumePilot() {
        if (!pilotToolsEnabled) return
        val pid = _profileId.value
        viewModelScope.launch { trialMutex.withLock { c.pilot.update(pid, c.pilot.get(pid).copy(paused = false, consecutiveFailures = 0)) } }
    }
    suspend fun withdrawPilot() {
        val pid = _profileId.value
        trial.discard(); syncTrial()
        trialMutex.withLock {
            c.settings.setTrialLogging(false); c.pilot.withdraw(pid); c.trialLogs.deleteAll(pid); c.dataManagement.clearTrialExportCache()
        }
    }
    fun setTrialLogging(v: Boolean) { viewModelScope.launch {
        if (v && !pilotToolsEnabled) return@launch
        if (!v) { finishTrial(); c.settings.setTrialLogging(false) }
        else if (pilotConfig.value.canCollect) c.settings.setTrialLogging(true)
        else _events.tryEmit(SessionEvent.Toast("실증 참여 코드와 별도 동의를 먼저 준비해야 합니다."))
    } }
    suspend fun deleteCurrentTrialRecords() {
        val pid = _profileId.value
        trial.discard(); syncTrial()
        trialMutex.withLock { c.trialLogs.deleteAll(pid); c.dataManagement.clearTrialExportCache() }
    }
    fun clearTrialLogs() { viewModelScope.launch { runCatching { deleteCurrentTrialRecords() }.onFailure { _events.tryEmit(SessionEvent.Toast("실증 기록과 임시 CSV 정리를 마치지 못했습니다. 다시 확인해야 합니다.")) } } }
    fun trialExportScope() = kr.voicemate.malitda.domain.TrialExportScope(_profileId.value, pilotConfig.value.participantCode, pilotToolsEnabled && pilotConfig.value.hasConsent && pilotState.value.first == _profileId.value)
    suspend fun trialCsv(): String {
        check(pilotToolsEnabled) { "평가 도구는 별도 비공개 개발 빌드에서만 사용합니다." }
        val pid = _profileId.value
        return trialMutex.withLock {
            val current = c.pilot.get(pid)
            require(current.hasConsent) { "동의 상태를 다시 확인해야 합니다." }
            val logs = c.trialLogs.observeAll(pid).first()
            val latest = c.pilot.get(pid)
            check(pid == _profileId.value && latest.hasConsent && current.consentedAt == latest.consentedAt && current.participantCode == latest.participantCode) { "참여자나 동의 상태가 바뀌었습니다." }
            kr.voicemate.malitda.domain.TrialCsv.of(logs, kr.voicemate.malitda.domain.TrialExportScope(pid, latest.participantCode, true))
        }
    }
    suspend fun wipeAllData() {
        dataEpoch++
        resetSession(); trial.discard(); syncTrial()
        mutationMutex.lock(); trialMutex.lock(); try {
            c.dataManagement.wipeAll(); c.pilot.clearAll(); c.settings.clearAll()
            _profileId.value = 0L
            val id = c.profiles.ensureCurrent()
            _profileId.value = id
            c.settings.setV2Seeded(true) // reset must not resurrect sample cards
        } finally { trialMutex.unlock(); mutationMutex.unlock() }
    }
    suspend fun resetPersonalRanking() {
        val pid = _profileId.value
        resetSession(); mutationMutex.withLock { c.dataManagement.resetRanking(pid) }
    }
    suspend fun dataCounts(): Map<String, Int> {
        val pid = _profileId.value
        val all = c.db.meaningDao().allFor(pid)
        val aliases = all.sumOf { c.meanings.expressionsFor(it.id).size }
        val confirmations = all.sumOf { c.meanings.confirmationHistory(it.id).size }
        val result = mapOf("meanings" to all.size, "aliases" to aliases,
            "confirmations" to confirmations, "corrections" to c.db.correctionDao().all(pid).size,
            "expressions" to c.expressions.observeAll(pid).first().size,
            "trials" to c.trialLogs.observeAll(pid).first().size,
            "photos" to all.mapNotNull { it.imageKey?.takeIf { key -> key.startsWith("file:") } }.toSet().size)
        check(pid == _profileId.value) { "학생이 바뀌었습니다. 자료 현황을 다시 확인합니다." }
        return result
    }
    suspend fun clearMeaningDictionary() {
        val pid = _profileId.value
        dataEpoch++; resetSession()
        mutationMutex.withLock { trialMutex.withLock {
            c.pilot.update(pid, c.pilot.get(pid).copy(paused = true, targetMeaningId = null, taskTargets = emptyMap()))
            c.dataManagement.clearMeanings(pid)
        } }
    }


    // ---------- 세션 정리 ----------
    fun resetSession() {
        finishTrial()
        cancelListening(); c.tts.stop(); revokeLiteral()
        _ui.value = SessionUiState()
        _v2.value = V2State(selectionRevision = _v2.value.selectionRevision + 1)
    }

    /** 문장은 남기되 인식 결과만 정리(다시 말하기 진입 시). */
    fun prepareRespeak() { cancelListening(); c.tts.stop(); v2Reset(); revokeLiteral(); _ui.update { it.copy(listen = ListenState.Idle, raw = null, alternatives = emptyList(), candidates = emptyList(), approvalToken = null) } }

    override fun onCleared() { cancelListening(); c.tts.stop() }

    companion object {
        fun factory(c: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T = SessionViewModel(c) as T
        }
    }
}
