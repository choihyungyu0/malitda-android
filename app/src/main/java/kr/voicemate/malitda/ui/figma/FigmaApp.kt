package kr.voicemate.malitda.ui.figma

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxHeight
import kr.voicemate.malitda.R
import kr.voicemate.malitda.ui.vm.SessionViewModel

/**
 * Figma 목업 이미지를 그대로 화면으로 쓰는 이미지 기반 앱.
 * control-contract.json 좌표의 투명 터치영역으로 화면을 전환한다(디자이너 프로토타입과 동일).
 * S22~S27은 계약 좌표가 없어 하단 주/보조 행동 영역만 연결한다.
 */
@Composable
fun FigmaApp(vm: SessionViewModel, start: String = "S01") {
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val profileId by vm.profileId.collectAsStateWithLifecycle()
    var current by rememberSaveable { mutableStateOf(start) }
    var overlay by rememberSaveable { mutableStateOf<String?>(null) }
    var history by rememberSaveable { mutableStateOf(listOf<String>()) }
    // 이미지 위에 얹는 실제 상태(체크박스 토글 등). id = "화면/스팟".
    var checked by rememberSaveable { mutableStateOf(setOf<String>()) }
    fun isChecked(id: String) = "$current/$id" in checked
    fun toggle(id: String) { val k = "$current/$id"; checked = if (k in checked) checked - k else checked + k }
    // 입력칸의 실제 텍스트. id = "화면/스팟".
    val inputs = remember { androidx.compose.runtime.mutableStateMapOf<String, String>() }
    // S07에서 고른 카테고리(기본 시간, 목업 기본값과 일치).
    var s07CatKey by rememberSaveable { mutableStateOf("time") }
    val s07Cat = kr.voicemate.malitda.domain.Category.fromKey(s07CatKey)
    // 라디오 단일 선택(화면 → 선택된 스팟 id). S06 템플릿은 기본 category0.
    val selected = remember { androidx.compose.runtime.mutableStateMapOf("S06" to "category0") }
    // category0..4 → 카테고리(카드 순서: 이름·장소·시간·메시지·자주쓰는말)
    fun catOf(spotId: String) = when (spotId) {
        "category0" -> kr.voicemate.malitda.domain.Category.NAME
        "category1" -> kr.voicemate.malitda.domain.Category.PLACE
        "category2" -> kr.voicemate.malitda.domain.Category.TIME
        "category3" -> kr.voicemate.malitda.domain.Category.MESSAGE
        else -> kr.voicemate.malitda.domain.Category.OFTEN
    }

    fun go(target: String) {
        if (target == current) return
        history = history + current
        overlay = null
        current = target
    }
    fun back() {
        if (overlay != null) { overlay = null; return }
        val h = history
        if (h.isNotEmpty()) { current = h.last(); history = h.dropLast(1) }
    }
    BackHandler(enabled = overlay != null || history.isNotEmpty()) { back() }

    fun handle(s: Spot) {
        val a = s.action
        val t = s.target
        when {
            a == "overlay" && t != null -> overlay = t
            a.startsWith("toggle:") -> toggle(s.id)
            a == "consent_gate" -> {
                val boxes = (HOTSPOTS[current] ?: emptyList()).filter { it.kind == "checkbox" }
                if (boxes.all { "$current/${it.id}" in checked }) go(t ?: "S04")
                else Toast.makeText(context, "위 두 항목을 모두 확인해 주세요", Toast.LENGTH_SHORT).show()
            }
            a == "record" -> go("S09")
            a == "finish_record" -> go("S10")
            a == "cancel_record" -> go("S08")
            // 라디오 단일 선택(S06 템플릿 카드, S10 후보): 선택만 하고 이동하지 않음
            s.kind == "radio" -> selected[current] = s.id
            a == "confirm_selection" -> go("S11")
            a == "confirm_message" -> go("S12")
            a == "approve" -> go("S21")
            a == "unapprove" -> go("S12")
            a.startsWith("share_gate") -> {
                val sentence = inputs["S11/content"].orEmpty().trim()
                if (sentence.isBlank()) Toast.makeText(context, "먼저 문장을 확인해 주세요", Toast.LENGTH_SHORT).show()
                else {
                    if (s.id == "copy") {
                        val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("말잇다", sentence))
                        if (android.os.Build.VERSION.SDK_INT < 33) Toast.makeText(context, "복사했어요", Toast.LENGTH_SHORT).show()
                    } else runCatching {
                        val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, sentence) }
                        context.startActivity(Intent.createChooser(send, "공유할 앱 선택").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    go("S26")
                }
            }
            a == "save_expression" -> {
                val title = inputs["S07/title"].orEmpty()
                val content = inputs["S07/content"].orEmpty()
                if (content.isBlank()) Toast.makeText(context, "표현 내용을 입력해 주세요", Toast.LENGTH_SHORT).show()
                else scope.launch {
                    vm.c.expressions.save(profileId, null, s07Cat, title, content)
                    inputs.remove("S07/title"); inputs.remove("S07/content")
                    go("S13")
                }
            }
            a == "choose_registered" -> go("S13")
            a == "new_expression" -> go("S07")
            a == "edit_expression" -> go("S07")
            a == "edit_reset" -> go(t ?: "S11")
            a == "select_character" -> go("S08")
            a == "clear:expression" -> { /* 초안 지우기: 정적 */ }
            a.startsWith("category:") || a.startsWith("filter:") || a.startsWith("favorite:") ||
                a.startsWith("speed:") || a.startsWith("toggle:") || a.startsWith("quick:") ||
                a == "reset_settings" || a == "inputmode:text" -> { t?.let { go(it) } }
            a == "start" || a == "navigate" -> {
                // S06 '다음'으로 S07 진입 시, 선택한 템플릿 카테고리를 S07로 넘긴다.
                if (current == "S06" && t == "S07" && s.id == "next") {
                    s07CatKey = catOf(selected["S06"] ?: "category0").key
                }
                t?.let { go(it) }
            }
            else -> t?.let { go(it) }
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF6F4F0)).safeDrawingPadding()) {
        val img = FIGMA_IMAGE[current] ?: FIGMA_IMAGE["S01"]!!
        val spots = HOTSPOTS[current] ?: EXTRA_SPOTS[current] ?: emptyList()
        FigmaScreen(
            imageRes = img, spots = spots, onSpot = { handle(it) },
            overlay = { w, h ->
                // 이미지에 그려진 빈 체크박스 위에, 체크한 항목만 실제 체크 표시를 얹는다.
                spots.filter { it.kind == "checkbox" && isChecked(it.id) }.forEach { s ->
                    CheckMark(
                        Modifier
                            .offset((s.x * w).dp, (s.y * h).dp)
                            .requiredSize((s.w * w).dp, (s.h * h).dp),
                    )
                }
                // 이미지의 입력칸 위에 실제 입력칸을 얹는다(그림의 예시 글자를 덮고 직접 타이핑).
                spots.filter { (it.kind == "input" || it.kind == "textarea") && it.action != "overlay" || it.id == "title" || it.id == "content" }.forEach { s ->
                    val key = "$current/${s.id}"
                    FigmaInput(
                        value = inputs[key] ?: "",
                        onValue = { inputs[key] = it },
                        singleLine = s.kind == "input",
                        modifier = Modifier
                            .offset((s.x * w).dp, (s.y * h).dp)
                            .requiredSize((s.w * w).dp, (s.h * h).dp),
                    )
                }
                // S12/S21: 이미지의 고정 예시 문장을 덮고, S11에서 확인·수정한 실제 문장을 표시.
                if (current == "S12" || current == "S21") {
                    val sentence = inputs["S11/content"].orEmpty().trim()
                    Box(
                        Modifier
                            .offset((0.09f * w).dp, (0.255f * h).dp)
                            .requiredSize((0.82f * w).dp, (0.155f * h).dp)
                            .background(Color(0xFFF9F6FC))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material3.Text(
                            if (sentence.isBlank()) "확인할 문장이 없어요" else "“ $sentence ”",
                            color = if (sentence.isBlank()) Color(0xFF9A93AC) else Color(0xFF081F45),
                            fontSize = 20.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, lineHeight = 28.sp,
                        )
                    }
                }
                // S07 카테고리 칸: 이미지의 고정 "시간"을 덮고 선택한 카테고리(아이콘+이름)를 표시.
                if (current == "S07") {
                    HOTSPOTS["S07"]?.firstOrNull { it.id == "category" }?.let { s ->
                        androidx.compose.foundation.layout.Row(
                            Modifier
                                .offset((s.x * w).dp, (s.y * h).dp)
                                .requiredSize((s.w * 0.62f * w).dp, (s.h * h).dp)
                                .background(Color.White)
                                .padding(start = (s.w * 0.06f * w).dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(painterResource(catIconRes(s07Cat)), null, modifier = Modifier.requiredSize((s.h * h * 0.82f).dp))
                            androidx.compose.foundation.layout.Spacer(Modifier.requiredSize(8.dp))
                            androidx.compose.material3.Text(s07Cat.label, color = Color(0xFF1A2440), fontSize = 15.sp)
                        }
                    }
                }
                // S06 템플릿: 라디오 단일 선택 표시. 고정 체크(category0)를 덮고 선택한 카드에 디자이너 체크를 얹는다.
                if (current == "S06") {
                    val sel = selected["S06"] ?: "category0"
                    val checkSize = 0.082f          // 이미지 폭 기준 체크 지름(측정)
                    val cxCenter = 0.892f           // 카드 우측 체크 가로 중심(측정)
                    val sizePx = checkSize * w
                    HOTSPOTS["S06"]?.filter { it.kind == "radio" }?.forEach { card ->
                        val cyCenter = card.y + card.h / 2f
                        val left = (cxCenter * w - sizePx / 2f)
                        val top = (cyCenter * h - sizePx / 2f)
                        if (card.id == sel) {
                            Image(
                                painter = painterResource(R.drawable.sel_check),
                                contentDescription = "선택됨",
                                modifier = Modifier.offset(left.dp, top.dp).requiredSize(sizePx.dp),
                            )
                        } else if (card.id == "category0") {
                            // 기존 고정 체크를 카드 배경색(살짝 큰 원)으로 덮어 완전히 가림
                            Box(
                                Modifier.offset((left - sizePx * 0.12f).dp, (top - sizePx * 0.12f).dp)
                                    .requiredSize((sizePx * 1.24f).dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape).background(Color.White),
                            )
                        }
                    }
                }
                // S13: 이미지의 예시 목록을 덮고, 사용자가 실제 등록한 표현 목록을 얹는다.
                if (current == "S13") {
                    S13RealList(
                        vm = vm, profileId = profileId,
                        modifier = Modifier
                            .offset((0.03f * w).dp, (0.325f * h).dp)
                            .requiredSize((0.94f * w).dp, (0.58f * h).dp),
                        onOpen = { go("S14") },
                    )
                }
            },
        )

        // 상단 좌측 뒤로가기(정적 이미지엔 항상 있으므로 안전망)
        if (history.isNotEmpty() && overlay == null) {
            Box(
                Modifier.padding(4.dp).fillMaxWidth(0.16f).fillMaxHeight(0.06f)
                    .pointerInput(Unit) { detectTapGestures(onTap = { back() }) },
            )
        }

        overlay?.let { ov ->
            val ovImg = FIGMA_IMAGE[ov] ?: return@let
            Box(
                Modifier.fillMaxSize().background(Color(0x99101018))
                    .pointerInput(ov) { detectTapGestures(onTap = { overlay = null }) },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(ovImg),
                    contentDescription = "$ov 안내",
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .pointerInput(ov) { detectTapGestures(onTap = { overlay = null }) },
                    contentScale = ContentScale.FillWidth,
                )
            }
        }
    }
}

