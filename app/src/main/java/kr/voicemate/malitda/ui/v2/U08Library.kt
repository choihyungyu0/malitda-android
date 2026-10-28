package kr.voicemate.malitda.ui.v2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kr.voicemate.malitda.data.db.MeaningEntity
import kr.voicemate.malitda.domain.LibraryBrowse
import kr.voicemate.malitda.domain.LibraryBrowseSection
import kr.voicemate.malitda.domain.LibraryBrowseSort

/** Browse a personal dictionary; choosing a meaning still requires confirmation. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun U08Library(
    studentName: String, profileId: Long, meanings: List<MeaningEntity>,
    onOpen: (MeaningEntity) -> Unit, onRegister: () -> Unit,
    onHome: () -> Unit, onSettings: () -> Unit, onHelp: () -> Unit,
    onUse: (MeaningEntity) -> Unit = onOpen, onListen: (MeaningEntity) -> Unit = {},
    onFavorite: (MeaningEntity) -> Unit = {}, aliases: Map<Long, String> = emptyMap(),
    savedCorrectionCount: Int = 0, onSavedCorrections: (() -> Unit)? = null,
) {
    var section by rememberSaveable(profileId) { mutableStateOf(LibraryBrowseSection.ALL) }
    var query by rememberSaveable(profileId) { mutableStateOf("") }
    var category by rememberSaveable(profileId) { mutableStateOf("") }
    var situation by rememberSaveable(profileId) { mutableStateOf<String?>(null) }
    var sort by rememberSaveable(profileId) { mutableStateOf(LibraryBrowseSort.RECENT) }
    val situations = LibraryBrowse.situations(meanings, profileId)
    val validSituation = situation?.takeIf { chosen -> situations.any { it.equals(chosen, true) } }
    val filtered = LibraryBrowse.filter(meanings, profileId, aliases, query, section,
        category, validSituation, sort, meaningCategories)
    V2Screen {
        V2Header(onHelp = onHelp, logoStart = true)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            V2Title("내 표현")
            V2SubTitle("${studentName.ifBlank { "친구" }}의 뜻을 고릅니다.")
            if (onSavedCorrections != null) V2TextButton("저장한 수정 · ${savedCorrectionCount}개", onClick = onSavedCorrections)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(LibraryBrowseSection.ALL to "전체 뜻", LibraryBrowseSection.FAVORITES to "즐겨찾기",
                    LibraryBrowseSection.SITUATIONS to "상황별").forEach { (value, label) ->
                    Box(Modifier.weight(1f).heightIn(min = 48.dp)
                        .background(if (section == value) V2C.BannerBg else V2C.Surface, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .v2PorcelainFinish(RoundedCornerShape(18.dp))
                        .selectable(section == value, role = Role.Tab) { section = value; category = ""; situation = null }
                        .padding(horizontal = 6.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Text(label, style = jua(16, if (section == value) V2C.Title else V2C.Muted), textAlign = TextAlign.Center)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.padding(horizontal = 24.dp)) {
                V2TextField(query, { query = it }, "뜻·평소 표현·상황 검색")
            }
            if (section == LibraryBrowseSection.ALL) MeaningCategoryField(category, { category = it }, allowAll = true)
            if (section == LibraryBrowseSection.SITUATIONS) {
                Spacer(Modifier.height(10.dp))
                if (situations.isEmpty()) {
                    V2Banner("아직 상황을 정하지 않았습니다. 함께 등록에서 표현의 사용 상황을 추가할 수 있습니다.")
                } else FlowRow(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (listOf<String?>(null) + situations).forEach { value ->
                        androidx.compose.material3.FilterChip(
                            selected = validSituation == value, onClick = { situation = value },
                            label = { Text(value ?: "모든 상황", style = jua(16, V2C.Ink)) },
                        )
                    }
                }
            }
            V2TextButton("정렬: " + when (sort) {
                LibraryBrowseSort.RECENT -> "최근 사용"
                LibraryBrowseSort.ALPHABETICAL -> "가나다"
                LibraryBrowseSort.CATEGORY -> "분류"
            }) { sort = LibraryBrowseSort.entries[(sort.ordinal + 1) % LibraryBrowseSort.entries.size] }
            Text("${filtered.size}개", style = jua(14, V2C.Muted), modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))
            if (meanings.none { it.profileId == profileId }) {
                V2Banner("아직 함께 확인한 뜻이 없습니다. 자주 쓰는 표현부터 준비합니다.")
                V2PrimaryButton("표현 함께 준비하기", onClick = onRegister)
            } else if (filtered.isEmpty()) {
                V2Banner(if (section == LibraryBrowseSection.FAVORITES) "즐겨찾기에 담은 뜻이 없습니다. 별을 눌러 자주 쓸 뜻을 담습니다." else "조건에 맞는 뜻이 없습니다. 검색어와 선택한 상황을 확인합니다.")
                V2TextButton("전체 뜻 다시 보기") { section = LibraryBrowseSection.ALL; query = ""; category = ""; situation = null }
            }
            filtered.forEach { m ->
                LibraryRow(m, chip = if (m.isSample) "연습용 예시" else m.situation?.takeIf { it.isNotBlank() } ?: meaningCategories[m.category]) { onUse(m) }
                Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 12.dp), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { onListen(m) }, modifier = Modifier.semantics { contentDescription = "${m.text} 소리 듣기" }) {
                        Icon(Icons.Rounded.VolumeUp, null, tint = V2C.Title)
                    }
                    IconToggleButton(checked = m.favorite, onCheckedChange = { onFavorite(m) },
                        modifier = Modifier.semantics { contentDescription = "${m.text} " + if (m.favorite) "즐겨찾기 해제" else "즐겨찾기 추가" }) {
                        Icon(if (m.favorite) Icons.Rounded.Star else Icons.Rounded.StarBorder, null, tint = V2C.Title)
                    }
                    IconButton(onClick = { onOpen(m) }, modifier = Modifier.semantics { contentDescription = "${m.text} 등록 내용 수정 · 지원자" }) {
                        Icon(Icons.Rounded.Edit, null, tint = V2C.Muted)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        V2BottomNav("library", onHome, {}, onSettings, onRegister)
    }
}

@Composable
fun LibraryRow(m: MeaningEntity, chip: String? = null, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(6.dp, RoundedCornerShape(26.dp), clip = false,
                ambientColor = V2C.Shadow.copy(alpha = 0.08f), spotColor = V2C.Shadow.copy(alpha = 0.16f))
            .background(V2C.Surface, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .v2PorcelainFinish(RoundedCornerShape(26.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MeaningPicture(m.imageKey, Modifier.size(width = if (LocalDensity.current.fontScale >= 1.4f) 64.dp else 112.dp, height = 78.dp).clip(RoundedCornerShape(16.dp)), glyphSize = 28)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(m.text, style = jua(19, V2C.Ink))
            if (chip != null) {
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier.background(Color(0xFFE9E4F4), RoundedCornerShape(10.dp))
                        .v2PorcelainFinish(RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 3.dp),
                ) { Text(chip, style = jua(12, Color(0xFF6B6485))) }
            }
            Spacer(Modifier.height(8.dp))
            Text("→", style = jua(20, V2C.Title), modifier = Modifier.align(Alignment.End))
        }
    }
}
