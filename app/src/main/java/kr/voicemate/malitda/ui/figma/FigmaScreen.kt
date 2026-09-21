package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.min

/** 원안 프레임 비율(390 × 843.09). */
private const val FRAME_W = 390f
private const val FRAME_H = 843.09496f

/**
 * Figma 목업 이미지를 화면에 **전체가 보이도록 비율 유지·가운데 정렬**해 배경으로 깔고,
 * control-contract.json 좌표의 투명 터치영역을 이미지에 정확히 맞춰 얹는다.
 * 스크롤 없이 프레임 전체를 표시하므로 하단 버튼도 화면 안에 들어오고 좌표가 어긋나지 않는다.
 * 접근성을 위해 각 터치영역에 라벨(contentDescription)을 부여해 TalkBack이 읽을 수 있게 한다.
 */
@Composable
fun FigmaScreen(
    imageRes: Int,
    spots: List<Spot>,
    modifier: Modifier = Modifier,
    onSpot: (Spot) -> Unit,
    overlay: (@Composable (imgWidthDp: Float, imgHeightDp: Float) -> Unit)? = null,
) {
    Surface(color = Color(0xFFF6F4F0)) {
        BoxWithConstraints(modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val availW: Float
            val availH: Float
            with(density) { availW = maxWidth.toPx(); availH = maxHeight.toPx() }
            val scale = min(availW / FRAME_W, availH / FRAME_H)
            val imgWpx = FRAME_W * scale
            val imgHpx = FRAME_H * scale
            val offXpx = (availW - imgWpx) / 2f
            val offYpx = (availH - imgHpx) / 2f
            val imgWdp: Float; val imgHdp: Float; val offXdp: Float; val offYdp: Float
            with(density) {
                imgWdp = imgWpx.toDp().value; imgHdp = imgHpx.toDp().value
                offXdp = offXpx.toDp().value; offYdp = offYpx.toDp().value
            }
            Box(
                Modifier
                    .offset(offXdp.dp, offYdp.dp)
                    .requiredSize(imgWdp.dp, imgHdp.dp),
            ) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                spots.forEach { s ->
                    key(s.id) {
                        Box(
                            Modifier
                                .offset((s.x * imgWdp).dp, (s.y * imgHdp).dp)
                                .requiredSize((s.w * imgWdp).dp, (s.h * imgHdp).dp)
                                .pointerInput(s.id) { detectTapGestures(onTap = { onSpot(s) }) }
                                .semantics {
                                    contentDescription = if (s.label.isNotBlank()) s.label else s.id
                                    role = Role.Button
                                    onClick(label = "실행") { onSpot(s); true }
                                },
                        )
                    }
                }
                overlay?.invoke(imgWdp, imgHdp)
            }
        }
    }
}
