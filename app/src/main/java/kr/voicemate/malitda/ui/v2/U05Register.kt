package kr.voicemate.malitda.ui.v2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** U05 · 표현과 뜻 등록(화면 20) / U05-E · 입력 오류(화면 21). */
@Composable
fun U05Register(
    expression: String,
    meaning: String,
    imageKey: String?,
    situation: String,
    showError: Boolean,
    conflictMessage: String?,
    imageGenerating: Boolean,
    saving: Boolean = false,
    category: String = "often",
    onCategory: (String) -> Unit = {},
    onPreview: () -> Unit = {},
    onPreviewStop: () -> Unit = {},
    onReadText: (String) -> Unit = {},
    similarMeanings: List<kr.voicemate.malitda.data.db.MeaningEntity> = emptyList(),
    onOpenExisting: (kr.voicemate.malitda.data.db.MeaningEntity) -> Unit = {},
    onExpression: (String) -> Unit,
    onMeaning: (String) -> Unit,
    onSituation: (String) -> Unit,
    onPickImage: () -> Unit,
    onConfirmWithStudent: () -> Unit,
    onSaveUnconfirmed: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onHelp: () -> Unit,
) {
    val expressionError = showError && expression.isBlank()
    val meaningError = showError && meaning.isBlank()
    V2Screen {
        V2Header(onBack = onCancel, onHelp = onHelp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            V2Title("표현과 뜻을 등록해요")
            Spacer(Modifier.height(8.dp))
            MeaningCategoryField(category, onCategory)
            if (similarMeanings.isNotEmpty()) {
                V2Banner("비슷한 뜻이 이미 있습니다. 기존 뜻을 확인하거나, 상황이 다른 뜻이면 새로 등록합니다. 자동으로 합치지 않습니다.")
                similarMeanings.forEach { existing ->
                    V2TextButton("기존 뜻 확인: ${existing.text}", onClick = { onOpenExisting(existing) })
                }
            }
            if (conflictMessage != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .background(Color(0xFFFDF1F3), RoundedCornerShape(20.dp))
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                ) { Text(conflictMessage, style = jua(16, Color(0xFFA52D4C), 1.5f)) }
                Spacer(Modifier.height(12.dp))
            }
            V2Card(radius = 28) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp)) {
                    Text("평소 쓰는 표현", style = jua(17, V2C.Ink))
                    Spacer(Modifier.height(8.dp))
                    V2TextField(
                        value = expression, onValueChange = onExpression,
                        placeholder = if (expressionError) "표현을 입력해 주세요" else "예: 바람, 산책 가자",
                        error = expressionError,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("쉼표(,)로 여러 표현을 함께 등록할 수 있어요.", style = jua(13, V2C.Muted))
                    Spacer(Modifier.height(16.dp))
                    Text("전할 뜻", style = jua(17, V2C.Ink))
                    Spacer(Modifier.height(8.dp))
                    V2TextField(
                        value = meaning, onValueChange = onMeaning,
                        placeholder = if (meaningError) "뜻을 입력해 주세요" else "예: 산책하고 싶어요",
                        error = meaningError,
                    )
                    Text("${meaning.trim().length}/100자", style = jua(14, if (meaning.trim().length > 100) Color(0xFFA52D4C) else V2C.Muted))
                    val compared = kr.voicemate.malitda.domain.TextNormalizer.comparison(meaning)
                    Spacer(Modifier.height(8.dp))
                    Text("검색할 때 비교하는 형태: $compared", style = jua(14, V2C.Muted))
                    Text("전달 문장은 입력한 원문을 유지합니다.", style = jua(14, V2C.Muted))
                    V2TextButton("저장 전 소리로 확인", onClick = onPreview)
                    V2TextButton("소리 멈추기", onClick = onPreviewStop)
                    EasyTextOffer(meaning, onUse = onMeaning, onListen = onReadText, enabled = !saving && !imageGenerating, onStop = onPreviewStop)
                    Spacer(Modifier.height(18.dp))
                    // 의미 그림 선택 줄
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(20.dp), clip = false,
                                ambientColor = V2C.Shadow.copy(alpha = 0.07f), spotColor = V2C.Shadow.copy(alpha = 0.12f))
                            .background(Brush.linearGradient(if (V2C.highContrast) listOf(V2C.BannerBg, V2C.BannerBg)
                                else listOf(V2C.BannerBg, V2C.CardInner, V2C.GreenBg.copy(alpha = 0.45f))), RoundedCornerShape(20.dp))
                            .border(if (V2C.highContrast) 2.dp else 0.75.dp,
                                if (V2C.highContrast) V2C.Ink else Color.White.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onPickImage() }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MeaningPicture(imageKey, Modifier.size(width = 104.dp, height = 70.dp).clip(RoundedCornerShape(14.dp)), glyphSize = 26)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("의미 그림", style = jua(14, V2C.Muted))
                            Spacer(Modifier.height(4.dp))
                            Text(
                                when {
                                    imageGenerating -> "그림 만드는 중…"
                                    imageKey == null -> "그림 선택하기"
                                    else -> "그림 바꾸기"
                                },
                                style = jua(19, V2C.Title),
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("사용 상황 · 선택", style = jua(17, V2C.Ink))
                    Spacer(Modifier.height(8.dp))
                    V2TextField(value = situation, onValueChange = onSituation, placeholder = "예: 쉬는 시간, 점심 후")
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "이 뜻이 맞는지 학생과 함께 확인해요.",
                        style = jua(15, V2C.Muted), textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            if (saving) {
                V2PrimaryButton("저장 중…", enabled = false) {}
            } else if (showError && (expression.isBlank() || meaning.isBlank())) {
                V2PrimaryButton("표현 다시 입력하기") { onRetry() }
                Spacer(Modifier.height(12.dp))
                V2SecondaryButton("취소") { onCancel() }
            } else {
                V2PrimaryButton("학생과 뜻 확인하기", icon = "✓") { onConfirmWithStudent() }
                Spacer(Modifier.height(12.dp))
                V2SecondaryButton("미확인으로 저장") { onSaveUnconfirmed() }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** 내장 그림 전부 + 맨 끝 '그림 없음'. 그림이 늘면 5개씩 줄을 바꿔 보여 준다. */
private val PICKER_KEYS: List<String?> = BUILTIN_MEANING_IMAGES.map { it.first } + listOf(null)

/** U05-M · 뜻을 보여줄 그림(화면 22) — 내장 그림 중 선택 후 확정. */
@Composable
fun U05PickImage(
    meaningText: String,
    selectedKey: String?,
    onSelect: (String?) -> Unit,
    onPickPhoto: () -> Unit,
    onUse: () -> Unit,
    onCancel: () -> Unit,
    onHelp: () -> Unit,
    // null이면 '그림 만들기'를 숨긴다(서버 연결이 없는 릴리스 빌드).
    onGenerate: (() -> Unit)? = null,
    generating: Boolean = false,
    onCancelGenerate: (() -> Unit)? = null,
    demoMode: Boolean = false,
    // R4 서버 상태 안내(꺼짐·점검 모드). null이면 생성 가능.
    serverNote: String? = null,
    lastError: String? = null,
    styleLabel: String = "",
    onChangeStyle: () -> Unit = {},
    consent: Boolean = false,
    onConsent: (Boolean) -> Unit = {},
    onRecheckServer: () -> Unit = {},
) {
    V2Screen {
        V2Header(onBack = onCancel, onHelp = onHelp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            V2Title("뜻을 보여줄 그림")
            V2SubTitle(meaningText.ifBlank { "전할 뜻을 함께 보여줘요" })
            Spacer(Modifier.height(14.dp))
            // 큰 미리보기 카드
            V2Card {
                Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (meaningImageAvailable(selectedKey)) {
                        MeaningPicture(selectedKey, Modifier.fillMaxWidth().aspectRatio(1.5f).clip(RoundedCornerShape(22.dp)))
                    } else {
                        Box(
                            Modifier.fillMaxWidth().aspectRatio(1.5f).background(V2C.CardInner, RoundedCornerShape(22.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CardGlyph(size = 40)
                                Spacer(Modifier.height(12.dp))
                                Text("그림 없이 문장으로 보여줘요", style = jua(16, V2C.Muted))
                            }
                        }
                    }
                    if (meaningText.isNotBlank()) {
                        Spacer(Modifier.height(16.dp))
                        Text(meaningText, style = jua(24, V2C.Ink), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            if (demoMode) V2Banner("모의 서버 · 실제 AI 생성 아님\n연결 시험용 물 그림을 반환합니다. 생성 성능이나 뜻 일치를 검증한 결과가 아닙니다.")
            Spacer(Modifier.height(14.dp))
            // 내장 그림 선택 격자(5개씩 한 줄)
            val columns = if (androidx.compose.ui.platform.LocalDensity.current.fontScale >= 1.4f) 2 else 4
            PICKER_KEYS.chunked(columns).forEach { rowKeys ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowKeys.forEach { key ->
                        val selected = key == selectedKey
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(V2C.Surface, RoundedCornerShape(16.dp))
                                .border(
                                    if (selected) 3.dp else 1.5.dp,
                                    if (selected) V2C.Title else Color(0xFFE4DEF2),
                                    RoundedCornerShape(16.dp),
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(role = Role.RadioButton) { onSelect(key) }
                                .semantics { contentDescription = (mapOf("walk" to "산책 그림", "water" to "물 그림", "rest" to "쉬기 그림", "cold" to "추위 그림")[key] ?: "그림 없음") + " 선택"; this.selected = selected },
                            contentAlignment = Alignment.Center,
                        ) {
                            val p = meaningPainter(key)
                            if (p != null) Image(p, null, Modifier.fillMaxWidth().aspectRatio(1f), contentScale = ContentScale.Crop)
                            else CardGlyph(size = 24)
                        }
                    }
                    // 마지막 줄이 짧아도 칸 크기를 같게 유지
                    repeat(columns - rowKeys.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(12.dp))
            // 학생에게 익숙한 실제 사진을 그림으로 쓸 수 있다(예: 산책 장소 사진).
            V2SecondaryButton("사진 가져오기", enabled = !generating, onClick = onPickPhoto)
            if (onGenerate != null) {
                Spacer(Modifier.height(12.dp))
                if (generating) {
                    V2Banner("그림을 만들고 있습니다. 완료되면 표시합니다.\n기다리는 동안 다른 칸을 채우거나, 결과 받기를 중단할 수 있습니다.", icon = Icons.Rounded.Info)
                    Spacer(Modifier.height(10.dp))
                    V2SecondaryButton("기다리는 동안 다른 칸 채우기") { onUse() }
                    onCancelGenerate?.let { cancel -> V2TextButton("그림 만들기 그만두기") { cancel() } }
                } else {
                    // 그림체 줄
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .background(V2C.BannerBg, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onChangeStyle() }
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("그림체: $styleLabel", style = jua(16, V2C.Title), modifier = Modifier.weight(1f))
                        Text("바꾸기 →", style = jua(15, V2C.Muted))
                    }
                    Spacer(Modifier.height(10.dp))
                    if (serverNote != null) {
                        V2Banner(serverNote, icon = Icons.Rounded.Info)
                        V2TextButton("연결 다시 확인") { onRecheckServer() }
                    } else {
                        // 직전 생성이 실패한 이유는 토스트가 사라져도 남겨 둔다(지원자가 무엇을 고칠지 알 수 있게).
                        if (lastError != null) {
                            V2Banner("지난번 그림 만들기 실패: $lastError", icon = Icons.Rounded.Info)
                            Spacer(Modifier.height(8.dp))
                        }
                        // 외부 전송·비용 동의(팀원 R4 규칙: 동의와 생성 버튼을 분리)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onConsent(!consent) }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier.size(26.dp)
                                    .background(if (consent) V2C.GreenText else Color.Transparent, RoundedCornerShape(6.dp))
                                    .border(2.dp, if (consent) V2C.GreenText else Color(0xFFC9C2DF), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center,
                            ) { if (consent) Text("✓", style = jua(16, Color.White)) }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "'전할 뜻' 문장이 인터넷으로 그림 AI에 전송되고, 비용이 들어요. 학생 이름·사진·목소리는 보내지 않아요.",
                                style = jua(14, V2C.Ink, 1.5f),
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        if (consent) V2SecondaryButton("✨  이 뜻으로 그림 만들기") { onGenerate() }
                        else Box(
                            Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(60.dp)
                                .background(Color(0xFFE9E6F2), RoundedCornerShape(30.dp)),
                            contentAlignment = Alignment.Center,
                        ) { Text("✨  이 뜻으로 그림 만들기", style = jua(20, Color(0xFFA9A3BE))) }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            V2Banner("이 그림으로 함께 확인해요.", green = true, icon = Icons.Rounded.Check)
            Spacer(Modifier.height(16.dp))
        }
        if (!generating) V2PrimaryButton("이 그림 사용", icon = "✓") { onUse() }
        V2TextButton("취소") { onCancel() }
        Spacer(Modifier.height(8.dp))
    }
}
