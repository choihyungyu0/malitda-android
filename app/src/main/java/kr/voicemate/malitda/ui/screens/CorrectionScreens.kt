package kr.voicemate.malitda.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.voicemate.malitda.R
import kr.voicemate.malitda.data.db.CorrectionEntity
import kr.voicemate.malitda.ui.components.CharacterImage
import kr.voicemate.malitda.ui.components.GradientTitle
import kr.voicemate.malitda.ui.components.InfoBox
import kr.voicemate.malitda.ui.components.MCard
import kr.voicemate.malitda.ui.components.OutlineButton
import kr.voicemate.malitda.ui.components.ScreenScaffold
import kr.voicemate.malitda.ui.components.Subtitle
import kr.voicemate.malitda.ui.components.TextAction
import kr.voicemate.malitda.ui.theme.MColors

/** S15 교정 이력 (+S22 전체 초기화 확인) */
@Composable
fun CorrectionHistoryScreen(items: List<CorrectionEntity>, onDelete: (CorrectionEntity) -> Unit, onClearAll: () -> Unit, onHome: () -> Unit, onBack: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    ScreenScaffold(onBack = onBack, scrollable = false, bottomBar = {
        OutlineButton("전체 초기화", onClick = { confirm = true }, icon = Icons.Rounded.Delete, enabled = items.isNotEmpty(), color = MColors.Danger)
        TextAction("홈으로", onClick = onHome, color = MColors.Ink2)
    }) {
        GradientTitle("교정 이력")
        Spacer(Modifier.height(4.dp))
        Subtitle("내가 승인한 교정을 관리해요.")
        Spacer(Modifier.height(12.dp))
        InfoBox("같은 오인식일 때만 다시 제안해요. 뜻이 비슷하다고 새 문장을 만들지 않아요.\n지우면 기본 인식 결과로 돌아가요.")
        Spacer(Modifier.height(10.dp))
        if (items.isEmpty()) {
            Spacer(Modifier.height(20.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                CharacterImage(R.drawable.char_cloud, 120.dp)
                Spacer(Modifier.height(10.dp))
                Text("아직 승인한 교정이 없어요", style = MaterialTheme.typography.titleMedium, color = MColors.Ink2)
            }
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items, key = { it.id }) { c ->
                MCard(padding = PaddingValues(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("오인식", style = MaterialTheme.typography.labelSmall, color = MColors.Muted)
                            Text(c.sourceRaw, style = MaterialTheme.typography.bodyMedium, color = MColors.Ink2)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.ArrowForward, null, tint = MColors.Violet, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("승인한 문장", style = MaterialTheme.typography.labelSmall, color = MColors.Violet)
                            }
                            Text(c.approvedText, style = MaterialTheme.typography.titleMedium, color = MColors.Ink)
                            if (c.useCount > 0) Text("다시 제안 ${c.useCount}회", style = MaterialTheme.typography.labelSmall, color = MColors.Muted)
                        }
                        IconButton(onClick = { onDelete(c) }, modifier = Modifier.size(48.dp)) { Icon(Icons.Rounded.Delete, "이 교정 삭제", tint = MColors.Danger) }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("교정 이력을 모두 지울까요?") },
            text = { Text("기기에 저장한 승인 교정을 지워요. 지운 교정은 되돌릴 수 없어요.\n등록한 핵심표현은 유지돼요. 기본 음성인식 결과로 돌아가요.") },
            confirmButton = { TextButton(onClick = { confirm = false; onClearAll() }) { Text("모두 지우기", color = MColors.Danger) } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("취소") } },
        )
    }
}
