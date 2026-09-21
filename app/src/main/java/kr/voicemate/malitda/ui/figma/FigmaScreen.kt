package kr.voicemate.malitda.ui.figma

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** 원안 프레임 비율(390 × 843.09). */
private const val FRAME_W = 390f
private const val FRAME_H = 843.09496f

/**
 * Figma 목업 이미지를 그대로 배경으로 깔고, control-contract.json 좌표의 투명 터치영역을 얹는다.
 * 글자·버튼을 다시 그리지 않고 디자인 이미지를 그대로 사용한다.
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
        BoxWithConstraints(
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            val wDp = maxWidth.value
            val hDp = wDp * (FRAME_H / FRAME_W)
            Box(Modifier.requiredSize(wDp.dp, hDp.dp)) {
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
                                .offset((s.x * wDp).dp, (s.y * hDp).dp)
                                .requiredSize((s.w * wDp).dp, (s.h * hDp).dp)
                                .pointerInput(s.id) { detectTapGestures(onTap = { onSpot(s) }) }
                                .semantics {
                                    contentDescription = if (s.label.isNotBlank()) s.label else s.id
                                    role = Role.Button
                                    onClick(label = "실행") { onSpot(s); true }
                                },
                        )
                    }
                }
                overlay?.invoke(wDp, hDp)
            }
        }
    }
}
