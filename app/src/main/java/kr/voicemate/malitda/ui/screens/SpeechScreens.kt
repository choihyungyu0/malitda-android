package kr.voicemate.malitda.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.voicemate.malitda.R
import kr.voicemate.malitda.domain.Candidate
import kr.voicemate.malitda.domain.CandidateSource
import kr.voicemate.malitda.domain.Limits
import kr.voicemate.malitda.tts.SpeakProgress
import kr.voicemate.malitda.ui.components.CharacterImage
import kr.voicemate.malitda.ui.components.CheckCircle
import kr.voicemate.malitda.ui.components.GradientButton
import kr.voicemate.malitda.ui.components.GradientTitle
import kr.voicemate.malitda.ui.components.InfoBox
import kr.voicemate.malitda.ui.components.MCard
import kr.voicemate.malitda.ui.components.NumberBadge
import kr.voicemate.malitda.ui.components.OutlineButton
import kr.voicemate.malitda.ui.components.Pill
import kr.voicemate.malitda.ui.components.ScreenScaffold
import kr.voicemate.malitda.ui.components.SectionLabel
import kr.voicemate.malitda.ui.components.Subtitle
import kr.voicemate.malitda.ui.components.TextAction
import kr.voicemate.malitda.ui.theme.LocalA11y
import kr.voicemate.malitda.ui.theme.MColors
import kr.voicemate.malitda.ui.theme.MicGradient
import kr.voicemate.malitda.ui.theme.PrimaryGradient
import kr.voicemate.malitda.ui.theme.SuccessGradient
import kr.voicemate.malitda.ui.vm.ListenState

/** S09 말씀해 주세요 */
@Composable
fun ListeningScreen(listen: ListenState, onStart: () -> Unit, onStop: () -> Unit, onCancel: () -> Unit) {
    LaunchedEffect(Unit) { onStart() }
    androidx.activity.compose.BackHandler { onCancel() }
    val processing = listen is ListenState.Processing
    val partial = (listen as? ListenState.Listening)?.partial.orEmpty()
    val segments = (listen as? ListenState.Listening)?.segments.orEmpty()
    val shown = listOf(segments, partial).filter { it.isNotBlank() }.joinToString(" ")
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(1f, 1.08f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "s")

    ScreenScaffold(scrollable = false, bottomBar = {
        GradientButton(if (processing) "정리하는 중…" else "중지", onClick = onStop, enabled = !processing, icon = Icons.Rounded.Stop)
        TextAction("취소", onClick = onCancel)
    }) {
        GradientTitle("말씀해 주세요")
        Spacer(Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(170.dp).scale(if (processing) 1f else scale).shadow(14.dp, CircleShape, spotColor = MColors.Violet.copy(alpha = 0.4f)).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(120.dp).clip(CircleShape).background(MicGradient), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Mic, null, tint = Color.White, modifier = Modifier.size(60.dp))
                }
            }
            Spacer(Modifier.height(22.dp))
            Text(
                if (processing) "정리하는 중…" else "듣는 중…",
                style = MaterialTheme.typography.titleLarge, color = MColors.Violet,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            Spacer(Modifier.height(12.dp))
            MCard(color = MColors.SurfaceSoft, padding = PaddingValues(16.dp)) {
                Text(
                    if (shown.isBlank()) "천천히 말해도 괜찮아요.\n준비되면 중지를 눌러 주세요." else shown,
                    style = MaterialTheme.typography.bodyLarge, color = if (shown.isBlank()) MColors.Muted else MColors.Ink, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            Spacer(Modifier.height(10.dp))
            Text("취소하면 지금 녹음을 버려요. 이전에 작성하던 문장은 유지해요.", style = MaterialTheme.typography.bodySmall, color = MColors.Muted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            CharacterImage(R.drawable.char_purple, 110.dp)
        }
    }
}

/** S10 이렇게 인식했어요 */
@Composable
fun ResultScreen(
    raw: String,
    candidates: List<Candidate>,
    selectedIndex: Int?,
    m1Applied: Boolean,
    onSelect: (Int) -> Unit,
    onListen: (String) -> Unit,
    onProceed: () -> Unit,
    onDirectEdit: () -> Unit,
    onBack: () -> Unit,
) {
    ScreenScaffold(onBack = onBack, bottomBar = {
        GradientButton("선택한 문장 확인", onClick = onProceed, enabled = selectedIndex != null)
        TextAction("직접 수정", onClick = onDirectEdit)
    }) {
        GradientTitle("이렇게 인식했어요")
        Spacer(Modifier.height(4.dp))
        Subtitle("가장 잘 맞는 문장을 직접 골라 주세요.")
        Spacer(Modifier.height(18.dp))
        MCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("인식된 원문")
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onListen(raw) }, modifier = Modifier.size(44.dp)) { Icon(Icons.AutoMirrored.Rounded.VolumeUp, "원문 들어보기", tint = MColors.Violet) }
            }
            Text(raw, style = MaterialTheme.typography.headlineSmall, color = MColors.Ink)
        }
        Spacer(Modifier.height(18.dp))
        SectionLabel("고를 수 있는 문장")
        Spacer(Modifier.height(8.dp))
        candidates.forEachIndexed { i, c ->
            CandidateRow(i + 1, c, selected = selectedIndex == i, onSelect = { onSelect(i) }, onListen = { onListen(c.text) })
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(4.dp))
        InfoBox(
            if (m1Applied) "내가 전에 승인했던 문장이 있어 앞에 두었어요. 같은 인식 결과일 때만 제안해요."
            else "후보 수는 실제 인식 결과에 따라 달라져요. 자동 선택·자동 승인은 하지 않아요.",
        )
    }
}

