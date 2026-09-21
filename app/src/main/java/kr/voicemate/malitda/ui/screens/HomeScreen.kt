package kr.voicemate.malitda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.voicemate.malitda.R
import kr.voicemate.malitda.data.db.CounterEntity
import kr.voicemate.malitda.data.db.ExpressionEntity
import kr.voicemate.malitda.domain.Category
import kr.voicemate.malitda.domain.Friends
import kr.voicemate.malitda.stt.PrepareState
import kr.voicemate.malitda.ui.components.CharacterImage
import kr.voicemate.malitda.ui.components.MCard
import kr.voicemate.malitda.ui.components.ScreenScaffold
import kr.voicemate.malitda.ui.components.SectionLabel
import kr.voicemate.malitda.ui.theme.MColors
import kr.voicemate.malitda.ui.theme.MicGradient

/** S08 홈 */
@Composable
fun HomeScreen(
    profileName: String,
    prepare: PrepareState,
    recent: List<ExpressionEntity>,
    rewardEnabled: Boolean,
    counters: CounterEntity?,
    onMic: () -> Unit,
    onCategory: (Category) -> Unit,
    onExpression: (ExpressionEntity) -> Unit,
    onExpressions: () -> Unit,
    onCorrections: () -> Unit,
    onSettings: () -> Unit,
    onReward: () -> Unit,
) {
    ScreenScaffold(topRight = {
        IconButton(onClick = onSettings, modifier = Modifier.size(48.dp)) { Icon(Icons.Rounded.Settings, contentDescription = "설정", tint = MColors.Ink2) }
    }) {
        val greeting = if (profileName.isBlank()) "안녕하세요 👋" else "안녕하세요,\n${profileName}님 👋"
        Text(greeting, style = MaterialTheme.typography.displayLarge.copy(fontSize = 30.sp, lineHeight = 40.sp), color = MColors.Ink)
        Spacer(Modifier.height(16.dp))

        if (rewardEnabled) {
            val approvals = counters?.approvals ?: 0
            val next = Friends.nextUnlockIn(approvals)
            MCard(color = MColors.SurfaceSoft, onClick = onReward, contentDescription = "말친구 리워드 보기") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(if (next != null) "다음 말친구까지\n${next}번 남았어요!" else "말친구를 모두 만났어요!", style = MaterialTheme.typography.titleMedium, color = MColors.Ink)
                        Spacer(Modifier.height(6.dp))
                        Text("문장을 확인할 때마다 한 걸음씩 가까워져요.", style = MaterialTheme.typography.bodySmall, color = MColors.Ink2)
                        Spacer(Modifier.height(8.dp))
                        val step = Friends.UNLOCK_STEP
                        LinearProgressIndicator(progress = { (approvals % step).toFloat() / step }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = MColors.Violet, trackColor = MColors.LavenderDeep)
                    }
                    Spacer(Modifier.width(10.dp))
                    CharacterImage(Friends.byId(counters?.mainCharacter).drawable, 84.dp)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // 마이크
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            val ready = prepare is PrepareState.Ready
            Box(
                Modifier.size(180.dp).shadow(12.dp, CircleShape, spotColor = MColors.Violet.copy(alpha = 0.35f)).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color.White, MColors.Lavender)))
                    .clickable(role = Role.Button, onClick = onMic)
                    .semantics { contentDescription = if (ready) "눌러서 말하기" else "음성인식 준비 중, 눌러서 상태 보기" },
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(150.dp).clip(CircleShape).background(if (ready) MicGradient else Brush.linearGradient(listOf(MColors.Locked, MColors.Locked))), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 14.dp)) {
                        if (ready) Icon(Icons.Rounded.Mic, null, tint = Color.White, modifier = Modifier.size(52.dp))
                        else CircularProgressIndicator(color = Color.White, modifier = Modifier.size(40.dp), strokeWidth = 4.dp)
                        Spacer(Modifier.height(4.dp))
                        // 큰 글자 설정(200%)에서도 원 안에 들어가도록 자동 축소
                        BasicText(
                            if (ready) "눌러서 말하기" else "준비 중",
                            style = MaterialTheme.typography.labelLarge.copy(color = Color.White, textAlign = TextAlign.Center),
                            maxLines = 2,
                            autoSize = TextAutoSize.StepBased(minFontSize = 11.sp, maxFontSize = 17.sp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            val status = when (prepare) {
                is PrepareState.Installing -> if (prepare.filesTotal > 0) "음성인식 모델 준비 중… ${prepare.filesDone}/${prepare.filesTotal}" else "음성인식 모델 준비 중…"
                PrepareState.Loading -> "음성인식 모델 불러오는 중…"
                is PrepareState.Failed -> "음성인식을 준비하지 못했어요. 눌러서 자세히 보기"
                is PrepareState.Ready -> "기기 안에서 인식해요 · 인터넷 연결 없음"
                PrepareState.NotStarted -> "잠시만요…"
            }
            Text(status, style = MaterialTheme.typography.bodySmall, color = if (prepare is PrepareState.Failed) MColors.Danger else MColors.Muted)
        }
        Spacer(Modifier.height(22.dp))

        // 핵심표현 바로가기
        SectionLabel("핵심표현 바로가기", color = MColors.Ink)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CategoryChip(Category.NAME, Icons.Rounded.Person, MColors.ChipBlue, Modifier.weight(1f)) { onCategory(Category.NAME) }
            CategoryChip(Category.PLACE, Icons.Rounded.Place, MColors.ChipGreen, Modifier.weight(1f)) { onCategory(Category.PLACE) }
            CategoryChip(Category.TIME, Icons.Rounded.Schedule, MColors.ChipYellow, Modifier.weight(1f)) { onCategory(Category.TIME) }
            CategoryChip(Category.OFTEN, Icons.Rounded.ChatBubble, MColors.ChipPurple, Modifier.weight(1f)) { onCategory(Category.OFTEN) }
        }
        Spacer(Modifier.height(22.dp))

        // 최근에 등록한 표현
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("최근에 등록한 표현", color = MColors.Ink)
            Spacer(Modifier.weight(1f))
            Text("더보기 ›", style = MaterialTheme.typography.labelMedium, color = MColors.Muted, modifier = Modifier.heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onExpressions).padding(8.dp))
        }
        MCard(padding = androidx.compose.foundation.layout.PaddingValues(vertical = 6.dp)) {
            if (recent.isEmpty()) {
                Text("등록한 표현이 아직 없어요. 등록한 문장을 골라 바로 확인할 수 있어요.", Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall, color = MColors.Muted)
            } else {
                recent.forEach { e ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(role = Role.Button) { onExpression(e) }.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Favorite, null, tint = if (e.favorite) MColors.ChipPink else MColors.Line, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(e.text, style = MaterialTheme.typography.bodyMedium, color = MColors.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NavTile("내 핵심표현", Icons.Rounded.List, Modifier.weight(1f), onExpressions)
            NavTile("교정 이력", Icons.Rounded.History, Modifier.weight(1f), onCorrections)
            NavTile("설정", Icons.Rounded.Settings, Modifier.weight(1f), onSettings)
        }
    }
}

@Composable
private fun CategoryChip(cat: Category, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.heightIn(min = 84.dp).clip(RoundedCornerShape(18.dp)).background(color.copy(alpha = 0.14f)).border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(6.dp))
        BasicText(
            cat.label,
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, color = MColors.Ink, textAlign = TextAlign.Center),
            maxLines = 2,
            autoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = 13.sp),
        )
    }
}

@Composable
private fun NavTile(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.heightIn(min = 72.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).border(1.dp, MColors.Line, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = MColors.Violet, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MColors.Ink)
    }
}