/** S13 목록 자리에 얹는 사용자의 실제 등록 표현 목록(예시 이미지를 덮음). */
@Composable
private fun S13RealList(
    vm: SessionViewModel,
    profileId: Long,
    modifier: Modifier,
    onOpen: (Long) -> Unit,
) {
    val exprs by androidx.compose.runtime.remember(profileId) { vm.c.expressions.observeAll(profileId) }
        .collectAsStateWithLifecycle(emptyList())
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    Box(modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFFBFAF6))) {
        if (exprs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.TopCenter) {
                androidx.compose.material3.Text(
                    "아직 등록한 표현이 없어요.\n아래 ‘새 표현 추가’로 시작해요.",
                    color = Color(0xFF9A93AC), fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                Modifier.fillMaxSize(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            ) {
                items(exprs, key = { it.id }) { e ->
                    val cat = kr.voicemate.malitda.domain.Category.fromKey(e.category)
                    androidx.compose.foundation.layout.Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)
                            .pointerInput(e.id) { detectTapGestures(onTap = { onOpen(e.id) }) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 디자이너 원안에서 추출한 실제 카테고리 아이콘(내가 그린 게 아님)
                        Image(
                            painter = painterResource(catIconRes(cat)),
                            contentDescription = null,
                            modifier = Modifier.requiredSize(40.dp),
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.requiredSize(10.dp))
                        androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                            androidx.compose.material3.Text(cat.label, color = catColor(cat), fontSize = 12.sp)
                            androidx.compose.material3.Text(
                                e.text, color = Color(0xFF1A2440), fontSize = 15.sp,
                                maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                        }
                        // 디자이너 원안 하트(채워짐/빈) 아이콘
                        Image(
                            painter = painterResource(if (e.favorite) R.drawable.heart_on else R.drawable.heart_off),
                            contentDescription = "즐겨찾기",
                            modifier = Modifier.requiredSize(26.dp)
                                .pointerInput(e.id) { detectTapGestures(onTap = { scope.launch { vm.c.expressions.toggleFavorite(e.id) } }) },
                        )
                    }
                }
            }
        }
    }
}