@Composable
private fun CandidateRow(n: Int, c: Candidate, selected: Boolean, onSelect: () -> Unit, onListen: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    val tag = when (c.source) {
        CandidateSource.RAW -> "원문"
        CandidateSource.ALTERNATIVE -> null
        CandidateSource.M1_EXACT -> "내가 승인했던 문장"
        CandidateSource.M1_REORDERED -> "전에 승인한 문장"
    }
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).shadow(if (selected) 6.dp else 2.dp, shape, spotColor = MColors.Violet.copy(alpha = 0.3f)).clip(shape).background(Color.White)
            .border(2.dp, if (selected) MColors.Violet else Color.Transparent, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NumberBadge(n, active = selected)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(c.text, style = MaterialTheme.typography.titleMedium, color = MColors.Ink)
            if (tag != null) { Spacer(Modifier.height(4.dp)); Pill(tag, if (c.isM1) MColors.Violet else MColors.LavenderDeep, textColor = if (c.isM1) Color.White else MColors.Violet) }
        }
        IconButton(onClick = onListen, modifier = Modifier.size(44.dp)) { Icon(Icons.AutoMirrored.Rounded.VolumeUp, "${n}번 문장 들어보기", tint = MColors.Violet) }
        CheckCircle(selected)
        Spacer(Modifier.width(6.dp))
    }
}

