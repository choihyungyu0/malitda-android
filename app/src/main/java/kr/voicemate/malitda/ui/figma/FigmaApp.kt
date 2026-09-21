package kr.voicemate.malitda.ui.figma

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxHeight
import kr.voicemate.malitda.ui.vm.SessionViewModel

/**
 * Figma 목업 이미지를 그대로 화면으로 쓰는 이미지 기반 앱.
 * control-contract.json 좌표의 투명 터치영역으로 화면을 전환한다(디자이너 프로토타입과 동일).
 * S22~S27은 계약 좌표가 없어 하단 주/보조 행동 영역만 연결한다.
 */
@Composable
fun FigmaApp(vm: SessionViewModel, start: String = "S01") {
    val context = LocalContext.current
    var current by rememberSaveable { mutableStateOf(start) }
    var overlay by rememberSaveable { mutableStateOf<String?>(null) }
    var history by rememberSaveable { mutableStateOf(listOf<String>()) }
    // 이미지 위에 얹는 실제 상태(체크박스 토글 등). id = "화면/스팟".
    var checked by rememberSaveable { mutableStateOf(setOf<String>()) }
    fun isChecked(id: String) = "$current/$id" in checked
    fun toggle(id: String) { val k = "$current/$id"; checked = if (k in checked) checked - k else checked + k }

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
            a.startsWith("candidate") -> { /* 정적 이미지: 선택 표시 없음 */ }
            a == "confirm_selection" -> go("S11")
            a == "confirm_message" -> go("S12")
            a == "approve" -> go("S21")
            a == "unapprove" -> go("S12")
            a.startsWith("share_gate") -> {
                runCatching {
                    val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "와, 오늘 날씨 너무 좋다!") }
                    context.startActivity(Intent.createChooser(send, "공유할 앱 선택"))
                }
                go("S26")
            }
            a == "save_expression" -> go("S13")
            a == "choose_registered" -> go("S13")
            a == "new_expression" -> go("S07")
            a == "edit_expression" -> go("S07")
            a == "edit_reset" -> go(t ?: "S11")
            a == "select_character" -> go("S08")
            a == "clear:expression" -> { /* 초안 지우기: 정적 */ }
            a.startsWith("category:") || a.startsWith("filter:") || a.startsWith("favorite:") ||
                a.startsWith("speed:") || a.startsWith("toggle:") || a.startsWith("quick:") ||
                a == "reset_settings" || a == "inputmode:text" -> { t?.let { go(it) } }
            a == "start" || a == "navigate" -> t?.let { go(it) }
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
