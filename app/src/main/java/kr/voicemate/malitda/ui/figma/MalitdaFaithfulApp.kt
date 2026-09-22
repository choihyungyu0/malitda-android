package kr.voicemate.malitda.ui.figma

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.voicemate.malitda.data.repo.ExpressionRepository
import kr.voicemate.malitda.domain.Category
import kr.voicemate.malitda.ui.vm.ListenState
import kr.voicemate.malitda.ui.vm.SessionEvent
import kr.voicemate.malitda.ui.vm.SessionViewModel
import kr.voicemate.malitda.ui.vm.ShareKind

/** 뒤로가기 이전 화면 맵(S07·S09는 별도 처리). */
private val BACK_MAP = mapOf(
    "S02" to "S01", "S03" to "S02",
    "S10" to "S08", "S11" to "S10", "S12" to "S11", "S17" to "S08",
    "S13" to "S08", "S14" to "S13", "S05" to "S08", "S06" to "S05",
    "S15" to "S08", "S16" to "S08", "S18" to "S08", "S19" to "S08",
    "S20" to "S12", "S04" to "S18",
    "S22" to "S15", "S23" to "S08", "S24" to "S08", "S25" to "S08", "S26" to "S12", "S27" to "S14",
)

/**
 * 구현정본 네이티브 화면들을 잇는 앱 호스트.
 * 온보딩(S01~S03) → 홈(S08) → 말하기 흐름(S09~S12/S21), 핵심표현 등록(S05~S07)·목록(S13)·상세(S14).
 * SessionViewModel의 실제 로직·이벤트로 내비게이션한다. 에뮬(마이크 없음)은 평가음원으로 같은 흐름을 탄다.
 */