private fun catColor(c: kr.voicemate.malitda.domain.Category): Color = when (c) {
    kr.voicemate.malitda.domain.Category.NAME -> Color(0xFF41AFFC)
    kr.voicemate.malitda.domain.Category.PLACE -> Color(0xFF22B573)
    kr.voicemate.malitda.domain.Category.TIME -> Color(0xFFF2B300)
    kr.voicemate.malitda.domain.Category.MESSAGE -> Color(0xFFFF6B9D)
    kr.voicemate.malitda.domain.Category.OFTEN -> Color(0xFF8B5CF6)
}

/** 디자이너 원안에서 추출한 카테고리 아이콘 리소스. */
private fun catIconRes(c: kr.voicemate.malitda.domain.Category): Int = when (c) {
    kr.voicemate.malitda.domain.Category.NAME -> R.drawable.cat_name
    kr.voicemate.malitda.domain.Category.PLACE -> R.drawable.cat_place
    kr.voicemate.malitda.domain.Category.TIME -> R.drawable.cat_time
    kr.voicemate.malitda.domain.Category.MESSAGE -> R.drawable.cat_message
    kr.voicemate.malitda.domain.Category.OFTEN -> R.drawable.cat_often
}

/** 이미지의 입력칸 위에 얹는 실제 입력칸(흰 배경으로 예시 글자를 덮고 직접 타이핑). */
@Composable
private fun FigmaInput(value: String, onValue: (String) -> Unit, singleLine: Boolean, modifier: Modifier) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValue,
        singleLine = singleLine,
        textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF1A2440), fontSize = 15.sp),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF7545DC)),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        decorationBox = { inner ->
            Box(contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart) { inner() }
        },
    )
}

