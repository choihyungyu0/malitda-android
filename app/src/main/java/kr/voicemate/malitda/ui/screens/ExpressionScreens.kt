package kr.voicemate.malitda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kr.voicemate.malitda.R
import kr.voicemate.malitda.data.db.ExpressionEntity
import kr.voicemate.malitda.domain.Category
import kr.voicemate.malitda.domain.Limits
import kr.voicemate.malitda.domain.Template
import kr.voicemate.malitda.domain.Templates
import kr.voicemate.malitda.ui.components.CharacterImage
import kr.voicemate.malitda.ui.components.GradientButton
import kr.voicemate.malitda.ui.components.GradientTitle
import kr.voicemate.malitda.ui.components.InfoBox
import kr.voicemate.malitda.ui.components.MCard
import kr.voicemate.malitda.ui.components.OutlineButton
import kr.voicemate.malitda.ui.components.Pill
import kr.voicemate.malitda.ui.components.ScreenScaffold
import kr.voicemate.malitda.ui.components.SectionLabel
import kr.voicemate.malitda.ui.components.Subtitle
import kr.voicemate.malitda.ui.components.TextAction
import kr.voicemate.malitda.ui.theme.MColors

fun categoryColor(c: Category): Color = when (c) {
    Category.NAME -> MColors.ChipBlue; Category.PLACE -> MColors.ChipGreen; Category.TIME -> MColors.ChipYellow
    Category.MESSAGE -> MColors.ChipPink; Category.OFTEN -> MColors.ChipPurple
}

/** S06 표현 템플릿 선택 */
@Composable
fun TemplateScreen(onPick: (Template) -> Unit, onQuickAdd: (Template) -> Unit, onCreate: () -> Unit, onBack: () -> Unit) {
    var cat by rememberSaveable { mutableStateOf<Category?>(null) }
    var q by rememberSaveable { mutableStateOf("") }
    val list = Templates.all.filter { (cat == null || it.category == cat) && (q.isBlank() || it.text.contains(q) || it.label.contains(q)) }
    ScreenScaffold(onBack = onBack, scrollable = false, bottomBar = { OutlineButton("직접 만들기", onClick = onCreate, icon = Icons.Rounded.Add) }) {
        GradientTitle("표현 템플릿을\n선택해요")
        Spacer(Modifier.height(4.dp))
        Subtitle("고른 문장으로 표현을 만들어요. 그대로 담아도 돼요.")
        Spacer(Modifier.height(12.dp))
        SearchField(q, { q = it }, "어떤 표현을 등록할까요?")
        Spacer(Modifier.height(10.dp))
        CategoryChips(cat, includeAll = true) { cat = it }
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.text }) { t ->
                MCard(onClick = { onPick(t) }, contentDescription = "${t.label}: ${t.text}, 눌러서 수정 후 저장", padding = PaddingValues(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Pill(t.category.label, categoryColor(t.category))
                            Spacer(Modifier.height(4.dp))
                            Text(t.text, style = MaterialTheme.typography.bodyLarge, color = MColors.Ink)
                        }
                        IconButton(onClick = { onQuickAdd(t) }, modifier = Modifier.size(48.dp)) { Icon(Icons.Rounded.Add, "바로 담기", tint = MColors.Violet) }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun SearchField(value: String, onChange: (String) -> Unit, hint: String) {
    OutlinedTextField(
        value = value, onValueChange = onChange, modifier = Modifier.fillMaxWidth(), singleLine = true,
        placeholder = { Text(hint, color = MColors.Muted) }, leadingIcon = { Icon(Icons.Rounded.Search, null, tint = MColors.Muted) },
        shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MColors.Violet, unfocusedBorderColor = MColors.Line, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    )
}

@Composable
fun CategoryChips(selected: Category?, includeAll: Boolean, onSelect: (Category?) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (includeAll) FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("전체") }, colors = chipColors())
        Category.entries.forEach { c -> FilterChip(selected = selected == c, onClick = { onSelect(c) }, label = { Text(c.label) }, colors = chipColors()) }
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(selectedContainerColor = MColors.Violet, selectedLabelColor = Color.White, containerColor = Color.White, labelColor = MColors.Ink)

/** S07 표현 추가·수정 */
@Composable
fun ExpressionEditScreen(
    initialCategory: Category,
    initialLabel: String,
    initialText: String,
    isEdit: Boolean,
    errorMessage: String?,
    onListen: (String) -> Unit,
    onSave: (Category, String, String) -> Unit,
    onCancel: () -> Unit,
) {
    var cat by rememberSaveable { mutableStateOf(initialCategory) }
    var label by rememberSaveable { mutableStateOf(initialLabel) }
    var text by rememberSaveable { mutableStateOf(initialText) }
    ScreenScaffold(onBack = onCancel, bottomBar = {
        GradientButton("저장", onClick = { onSave(cat, label, text) }, enabled = text.isNotBlank())
        TextAction("취소", onClick = onCancel)
    }) {
        GradientTitle(if (isEdit) "표현을\n수정해요" else "표현을\n추가해요")
        Spacer(Modifier.height(14.dp))
        SectionLabel("카테고리")
        Spacer(Modifier.height(6.dp))
        CategoryChips(cat, includeAll = false) { if (it != null) cat = it }
        Spacer(Modifier.height(14.dp))
        SectionLabel("표현 이름")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = label, onValueChange = { if (it.length <= 30) label = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            placeholder = { Text("예: 잠시 후 연락", color = MColors.Muted) }, shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MColors.Violet, unfocusedBorderColor = MColors.Line, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
        )
        Spacer(Modifier.height(14.dp))
        SectionLabel("표현 내용")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = text, onValueChange = { if (it.length <= Limits.MAX_TEXT_LENGTH) text = it }, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), minLines = 3,
            placeholder = { Text("예: 잠시 후 연락드릴게요.", color = MColors.Muted) }, shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MColors.Violet, unfocusedBorderColor = MColors.Line, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
            supportingText = { Text(if (text.isBlank()) "빈 내용은 저장할 수 없어요." else "${text.length}/${Limits.MAX_TEXT_LENGTH}", color = if (text.isBlank()) MColors.Danger else MColors.Muted) },
        )
        Spacer(Modifier.height(10.dp))
        OutlineButton("들어보기", onClick = { onListen(text) }, icon = Icons.AutoMirrored.Rounded.VolumeUp, enabled = text.isNotBlank())
        if (errorMessage != null) { Spacer(Modifier.height(10.dp)); InfoBox(errorMessage, color = MColors.DangerSoft, textColor = MColors.Danger) }
    }
}