@Composable
fun MalitdaFaithfulApp(vm: SessionViewModel, start: String = "S01") {
    val ctx = LocalContext.current
    var route by rememberSaveable { mutableStateOf(start) }
    var notice by rememberSaveable { mutableStateOf(false) }
    var privacy by rememberSaveable { mutableStateOf(false) }
    // 등록 폼 상태
    var regCatKey by rememberSaveable { mutableStateOf("name") }
    var regTitle by rememberSaveable { mutableStateOf("") }
    var regContent by rememberSaveable { mutableStateOf("") }
    var regEditId by rememberSaveable { mutableStateOf(-1L) }
    // 목록/상세 상태
    var s13FilterKey by rememberSaveable { mutableStateOf("") }
    var s13Query by rememberSaveable { mutableStateOf("") }
    var detailId by rememberSaveable { mutableStateOf(-1L) }

    var quick by rememberSaveable { mutableStateOf(false) }
    var friendName by rememberSaveable { mutableStateOf("") }
    var dialog by rememberSaveable { mutableStateOf<String?>(null) }

    val ui by vm.ui.collectAsStateWithLifecycle()
    val name by vm.profileName.collectAsStateWithLifecycle()
    val expressions by vm.expressions.collectAsStateWithLifecycle()
    val recentExpressions by vm.recentExpressions.collectAsStateWithLifecycle()
    val exprCount by vm.expressionCount.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val corrections by vm.corrections.collectAsStateWithLifecycle()
    val counters by vm.counters.collectAsStateWithLifecycle()

    fun toast(m: String) = Toast.makeText(ctx, m, Toast.LENGTH_SHORT).show()
    val regCat = Category.fromKey(regCatKey)
    val s13Filter = if (s13FilterKey.isBlank()) null else Category.fromKey(s13FilterKey)

    // 실제 마이크 권한(RECORD_AUDIO) 런처: 허용 시 인식 시작, 거부 시 대체 안내(S16)로.
    val micPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) { route = "S09"; vm.startListening() } else route = "S16"
    }
    fun hasMic() = ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    fun startMic() {
        val files = vm.testAudioFiles()
        // 에뮬레이터 등 평가음원이 있으면 마이크 없이 같은 인식 흐름을 탄다.
        if (files.isNotEmpty()) { route = "S09"; vm.recognizeTestFile(if ("weather.wav" in files) "weather.wav" else files.first()); return }
        // 실기기: 권한이 있으면 바로 인식, 없으면 권한 요청 후 허용 콜백에서 인식 시작.
        if (hasMic()) { route = "S09"; vm.startListening() } else micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun goRegisterForm(cat: Category) { regEditId = -1L; regCatKey = cat.key; regTitle = ""; regContent = ""; route = "S07" }

    LaunchedEffect(Unit) {
        vm.events.collect { e ->
            when (e) {
                SessionEvent.GoResult -> { if (ui.selectedIndex == null) vm.selectCandidate(0); route = "S10" }
                SessionEvent.GoNoResult -> route = "S17"
                is SessionEvent.GoSttError -> route = "S23"
                SessionEvent.GoConfirm -> route = "S11"
                SessionEvent.GoApprove -> route = "S12"
                SessionEvent.GoAfterShare -> { vm.resetSession(); route = "S08" }
                SessionEvent.GoTtsError -> route = "S24"
                is SessionEvent.NewFriend -> { friendName = e.friend.name; route = "S20" }
                is SessionEvent.Toast -> toast(e.message)
            }
        }
    }

    // 안드로이드 뒤로가기: 대화상자 닫기 → 이전 화면 → (홈/시작에서는 시스템 기본=종료)
    BackHandler(enabled = dialog != null || (route != "S01" && route != "S08")) {
        when {
            dialog != null -> dialog = null
            route == "S07" -> route = if (regEditId >= 0) "S14" else "S06"
            route == "S09" -> { vm.cancelListening(); route = "S08" }
            else -> route = BACK_MAP[route] ?: "S08"
        }
    }

    Box(Modifier.fillMaxSize()) {
    when (route) {
        "S01" -> S01Splash(onStart = { route = "S02" })
        "S02" -> S02Guide(onNext = { route = "S03" }, onSkip = { route = "S03" })
        "S03" -> S03Consent(
            checkedNotice = notice, checkedPrivacy = privacy,
            onToggleNotice = { notice = !notice }, onTogglePrivacy = { privacy = !privacy },
            onContinue = { if (notice && privacy) route = "S08" else toast("두 항목에 모두 동의해야 시작할 수 있어요") },
            onLater = { route = "S08" },
            onViewPolicy = { dialog = "O_PRIVACY" },
        )
        "S08" -> S08Home(
            name = name,
            recent = recentExpressions,
            approvals = counters?.approvals ?: 0,
            streakDays = counters?.streakDays ?: 0,
            onMic = { startMic() },
            onRecent = { text -> vm.useExpression(text) },
            onCategory = { key -> s13FilterKey = key; route = "S13" },
            onMenu = { dialog = "O_MENU" },
            onReward = { route = "S19" },
        )
        "S09" -> S09Listen(
            status = if (ui.listen is ListenState.Processing) "인식 중…" else "듣는 중…",
            onStop = { vm.stopListening() },
            onCancel = { vm.cancelListening(); route = "S08" },
        )
        "S10" -> S10Result(
            raw = ui.raw ?: "",
            candidates = ui.candidates.map { it.text },
            selectedIndex = ui.selectedIndex,
            onSelect = { vm.selectCandidate(it) },
            onListen = { vm.speak(it) },
            onListenOriginal = { ui.raw?.let { r -> vm.speak(r) } },
            onNext = { vm.proceedWithSelection() },
            onEdit = { vm.startDirectEdit() },
        )
        "S11" -> S11Confirm(
            selectedText = ui.selected?.text ?: ui.raw ?: ui.draft,
            draft = ui.draft,
            onDraftChange = { vm.updateDraft(it) },
            onListen = { vm.speak(ui.draft) },
            onRerecord = { vm.prepareRespeak(); startMic() },
            onNext = { vm.proceedToApprove() },
        )
        "S12" -> S12Approve(
            sentence = ui.draft,
            approved = ui.isApproved,
            onToggleApprove = { if (ui.isApproved) vm.unapprove() else vm.approve() },
            onEdit = { route = "S11" },
            onShareSystem = { dialog = "O_SHARE" },
            onShareCopy = { vm.share(ShareKind.COPY, ctx); dialog = "O_COPIED" },
            onShareMessage = { vm.share(ShareKind.SMS, ctx) },
            onHome = { vm.resetSession(); route = "S08" },
        )
        "S17" -> S17NoResult(
            onAgain = { startMic() },
            onEdit = { vm.startDirectInput() },
            onRegistered = { route = "S13" },
            onBack = { vm.resetSession(); route = "S08" },
        )
        // ── 핵심표현 등록·목록 ──
        "S05" -> S05Register(
            onStart = { route = "S06" },
            onLater = { route = "S08" },
            onCategory = { cat -> goRegisterForm(cat) },
        )
        "S06" -> S06Template(
            selected = regCat,
            onSelect = { regCatKey = it.key },
            onNext = { goRegisterForm(regCat) },
            onDirect = { goRegisterForm(Category.OFTEN) },
        )
        "S07" -> S07Edit(
            category = regCat,
            title = regTitle,
            content = regContent,
            onTitleChange = { regTitle = it },
            onContentChange = { regContent = it },
            onCategoryTap = { dialog = "O_CATEGORY" },
            onListen = { if (regContent.isNotBlank()) vm.speak(regContent) },
            onBack = { route = if (regEditId >= 0) "S14" else "S06" },
            onCancel = { route = if (regEditId >= 0) "S14" else "S08" },
            onDelete = {
                if (regEditId >= 0) { vm.deleteExpression(regEditId); detailId = -1L; route = "S13" } else route = "S08"
            },
            onSave = {
                vm.saveExpression(if (regEditId >= 0) regEditId else null, regCat, regTitle, regContent) { r ->
                    when (r) {
                        is ExpressionRepository.SaveResult.Ok -> { s13FilterKey = ""; route = "S13"; toast("표현을 저장했어요") }
                        ExpressionRepository.SaveResult.Empty -> dialog = "O_INPUT_ERROR"
                        ExpressionRepository.SaveResult.TooLong -> dialog = "O_INPUT_ERROR"
                        ExpressionRepository.SaveResult.LimitReached -> toast("표현은 최대 50개까지 저장할 수 있어요")
                    }
                }
            },
        )
        "S13" -> S13List(
            expressions = expressions,
            count = exprCount,
            filter = s13Filter,
            query = s13Query,
            onFilter = { s13FilterKey = if (s13Filter == it) "" else it.key },
            onSearch = { dialog = "O_SEARCH_EXPRESSION" },
            onDetail = { id -> detailId = id; route = "S14" },
            onToggleFav = { vm.toggleFavorite(it) },
            onAdd = { route = "S05" },
            onMenu = { dialog = "O_MENU" },
        )
        "S14" -> {
            val e = expressions.find { it.id == detailId }
            if (e == null) route = "S13"
            else S14Detail(
                e = e,
                onBack = { route = "S13" },
                onToggleFav = { vm.toggleFavorite(e.id) },
                onEdit = { regEditId = e.id; regCatKey = e.category; regTitle = e.label; regContent = e.text; route = "S07" },
                onDelete = { route = "S27" },
                onListen = { vm.speak(e.text) },
            )
        }
        // ── 설정·도움말 ──
        "S18" -> S18Settings(
            onAccess = { route = "S04" },
            onTts = { route = "S04" },
            onDeleteData = { toast("데이터 삭제 (준비 중)") },
            onPrivacy = { dialog = "O_PRIVACY" },
            onHelp = { route = "S02" },
            onContact = { dialog = "O_CONTACT" },
            onMenu = { dialog = "O_MENU" },
        )
        "S04" -> S04Settings(
            fontScale = settings.fontScale,
            ttsRate = settings.ttsRate,
            haptics = settings.haptics,
            emphasis = settings.visualEmphasis,
            quick = quick,
            onFontScale = { vm.setFontScale(it) },
            onTtsRate = { vm.setTtsRate(it) },
            onHaptics = { vm.setHaptics(it) },
            onEmphasis = { vm.setVisualEmphasis(it) },
            onQuick = { quick = it },
            onSave = { route = "S18"; toast("설정을 저장했어요") },
            onDefault = { vm.resetAccessibility(); quick = false; toast("기본값으로 되돌렸어요") },
        )
        "S16" -> S16Permission(
            onSettings = {
                ctx.startActivity(
                    android.content.Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.fromParts("package", ctx.packageName, null),
                    ),
                )
            },
            onRegistered = { route = "S13" },
            onType = { vm.startDirectInput() },
        )
        "S15" -> S15History(
            corrections = corrections,
            onDelete = { vm.deleteCorrection(it) },
            onReset = { route = "S22" },
            onHome = { route = "S08" },
        )
        "S20" -> S20Friend(
            name = friendName.ifBlank { "새 친구" },
            desc = "따뜻한 응원을 잘해요",
            onSet = { toast("대표 캐릭터로 설정했어요"); route = "S12" },
            onCatalog = { toast("도감 (준비 중)") },
            onLater = { route = "S12" },
        )
        "S19" -> S19Reward(
            approvals = counters?.approvals ?: 0,
            expressionsAdded = counters?.expressionsAdded ?: 0,
            streakDays = counters?.streakDays ?: 0,
            onCatalog = { toast("말친구 도감 (준비 중)") },
            onMenu = { dialog = "O_MENU" },
        )
        // ── 안내·확인 화면(S22~S27) ──
        "S22" -> InfoScreen(
            title = "교정 이력을\n모두 지울까요?",
            body = "기기에 저장한 교정 이력을 모두 지워요.\n지운 이력은 되돌릴 수 없어요.\n\n등록한 핵심표현은 그대로 유지돼요.",
            primaryLabel = "모두 지우기", onPrimary = { vm.resetCorrections(); toast("교정 이력을 모두 지웠어요"); route = "S15" },
            secondaryLabel = "취소", onSecondary = { route = "S15" },
        )
        "S23" -> InfoScreen(
            title = "지금은 음성을\n인식할 수 없어요",
            body = "잠시 후 다시 시도하거나\n등록한 문장을 선택해 주세요.\n\n글자를 직접 입력할 수도 있어요.\n작성 중인 문장은 그대로 유지돼요.",
            primaryLabel = "등록문장 선택", onPrimary = { route = "S13" },
            secondaryLabel = "직접 입력", onSecondary = { vm.startDirectInput() },
        )
        "S24" -> InfoScreen(
            title = "지금은 소리로\n읽을 수 없어요",
            body = "글과 그림으로 문장을 확인해 주세요.\n\n선택한 문장\n${ui.draft.ifBlank { "와, 오늘 날씨 너무 좋다!" }}\n\n읽어주는 속도·한국어 음성은\n설정에서 확인할 수 있어요.\n\n음성이 없어도 직접 승인할 수 있어요.",
            primaryLabel = "글로 확인하기", onPrimary = { route = "S08" },
            secondaryLabel = "설정 확인", onSecondary = { route = "S04" },
        )
        "S25" -> InfoScreen(
            title = "아직 등록한\n표현이 없어요",
            body = "자주 쓰는 문장을 등록해 보세요.\n\n이름 / 장소 / 시간 / 메시지\n자주 쓰는 말\n\n템플릿에서 고르거나\n직접 입력할 수 있어요.\n\n지금 등록하지 않아도 말할 수 있어요.",
            primaryLabel = "표현 등록", onPrimary = { route = "S05" },
            secondaryLabel = "말하러 가기", onSecondary = { route = "S08" },
        )
        "S26" -> InfoScreen(
            title = "문장을\n그대로 두었어요",
            body = "공유를 취소했어요.\n작성한 문장은 그대로 남아 있어요.\n\n다시 공유하거나 문장을 수정할 수 있어요.",
            primaryLabel = "다시 공유하기", onPrimary = { route = "S12" },
            secondaryLabel = "문장 수정", onSecondary = { route = "S11" },
        )
        "S27" -> InfoScreen(
            title = "이 표현을 지울까요?",
            body = "이 표현을 삭제할까요?\n지운 표현은 되돌릴 수 없어요.\n\n다른 표현과 교정 이력은 유지돼요.",
            titleColor = androidx.compose.ui.graphics.Color(0xFF7142E8),
            primaryLabel = "삭제하기",
            onPrimary = {
                val id = if (regEditId >= 0) regEditId else detailId
                if (id >= 0) vm.deleteExpression(id)
                regEditId = -1L; detailId = -1L; route = "S13"; toast("표현을 삭제했어요")
            },
            secondaryLabel = "취소", onSecondary = { route = if (detailId >= 0) "S14" else "S13" },
        )
        else -> S01Splash(onStart = { route = "S02" })
    }

    // ── 모달 대화상자(현재 화면 위 오버레이) ──
    when (dialog) {
        "O_MENU" -> OptionListModal(
            title = "어디로 갈까요?", subtitle = "말잇다 앱 메뉴",
            options = listOf(
                "홈" to { dialog = null; route = "S08" },
                "내 핵심표현" to { dialog = null; route = "S13" },
                "교정 이력" to { dialog = null; route = "S15" },
                "설정·도움말" to { dialog = null; route = "S18" },
                "말친구" to { dialog = null; route = "S19" },
            ),
            onDismiss = { dialog = null },
        )
        "O_CATEGORY" -> OptionListModal(
            title = "표현 카테고리", subtitle = "표현에 맞는 카테고리를 골라 주세요.",
            options = Category.entries.map { c -> c.label to { regCatKey = c.key; dialog = null } },
            onDismiss = { dialog = null },
        )
        "O_INPUT_ERROR" -> InfoModal(
            surfTop = 289.55f, surfH = 264f, title = "내용을 확인해 주세요",
            body = "공백만 입력할 수 없습니다.\n등록 표현은 80자, 보낼 문장은 200자 이내입니다.\n작성 중인 내용은 그대로 유지합니다.",
            primaryLabel = "수정하러 돌아가기", onPrimary = { dialog = null }, onDismiss = { dialog = null },
        )
        "O_COPIED" -> InfoModal(
            surfTop = 299.55f, surfH = 244f, title = "문장을 복사했어요",
            body = "다른 앱의 입력창에 붙여넣어 보세요.",
            primaryLabel = "돌아가기", onPrimary = { dialog = null }, onDismiss = { dialog = null },
        )
        "O_SHARE" -> InfoModal(
            surfTop = 258.55f, surfH = 326f, title = "공유할 앱을 선택해요",
            body = "공유할 앱을 선택해 문장을 전달해 주세요.\n취소해도 작성한 문장은 유지돼요.",
            primaryLabel = "공유 창 열기", onPrimary = { dialog = null; vm.share(ShareKind.SYSTEM, ctx) },
            secondaryLabel = "문장 수정", onSecondary = { dialog = null; route = "S11" }, onDismiss = { dialog = null },
        )
        "O_CONTACT" -> InfoModal(
            surfTop = 268.55f, surfH = 306f, title = "문의하기",
            body = "문의 창구를 준비하고 있어요.\n도움이 필요하면 사용법을 먼저 확인해 주세요.",
            primaryLabel = "사용법 보기", onPrimary = { dialog = null; route = "S02" },
            secondaryLabel = "닫기", onSecondary = { dialog = null }, onDismiss = { dialog = null },
        )
        "O_PRIVACY" -> InfoModal(
            surfTop = 279.55f, surfH = 284f, title = "개인정보 안내",
            body = "표현과 교정 이력은 기기에 저장해요.\n녹음 파일과 공유 상대방 정보는 보관하지 않아요.\n확인한 문장만 선택한 앱으로 공유해요.",
            primaryLabel = "닫기", onPrimary = { dialog = null }, onDismiss = { dialog = null },
        )
        "O_SEARCH_EXPRESSION" -> InputModal(
            surfTop = 228.55f, surfH = 386f, title = "표현 검색", subtitle = "저장한 표현을 찾아보세요.",
            value = s13Query, onValueChange = { s13Query = it },
            primaryLabel = "검색", onPrimary = { dialog = null },
            secondaryLabel = "닫기", onSecondary = { s13Query = ""; dialog = null }, onDismiss = { dialog = null },
        )
    }
    }
}