/** 이미지의 빈 체크박스 위에 얹는 실제 체크 표시(디자인 보라색 원형+흰 체크). */
@Composable
private fun CheckMark(modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(
            Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp))
                .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF6C5CE7), Color(0xFF9B5CFB)))),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(
                Icons.Rounded.Check,
                contentDescription = "선택됨",
                tint = Color.White,
                modifier = Modifier.fillMaxSize(0.8f),
            )
        }
    }
}

/** S22~S27: 계약 좌표가 없어 하단 주/보조 행동 2영역만 둔다(원안 프레임의 610/680 위치). */
val EXTRA_SPOTS: Map<String, List<Spot>> = mapOf(
    "S22" to twoActions("S15", "S15"),
    "S23" to twoActions("S13", "S11"),
    "S24" to twoActions("S12", "S18"),
    "S25" to twoActions("S06", "S08"),
    "S26" to twoActions("S21", "S11"),
    "S27" to twoActions("S15", "S15"),
)

private fun twoActions(primary: String, secondary: String): List<Spot> = listOf(
    Spot(24f / 390, 610f / 843.09f, 342f / 390, 54f / 843.09f, "primary", "주요 행동", "button", "navigate", primary),
    Spot(24f / 390, 680f / 843.09f, 342f / 390, 54f / 843.09f, "secondary", "보조 행동", "button", "navigate", secondary),
)