/** S13 내 핵심표현 (+S25 빈 목록) */
@Composable
fun ExpressionListScreen(
    items: List<ExpressionEntity>,
    initialCategory: Category?,
    onOpen: (ExpressionEntity) -> Unit,
    onListen: (String) -> Unit,
    onFavorite: (ExpressionEntity) -> Unit,
    onAdd: () -> Unit,
    onTemplates: () -> Unit,
    onSpeak: () -> Unit,
    onBack: () -> Unit,
) {
    var cat by rememberSaveable { mutableStateOf(initialCategory) }
    var q by rememberSaveable { mutableStateOf("") }
    val filtered = items.filter { (cat == null || it.category == cat!!.key) && (q.isBlank() || it.text.contains(q) || it.label.contains(q)) }
    if (items.isEmpty()) {
        // S25 아직 등록한 표현이 없어요
        ScreenScaffold(onBack = onBack, bottomBar = { GradientButton("표현 등록", onClick = onTemplates, icon = Icons.Rounded.Add); TextAction("말하러 가기", onClick = onSpeak) }) {
            GradientTitle("아직 등록한\n표현이 없어요")
            Spacer(Modifier.height(6.dp))
            Subtitle("자주 쓰는 문장을 등록해 보세요.")
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { CharacterImage(R.drawable.char_yellow, 140.dp) }
            Spacer(Modifier.height(20.dp))
            MCard {
                Text("이름 · 장소 · 시간 · 메시지 · 자주 쓰는 말", style = MaterialTheme.typography.titleSmall, color = MColors.Violet)
                Spacer(Modifier.height(6.dp))
                Text("템플릿에서 고르거나 직접 입력할 수 있어요.\n지금 등록하지 않아도 말할 수 있어요.", style = MaterialTheme.typography.bodySmall, color = MColors.Ink2)
            }
        }
        return
    }
    ScreenScaffold(onBack = onBack, scrollable = false, bottomBar = { GradientButton("새 표현 추가", onClick = onAdd, icon = Icons.Rounded.Add) }) {
        GradientTitle("내 핵심표현")
        Spacer(Modifier.height(4.dp))
        Subtitle("${items.size}개 등록 · 최대 ${Limits.MAX_EXPRESSIONS}개")
        Spacer(Modifier.height(12.dp))
        SearchField(q, { q = it }, "등록한 표현 찾기")
        Spacer(Modifier.height(10.dp))
        CategoryChips(cat, includeAll = true) { cat = it }
        Spacer(Modifier.height(8.dp))
        if (filtered.isEmpty()) InfoBox("조건에 맞는 표현이 없어요.")
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered, key = { it.id }) { e ->
                MCard(onClick = { onOpen(e) }, contentDescription = "${e.text}, 상세 보기", padding = PaddingValues(start = 6.dp, end = 4.dp, top = 6.dp, bottom = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onFavorite(e) }, modifier = Modifier.size(44.dp)) {
                            Icon(if (e.favorite) Icons.Rounded.Star else Icons.Rounded.StarBorder, if (e.favorite) "즐겨찾기 해제" else "즐겨찾기", tint = if (e.favorite) MColors.ChipYellow else MColors.Muted)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(e.text, style = MaterialTheme.typography.bodyLarge, color = MColors.Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(3.dp))
                            Pill(Category.fromKey(e.category).label, categoryColor(Category.fromKey(e.category)))
                        }
                        IconButton(onClick = { onListen(e.text) }, modifier = Modifier.size(44.dp)) { Icon(Icons.AutoMirrored.Rounded.VolumeUp, "들어보기", tint = MColors.Violet) }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

/** S14 표현 상세 (+S27 삭제 확인) */
@Composable
fun ExpressionDetailScreen(
    e: ExpressionEntity,
    onUse: () -> Unit,
    onEdit: () -> Unit,
    onListen: () -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
) {
    var confirm by remember { mutableStateOf(false) }
    ScreenScaffold(onBack = onBack, bottomBar = {
        GradientButton("문장으로 사용", onClick = onUse)
        OutlineButton("수정하기", onClick = onEdit, icon = Icons.Rounded.Edit)
    }) {
        GradientTitle("표현 상세")
        Spacer(Modifier.height(16.dp))
        MCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(Category.fromKey(e.category).label, categoryColor(Category.fromKey(e.category)))
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onFavorite, modifier = Modifier.size(44.dp)) {
                    Icon(if (e.favorite) Icons.Rounded.Star else Icons.Rounded.StarBorder, if (e.favorite) "즐겨찾기 해제" else "즐겨찾기", tint = if (e.favorite) MColors.ChipYellow else MColors.Muted)
                }
            }
            Text(e.label, style = MaterialTheme.typography.labelMedium, color = MColors.Muted)
            Spacer(Modifier.height(4.dp))
            Text(e.text, style = MaterialTheme.typography.headlineSmall, color = MColors.Ink)
            Spacer(Modifier.height(12.dp))
            OutlineButton("들어보기", onClick = onListen, icon = Icons.AutoMirrored.Rounded.VolumeUp)
        }
        Spacer(Modifier.height(12.dp))
        InfoBox("문장으로 사용 → 확인 화면으로 이동해요. 수정·삭제는 이 표현에만 적용돼요.")
        Spacer(Modifier.height(12.dp))
        TextAction("이 표현 삭제", onClick = { confirm = true }, color = MColors.Danger)
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("이 표현을 삭제할까요?") },
            text = { Text("선택한 표현만 삭제해요. 교정 이력과 다른 표현은 유지돼요. 취소하면 그대로 남아요.") },
            confirmButton = { TextButton(onClick = { confirm = false; onDelete() }) { Text("삭제", color = MColors.Danger) } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("취소") } },
        )
    }
}