/** S11 문장을 확인하고 수정해요 */
@Composable
fun ConfirmScreen(
    draft: String,
    raw: String?,
    speaking: Boolean,
    progress: SpeakProgress?,
    onDraft: (String) -> Unit,
    onListen: () -> Unit,
    onStopListen: () -> Unit,
    onProceed: () -> Unit,
    onRespeak: () -> Unit,
    onBack: () -> Unit,
) {
    val a11y = LocalA11y.current
    ScreenScaffold(onBack = onBack, bottomBar = { GradientButton("이 문장으로 진행", onClick = onProceed, enabled = draft.isNotBlank()) }) {
        GradientTitle("문장을 확인하고\n수정해요")
        Spacer(Modifier.height(4.dp))
        Subtitle("내가 말한 문장을 더 자연스럽게 다듬어 봐요.")
        Spacer(Modifier.height(18.dp))
        MCard {
            SectionLabel("선택한 문장")
            Spacer(Modifier.height(6.dp))
            if (draft.isBlank()) Text("아래 칸에 문장을 직접 적어 주세요.", style = MaterialTheme.typography.bodyMedium, color = MColors.Muted)
            else Text(highlighted(draft, progress, a11y.visualEmphasis), style = MaterialTheme.typography.headlineSmall, color = MColors.Ink)
            Spacer(Modifier.height(12.dp))
            OutlineButton(if (speaking) "멈추기" else "들어보기", onClick = { if (speaking) onStopListen() else onListen() }, icon = Icons.AutoMirrored.Rounded.VolumeUp, enabled = draft.isNotBlank())
        }
        Spacer(Modifier.height(14.dp))
        MCard {
            SectionLabel("문장을 직접 수정해 보세요")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = draft, onValueChange = { if (it.length <= Limits.MAX_TEXT_LENGTH) onDraft(it) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp).semantics { contentDescription = "문장 수정 입력칸" },
                textStyle = MaterialTheme.typography.bodyLarge, minLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MColors.Violet, unfocusedBorderColor = MColors.Line),
                shape = RoundedCornerShape(14.dp),
                supportingText = { Text("${draft.length}/${Limits.MAX_TEXT_LENGTH}", color = MColors.Muted) },
            )
            if (raw != null) { Spacer(Modifier.height(6.dp)); Text("원문: $raw", style = MaterialTheme.typography.bodySmall, color = MColors.Muted) }
        }
        Spacer(Modifier.height(14.dp))
        MCard(onClick = onRespeak, contentDescription = "다시 말하기", padding = PaddingValues(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Refresh, null, tint = MColors.Violet)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("다시 말하기", style = MaterialTheme.typography.titleSmall, color = MColors.Ink)
                    Text("문장을 다시 말하면 새로운 문장을 만들 수 있어요.", style = MaterialTheme.typography.bodySmall, color = MColors.Muted)
                }
                Text("›", color = MColors.Muted, fontSize = 22.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        InfoBox("원문은 따로 보존해요. 고치거나 다시 말하면 승인이 풀려요.")
    }
}

/** 읽어주는 구간 하이라이트(시각 강조 켜짐 시). */
fun highlighted(text: String, p: SpeakProgress?, enabled: Boolean): AnnotatedString {
    if (!enabled || p == null || p.done || p.end <= p.start || p.end > text.length) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text.substring(0, p.start))
        withStyleSafe(SpanStyle(background = MColors.Highlight)) { append(text.substring(p.start, p.end)) }
        append(text.substring(p.end))
    }
}

private inline fun androidx.compose.ui.text.AnnotatedString.Builder.withStyleSafe(style: SpanStyle, block: androidx.compose.ui.text.AnnotatedString.Builder.() -> Unit) {
    val i = pushStyle(style); block(); pop(i)
}

