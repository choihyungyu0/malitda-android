package kr.voicemate.malitda.ui.nav

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kr.voicemate.malitda.data.db.ExpressionEntity
import kr.voicemate.malitda.data.repo.ExpressionRepository
import kr.voicemate.malitda.domain.Category
import kr.voicemate.malitda.domain.Friends
import kr.voicemate.malitda.stt.PrepareState
import kr.voicemate.malitda.ui.screens.AccessibilityScreen
import kr.voicemate.malitda.ui.screens.AfterShareScreen
import kr.voicemate.malitda.ui.screens.ApproveScreen
import kr.voicemate.malitda.ui.screens.ConfirmScreen
import kr.voicemate.malitda.ui.screens.ConsentScreen
import kr.voicemate.malitda.ui.screens.CorrectionHistoryScreen
import kr.voicemate.malitda.ui.screens.ExpressionDetailScreen
import kr.voicemate.malitda.ui.screens.ExpressionEditScreen
import kr.voicemate.malitda.ui.screens.ExpressionListScreen
import kr.voicemate.malitda.ui.screens.HomeScreen
import kr.voicemate.malitda.ui.screens.IntroScreen
import kr.voicemate.malitda.ui.screens.ListeningScreen
import kr.voicemate.malitda.ui.screens.MicPermissionScreen
import kr.voicemate.malitda.ui.screens.NewFriendScreen
import kr.voicemate.malitda.ui.screens.NoResultScreen
import kr.voicemate.malitda.ui.screens.RegisterIntroScreen
import kr.voicemate.malitda.ui.screens.ResultScreen
import kr.voicemate.malitda.ui.screens.RewardScreen
import kr.voicemate.malitda.ui.screens.SettingsScreen
import kr.voicemate.malitda.ui.screens.SplashScreen
import kr.voicemate.malitda.ui.screens.SttErrorScreen
import kr.voicemate.malitda.ui.screens.TemplateScreen
import kr.voicemate.malitda.ui.screens.TtsErrorScreen
import kr.voicemate.malitda.ui.vm.SessionEvent
import kr.voicemate.malitda.ui.vm.SessionViewModel
import kr.voicemate.malitda.ui.vm.ShareKind
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun MalitdaNavHost(vm: SessionViewModel, nav: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val ui by vm.ui.collectAsStateWithLifecycle()
    val prepare by vm.prepareState.collectAsStateWithLifecycle()
    val profileId by vm.profileId.collectAsStateWithLifecycle()
    val profileName by vm.profileName.collectAsStateWithLifecycle()
    val counters by vm.counters.collectAsStateWithLifecycle()
    val speaking by vm.ttsSpeaking.collectAsStateWithLifecycle()
    val progress by vm.ttsProgress.collectAsStateWithLifecycle()
    val ttsState by vm.ttsState.collectAsStateWithLifecycle()
    val metrics by vm.metrics.collectAsStateWithLifecycle()
    val evalState by vm.eval.collectAsStateWithLifecycle()
    val selectedEngine by vm.selectedEngine.collectAsStateWithLifecycle()
    var sttErrorMessage by remember { mutableStateOf<String?>(null) }
    var micDeniedPermanently by remember { mutableStateOf(false) }

    fun haptic() {
        if (!settings.haptics) return
        val type = if (android.os.Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.LONG_PRESS
        view.performHapticFeedback(type)
    }
    fun goHome() { vm.resetSession(); nav.navigate(Routes.HOME) { popUpTo(0) { inclusive = true }; launchSingleTop = true } }
    fun openAppSettings() {
        runCatching { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    // 이벤트 → 내비게이션
    LaunchedEffect(Unit) {
        vm.events.collect { e ->
            when (e) {
                SessionEvent.GoResult -> nav.navigate(Routes.RESULT) { popUpTo(Routes.HOME); launchSingleTop = true }
                SessionEvent.GoNoResult -> nav.navigate(Routes.NO_RESULT) { popUpTo(Routes.HOME); launchSingleTop = true }
                is SessionEvent.GoSttError -> { sttErrorMessage = e.message; nav.navigate(Routes.STT_ERROR) { popUpTo(Routes.HOME); launchSingleTop = true } }
                SessionEvent.GoConfirm -> nav.navigate(Routes.CONFIRM) { launchSingleTop = true }
                SessionEvent.GoApprove -> nav.navigate(Routes.APPROVE) { launchSingleTop = true }
                SessionEvent.GoAfterShare -> nav.navigate(Routes.AFTER_SHARE) { launchSingleTop = true }
                SessionEvent.GoTtsError -> nav.navigate(Routes.TTS_ERROR) { launchSingleTop = true }
                is SessionEvent.NewFriend -> nav.navigate(Routes.newFriend(e.friend.id)) { launchSingleTop = true }
                is SessionEvent.Toast -> Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 마이크 권한
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) nav.navigate(Routes.LISTENING) { launchSingleTop = true }
        else { micDeniedPermanently = true; nav.navigate(Routes.MIC_DENIED) { launchSingleTop = true } }
    }
    fun requestMicThenListen() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ->
                nav.navigate(Routes.LISTENING) { launchSingleTop = true }
            else -> micLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    fun onMicPressed() {
        if (!settings.consentAccepted) {
            // S03을 건너뛴 사용자: 마이크를 켜기 전에 안내·동의를 한 번은 확인한다(자동 진행 없음).
            Toast.makeText(context, "말하기 전에 안내를 한 번 확인해 주세요", Toast.LENGTH_SHORT).show()
            nav.navigate(Routes.CONSENT) { launchSingleTop = true }
            return
        }
        when (prepare) {
            is PrepareState.Ready -> { vm.prepareRespeak(); requestMicThenListen() }
            is PrepareState.Failed -> { sttErrorMessage = (prepare as PrepareState.Failed).message; nav.navigate(Routes.STT_ERROR) { launchSingleTop = true } }
            else -> Toast.makeText(context, "음성인식 모델을 준비하고 있어요. 잠시만 기다려 주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    val start = if (settings.onboarded) Routes.HOME else Routes.SPLASH

    NavHost(navController = nav, startDestination = start) {
        // ---------- 온보딩 ----------
        composable(Routes.SPLASH) {
            SplashScreen(onStart = { nav.navigate(Routes.INTRO) }, onHome = { nav.navigate(Routes.HOME) { popUpTo(Routes.SPLASH) { inclusive = true } } }, autoAdvance = false)
        }
        composable(Routes.INTRO) {
            IntroScreen(onNext = { nav.navigate(Routes.CONSENT) }, onSkip = { finishOnboarding(vm, nav) })
        }
        composable(Routes.CONSENT) {
            val onboarded = settings.onboarded
            ConsentScreen(
                onAccept = {
                    scope.launch { vm.c.settings.setConsent(true) }
                    if (onboarded) { Toast.makeText(context, "이제 마이크를 눌러 말할 수 있어요", Toast.LENGTH_SHORT).show(); nav.popBackStack() }
                    else nav.navigate(Routes.access(onboarding = true))
                },
                onLater = { if (onboarded) nav.popBackStack() else finishOnboarding(vm, nav) },
            )
        }
        composable(Routes.ACCESS + "?onboarding={onboarding}", arguments = listOf(navArgument("onboarding") { type = NavType.BoolType; defaultValue = false })) { entry ->
            val onboarding = entry.arguments?.getBoolean("onboarding") ?: false
            AccessibilityScreen(
                settings = settings, onboarding = onboarding,
                onSave = { fs, rate, hap, vis, rew ->
                    scope.launch {
                        vm.c.settings.setFontScale(fs); vm.c.settings.setTtsRate(rate); vm.c.settings.setHaptics(hap); vm.c.settings.setVisualEmphasis(vis); vm.c.settings.setRewardEnabled(rew)
                    }
                    if (onboarding) nav.navigate(Routes.REGISTER_INTRO) else nav.popBackStack()
                },
                onDefault = { nav.navigate(Routes.REGISTER_INTRO) },
                onBack = if (onboarding) null else ({ nav.popBackStack() }),
            )
        }
        composable(Routes.REGISTER_INTRO) {
            RegisterIntroScreen(onStart = { nav.navigate(Routes.TEMPLATE) }, onLater = { finishOnboarding(vm, nav) }, onBack = null)
        }

        // ---------- 표현 ----------
        composable(Routes.TEMPLATE) {
            TemplateScreen(
                onPick = { t -> nav.navigate(Routes.edit(cat = t.category.key, label = t.label, text = t.text)) },
                onQuickAdd = { t ->
                    scope.launch {
                        val r = vm.c.expressions.save(profileId, null, t.category, t.label, t.text)
                        val msg = when (r) { is ExpressionRepository.SaveResult.Ok -> { vm.c.counters.onExpressionAdded(profileId); "담았어요: ${t.text}" }; ExpressionRepository.SaveResult.LimitReached -> "최대 50개까지 등록할 수 있어요"; else -> "저장하지 못했어요" }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                onCreate = { nav.navigate(Routes.edit()) },
                onBack = { if (!settings.onboarded) finishOnboarding(vm, nav) else nav.popBackStack() },
            )
        }
        composable(
            Routes.EDIT + "?id={id}&cat={cat}&label={label}&text={text}",
            arguments = listOf(
                navArgument("id") { type = NavType.LongType; defaultValue = 0L },
                navArgument("cat") { type = NavType.StringType; nullable = true },
                navArgument("label") { type = NavType.StringType; nullable = true },
                navArgument("text") { type = NavType.StringType; nullable = true },
            ),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            val argCat = entry.arguments?.getString("cat")
            val argLabel = entry.arguments?.getString("label").orEmpty()
            val argText = entry.arguments?.getString("text").orEmpty()
            val existing by (if (id != 0L) vm.c.expressions.observe(id) else flowOf(null)).collectAsStateWithLifecycle(null)
            var error by remember { mutableStateOf<String?>(null) }
            if (id != 0L && existing == null) return@composable
            val e = existing
            ExpressionEditScreen(
                initialCategory = Category.fromKey(e?.category ?: argCat),
                initialLabel = e?.label ?: argLabel,
                initialText = e?.text ?: argText,
                isEdit = e != null,
                errorMessage = error,
                onListen = { vm.speak(it) },
                onSave = { cat, label, text ->
                    scope.launch {
                        when (val r = vm.c.expressions.save(profileId, e?.id, cat, label, text)) {
                            is ExpressionRepository.SaveResult.Ok -> {
                                if (e == null) vm.c.counters.onExpressionAdded(profileId)
                                haptic()
                                if (!settings.onboarded) finishOnboarding(vm, nav)
                                else nav.navigate(Routes.expressions()) { popUpTo(Routes.HOME); launchSingleTop = true }
                            }
                            ExpressionRepository.SaveResult.Empty -> error = "빈 내용은 저장할 수 없어요."
                            ExpressionRepository.SaveResult.LimitReached -> error = "최대 50개까지 등록할 수 있어요. 안 쓰는 표현을 지운 뒤 추가해 주세요."
                            ExpressionRepository.SaveResult.TooLong -> error = "표현이 너무 길어요(200자 이내)."
                        }
                    }
                },
                onCancel = { nav.popBackStack() },
            )
        }
        composable(Routes.EXPRESSIONS + "?cat={cat}", arguments = listOf(navArgument("cat") { type = NavType.StringType; nullable = true })) { entry ->
            val cat = entry.arguments?.getString("cat")?.let { Category.fromKey(it) }
            val items by remember(profileId) { vm.c.expressions.observeAll(profileId) }.collectAsStateWithLifecycle(emptyList())
            ExpressionListScreen(
                items = items, initialCategory = cat,
                onOpen = { nav.navigate(Routes.detail(it.id)) },
                onListen = { vm.speak(it) },
                onFavorite = { e -> scope.launch { vm.c.expressions.toggleFavorite(e.id) } },
                onAdd = { nav.navigate(Routes.edit()) },
                onTemplates = { nav.navigate(Routes.TEMPLATE) },
                onSpeak = { onMicPressed() },
                onBack = { nav.popBackStack() },
            )
        }
        composable(Routes.DETAIL + "/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            val e by vm.c.expressions.observe(id).collectAsStateWithLifecycle(null)
            val ex: ExpressionEntity = e ?: return@composable
            ExpressionDetailScreen(
                e = ex,
                onUse = { vm.useExpression(ex.text) },
                onEdit = { nav.navigate(Routes.edit(id = ex.id)) },
                onListen = { vm.speak(ex.text) },
                onFavorite = { scope.launch { vm.c.expressions.toggleFavorite(ex.id) } },
                onDelete = { scope.launch { vm.c.expressions.delete(ex.id); nav.popBackStack() } },
                onBack = { nav.popBackStack() },
            )
        }

        // ---------- 홈·말하기 ----------
        composable(Routes.HOME) {
            val recent by remember(profileId) { vm.c.expressions.observeRecent(profileId, 3) }.collectAsStateWithLifecycle(emptyList())
            HomeScreen(
                profileName = profileName, prepare = prepare, recent = recent, rewardEnabled = settings.rewardEnabled, counters = counters,
                onMic = { onMicPressed() },
                onCategory = { nav.navigate(Routes.expressions(it.key)) },
                onExpression = { vm.useExpression(it.text) },
                onExpressions = { nav.navigate(Routes.expressions()) },
                onCorrections = { nav.navigate(Routes.CORRECTIONS) },
                onSettings = { nav.navigate(Routes.SETTINGS) },
                onReward = { nav.navigate(Routes.REWARD) },
            )
        }
        composable(Routes.LISTENING) {
            ListeningScreen(listen = ui.listen, onStart = { vm.startListening() }, onStop = { vm.stopListening() }, onCancel = { vm.cancelListening(); nav.popBackStack() })
        }
        composable(Routes.RESULT) {
            ResultScreen(
                raw = ui.raw.orEmpty(), candidates = ui.candidates, selectedIndex = ui.selectedIndex, m1Applied = ui.m1Applied,
                onSelect = { vm.selectCandidate(it) }, onListen = { vm.speak(it) },
                onProceed = { vm.proceedWithSelection() }, onDirectEdit = { vm.startDirectEdit() },
                onBack = { goHome() },
            )
        }
        composable(Routes.CONFIRM) {
            ConfirmScreen(
                draft = ui.draft, raw = ui.raw, speaking = speaking, progress = progress,
                onDraft = { vm.updateDraft(it) }, onListen = { vm.speak(ui.draft) }, onStopListen = { vm.stopSpeaking() },
                onProceed = { vm.proceedToApprove() },
                onRespeak = { vm.prepareRespeak(); requestMicThenListen() },
                onBack = { nav.popBackStack() },
            )
        }
        composable(Routes.APPROVE) {
            ApproveScreen(
                text = ui.draft.trim(), approved = ui.isApproved, speaking = speaking, progress = progress,
                onApprove = { checked -> if (checked) { vm.approve(); haptic() } else vm.unapprove() },
                onListen = { vm.speak(ui.draft) }, onStopListen = { vm.stopSpeaking() },
                onShareSystem = { vm.share(ShareKind.SYSTEM, context) }, onCopy = { vm.share(ShareKind.COPY, context) }, onSms = { vm.share(ShareKind.SMS, context) },
                onEdit = { nav.navigate(Routes.CONFIRM) { popUpTo(Routes.CONFIRM) { inclusive = true } } },
                onHome = { goHome() },
                onBack = { nav.popBackStack() },
            )
        }
        composable(Routes.AFTER_SHARE) {
            AfterShareScreen(
                text = ui.draft.trim(), approved = ui.isApproved,
                onShareAgain = { nav.popBackStack() },
                onEdit = { nav.navigate(Routes.CONFIRM) { popUpTo(Routes.CONFIRM) { inclusive = true } } },
                onHome = { goHome() },
            )
        }

        // ---------- 교정 이력 ----------
        composable(Routes.CORRECTIONS) {
            val items by remember(profileId) { vm.c.corrections.observeAll(profileId) }.collectAsStateWithLifecycle(emptyList())
            CorrectionHistoryScreen(
                items = items,
                onDelete = { c -> scope.launch { vm.c.corrections.delete(c.id) } },
                onClearAll = { scope.launch { vm.c.corrections.deleteAll(profileId); Toast.makeText(context, "기본 인식 결과로 돌아가요", Toast.LENGTH_SHORT).show() } },
                onHome = { goHome() }, onBack = { nav.popBackStack() },
            )
        }

        // ---------- 예외 ----------
        composable(Routes.MIC_DENIED) {
            MicPermissionScreen(
                onRetry = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                onOpenSettings = { openAppSettings() },
                onExpressions = { nav.navigate(Routes.expressions()) },
                onDirect = { vm.startDirectInput() },
                onBack = { nav.popBackStack() },
            )
        }
        composable(Routes.NO_RESULT) {
            NoResultScreen(
                onRespeak = { vm.prepareRespeak(); requestMicThenListen() },
                onDirect = { vm.startDirectInput() },
                onExpressions = { nav.navigate(Routes.expressions()) },
                onBack = { goHome() },
            )
        }
        composable(Routes.STT_ERROR) {
            SttErrorScreen(
                message = sttErrorMessage,
                onRetry = { vm.retryPrepare(); Toast.makeText(context, "다시 준비하고 있어요", Toast.LENGTH_SHORT).show(); goHome() },
                onExpressions = { nav.navigate(Routes.expressions()) },
                onDirect = { vm.startDirectInput() },
                onBack = { goHome() },
            )
        }
        composable(Routes.TTS_ERROR) {
            TtsErrorScreen(
                text = ui.draft, reason = (ttsState as? kr.voicemate.malitda.tts.TtsState.Unavailable)?.reason,
                onReadText = { nav.popBackStack() },
                onSettings = { nav.navigate(Routes.SETTINGS) },
                onBack = { nav.popBackStack() },
            )
        }

        // ---------- 설정·리워드 ----------
        composable(Routes.SETTINGS) {
            val profiles by vm.c.profiles.all.collectAsStateWithLifecycle(emptyList())
            SettingsScreen(
                profiles = profiles, currentProfileId = profileId, prepare = prepare, sttName = vm.c.stt.name, tts = ttsState, metrics = metrics,
                onAccessibility = { nav.navigate(Routes.access(onboarding = false)) },
                onExpressions = { nav.navigate(Routes.expressions()) },
                onCorrections = { nav.navigate(Routes.CORRECTIONS) },
                onReplayIntro = { nav.navigate(Routes.INTRO) },
                onOpenAppSettings = { openAppSettings() },
                onSwitchProfile = { id -> scope.launch { vm.resetSession(); vm.c.profiles.switchTo(id) } },
                onAddProfile = { name -> scope.launch { val id = vm.c.profiles.add(name); vm.resetSession(); vm.c.profiles.switchTo(id) } },
                onRenameProfile = { id, name -> scope.launch { vm.c.profiles.rename(id, name) } },
                onDeleteAll = { scope.launch { vm.c.expressions.deleteAll(profileId); vm.c.corrections.deleteAll(profileId); vm.c.counters.reset(profileId); vm.resetSession(); Toast.makeText(context, "이 사용자 자료를 모두 지웠어요", Toast.LENGTH_SHORT).show() } },
                onBack = { nav.popBackStack() },
                testFiles = remember { vm.testAudioFiles() },
                onTestFile = { name -> if (prepare is PrepareState.Ready) { vm.prepareRespeak(); vm.recognizeTestFile(name) } else Toast.makeText(context, "음성인식 모델이 아직 준비되지 않았어요", Toast.LENGTH_SHORT).show() },
                evalState = evalState,
                onRunEval = { if (prepare is PrepareState.Ready) vm.runEvaluation() else Toast.makeText(context, "음성인식 모델이 아직 준비되지 않았어요", Toast.LENGTH_SHORT).show() },
                onClearEval = { vm.clearEval() },
                engines = vm.c.sttRouter.ids.map { it to vm.c.sttRouter.engineName(it) },
                selectedEngine = selectedEngine,
                onSelectEngine = { id -> vm.switchEngine(id); Toast.makeText(context, "엔진을 바꾸는 중이에요", Toast.LENGTH_SHORT).show() },
            )
            LaunchedEffect(Unit) { vm.c.metrics.refresh() }
        }
        composable(Routes.REWARD) {
            val count by remember(profileId) { vm.c.expressions.observeCount(profileId) }.collectAsStateWithLifecycle(0)
            RewardScreen(
                counters = counters, expressionCount = count,
                onFriend = { f -> scope.launch { vm.c.counters.setMainCharacter(profileId, f.id); Toast.makeText(context, "${f.name}를 대표 캐릭터로 설정했어요", Toast.LENGTH_SHORT).show() } },
                onHome = { goHome() }, onBack = { nav.popBackStack() },
            )
        }
        composable(Routes.NEW_FRIEND + "/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
            val friend = Friends.byId(entry.arguments?.getString("id"))
            NewFriendScreen(
                friend = friend,
                onSetMain = { scope.launch { vm.c.counters.setMainCharacter(profileId, friend.id) }; nav.popBackStack() },
                onLater = { nav.popBackStack() },
            )
        }
    }
}

private fun finishOnboarding(vm: SessionViewModel, nav: NavHostController) {
    kotlinx.coroutines.MainScope().launch { vm.c.settings.setOnboarded(true) }
    nav.navigate(Routes.HOME) { popUpTo(0) { inclusive = true }; launchSingleTop = true }
}