/** S12 승인 전(잠김) / S21 승인 완료 */
@Composable
fun ApproveScreen(
    text: String,
    approved: Boolean,
    speaking: Boolean,
    progress: SpeakProgress?,
    onApprove: (Boolean) -> Unit,
    onListen: () -> Unit,
    onStopListen: () -> Unit,
    onShareSystem: () -> Unit,
    onCopy: () -> Unit,
    onSms: () -> Unit,
    onEdit: () -> Unit,
    onHome: () -> Unit,
    onBack: () -> Unit,
) {
    val a11y = LocalA11y.current
    ScreenScaffold(onBack = onBack, bottomBar = {
        GradientButton(
            if (approved) "공유하기" else "공유하기 · 잠김", onClick = onShareSystem, enabled = approved,
            icon = if (approved) Icons.Rounded.Share else Icons.Rounded.Lock,
            contentDescription = if (approved) "공유하기" else "공유하기, 잠김. 먼저 문장을 확인해 주세요",
        )
        TextAction("홈으로", onClick = onHome, color = MColors.Ink2)
    }) {
        GradientTitle(if (approved) "확인했어요.\n이제 보낼 수 있어요" else "이 문장이 맞아요")
        Spacer(Modifier.height(18.dp))
        MCard(color = MColors.SurfaceSoft, padding = PaddingValues(22.dp)) {
            Icon(Icons.Rounded.FormatQuote, null, tint = MColors.Locked, modifier = Modifier.size(28.dp))
            Text(highlighted(text, progress, a11y.visualEmphasis), style = MaterialTheme.typography.headlineMedium, color = MColors.Ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            OutlineButton(if (speaking) "멈추기" else "들어보기", onClick = { if (speaking) onStopListen() else onListen() }, icon = Icons.AutoMirrored.Rounded.VolumeUp)
        }
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth().heightIn(min = 64.dp).shadow(3.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(Color.White)
                .toggleable(value = approved, role = Role.Checkbox, onValueChange = onApprove).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(if (approved) PrimaryGradient else SolidColor(Color.White)).border(2.dp, if (approved) Color.Transparent else MColors.Line, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                if (approved) Icon(Icons.Rounded.Check, null, tint = Color.White)
            }
            Spacer(Modifier.width(14.dp))
            Text("문장을 확인했어요", style = MaterialTheme.typography.titleMedium, color = MColors.Ink)
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                if (approved) {
                    Text("공유가 활성화되었어요!", style = MaterialTheme.typography.titleSmall, color = MColors.Violet)
                    Text("공유할 앱은 내가 직접 골라요.\n문장을 바꾸면 공유가 다시 잠겨요.", style = MaterialTheme.typography.bodySmall, color = MColors.Ink2)
                } else {
                    Text("확인하면 공유할 수 있어요", style = MaterialTheme.typography.titleSmall, color = MColors.Ink)
                    Text("공유는 아직 잠겨 있어요.\n위 칸을 눌러 직접 확인해 주세요.\n승인은 지금 문장에만 적용돼요.", style = MaterialTheme.typography.bodySmall, color = MColors.Ink2)
                }
            }
            CharacterImage(if (approved) R.drawable.char_heart else R.drawable.char_purple, 96.dp)
        }
        Spacer(Modifier.height(12.dp))
        if (approved) {
            SectionLabel("공유 방법")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ShareTile("시스템 공유", Icons.Rounded.Share, Modifier.weight(1f), onShareSystem)
                ShareTile("텍스트 복사", Icons.Rounded.ContentCopy, Modifier.weight(1f), onCopy)
                ShareTile("메시지 공유", Icons.Rounded.Sms, Modifier.weight(1f), onSms)
            }
            Spacer(Modifier.height(10.dp))
        } else {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MColors.LavenderDeep).padding(14.dp).alpha(0.9f), contentAlignment = Alignment.Center) {
                Text("먼저 문장을 확인해 주세요", color = MColors.LockedText, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(10.dp))
        }
        OutlineButton("문장 다시 수정", onClick = onEdit, icon = Icons.Rounded.Edit)
    }
}

@Composable
private fun ShareTile(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.heightIn(min = 76.dp).shadow(2.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(Color.White)
            .clickable(role = Role.Button, onClick = onClick).padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = MColors.Violet, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MColors.Ink)
    }
}

/** S26 공유 후 복귀 */
@Composable
fun AfterShareScreen(text: String, approved: Boolean, onShareAgain: () -> Unit, onEdit: () -> Unit, onHome: () -> Unit) {
    ScreenScaffold(bottomBar = {
        GradientButton("다시 공유 화면", onClick = onShareAgain, brush = if (approved) SuccessGradient else PrimaryGradient)
        TextAction("문장 수정", onClick = onEdit)
        TextAction("홈으로", onClick = onHome, color = MColors.Ink2)
    }) {
        GradientTitle("문장을\n그대로 두었어요")
        Spacer(Modifier.height(6.dp))
        Subtitle("공유 화면에서 돌아와도 문장은 유지돼요.")
        Spacer(Modifier.height(18.dp))
        MCard(color = MColors.SurfaceSoft, padding = PaddingValues(22.dp)) {
            Text(text, style = MaterialTheme.typography.headlineSmall, color = MColors.Ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(14.dp))
        InfoBox("다시 공유하거나 문장을 수정할 수 있어요. 수정하면 기존 승인은 해제돼요.\n공유 이력·상대방 정보는 저장하지 않아요.")
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { CharacterImage(R.drawable.char_robot_mint, 120.dp) }
    }
}
