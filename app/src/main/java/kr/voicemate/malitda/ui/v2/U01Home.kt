package kr.voicemate.malitda.ui.v2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kr.voicemate.malitda.R
import kr.voicemate.malitda.data.db.MeaningEntity
import kr.voicemate.malitda.domain.HomeMeaningsOrder

/** U01 · 학생 홈(화면 01) / U01-E · 등록 없는 홈(화면 13). */
@Composable
fun U01Home(
    name: String,
    meanings: List<MeaningEntity>,
    onSpeak: () -> Unit,
    onCard: (MeaningEntity) -> Unit,
    onRegister: () -> Unit,
    onGuide: () -> Unit,
    onHelp: () -> Unit,
    onLibrary: () -> Unit,
    onSettings: () -> Unit,
    onDirectInput: () -> Unit = {},
    totalRegistered: Int = meanings.count { !it.isSample },
    lastModified: Long? = null,
    voiceStatus: String? = null,
    onRetryVoice: (() -> Unit)? = null,
) {
    V2Screen(background = Color(0xFFFDFAF6)) {
        V2Header(onHelp = onHelp, logoStart = true)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            if (meanings.isEmpty()) EmptyHome(onRegister, onGuide, onSpeak, onDirectInput)
            else {
                FilledHome(name, meanings, onSpeak, onCard, onRegister)
                V2SecondaryButton("문장 직접 입력", onClick = onDirectInput)
            }
            if (voiceStatus != null) V2SubTitle(voiceStatus)
            if (onRetryVoice != null) V2TextButton("음성인식 다시 준비", onClick = onRetryVoice)
            lastModified?.let { time ->
                V2SubTitle("최근 수정 " + java.text.SimpleDateFormat("M월 d일 HH:mm", java.util.Locale.KOREA).format(java.util.Date(time)))
            }
            V2SubTitle("개인 뜻 ${totalRegistered}/50개 · 표현 등록과 자료 관리는 함께 관리에서 확인")
            Spacer(Modifier.height(16.dp))
        }
        V2BottomNav(selected = "home", onHome = {}, onLibrary = onLibrary, onSettings = onSettings, onRegister = onRegister)
    }
}

@Composable
private fun FilledHome(
    name: String,
    meanings: List<MeaningEntity>,
    onSpeak: () -> Unit,
    onCard: (MeaningEntity) -> Unit,
    onRegister: () -> Unit,
) {
    val large = androidx.compose.ui.platform.LocalDensity.current.fontScale >= 1.4f
    Spacer(Modifier.height(4.dp))
    // PDF S08의 인사·캐릭터·원형 마이크 구성을 유지한다. 수치는 실제 개인 사전에서만 읽는다.
    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("${name.ifBlank { "친구" }}님, 안녕하세요", style = jua(23, V2C.Ink), modifier = Modifier.weight(1f))
        if (!large) { Spacer(Modifier.width(12.dp)); Image(painterResource(R.drawable.char_purple), null, Modifier.size(66.dp), contentScale = ContentScale.Fit) }
    }
    Spacer(Modifier.height(8.dp))
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Text("내 표현으로, 내 뜻을 전해요", style = jua(20, V2C.Ink, 1.25f))
        Spacer(Modifier.height(3.dp))
        Text("말하기·그림 선택 → 뜻 확인 → 전달", style = jua(13, V2C.Muted, 1.38f))
    }
    Spacer(Modifier.height(8.dp))
    val personalCount = meanings.count { !it.isSample }
    V2Card(radius = 26) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFFF6EFFB), Color(0xFFEFF2FC))))
                .v2PorcelainFinish(RoundedCornerShape(26.dp))
                .clickable(role = Role.Button, onClick = onRegister)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f).padding(vertical = 10.dp)) {
                Text(if (personalCount > 0) "함께 확인한 뜻" else "처음에는 연습 카드로", style = jua(16, V2C.Ink))
                Spacer(Modifier.height(4.dp))
                Text(if (personalCount > 0) "${personalCount}개가 있어요" else "천천히 시작해요", style = jua(21, V2C.Title))
                Spacer(Modifier.height(4.dp))
                Text("내 표현 함께 준비  ›", style = jua(14, V2C.Muted))
            }
            if (!large) Image(
                painterResource(R.drawable.char_robot_mint), contentDescription = null,
                modifier = Modifier.size(86.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    // At large text sizes, keep the microphone label above the first fold.
    // Decorative images and a larger circle must not displace the action name.
    val microphonePress = rememberV2Press()
    val useMicSurfaceAsset = !large && !V2C.highContrast
    Box(
        Modifier.fillMaxWidth().height(if (large) 228.dp else 182.dp)
            .drawBehind {
                if (!V2C.highContrast) {
                    val radius = size.minDimension * 0.64f
                    val cyan = Offset(size.width * 0.61f, size.height * 0.48f)
                    val lavender = Offset(size.width * 0.42f, size.height * 0.61f)
                    drawCircle(Brush.radialGradient(listOf(Color(0x1456C8E8), Color.Transparent), center = cyan, radius = radius), radius, cyan)
                    drawCircle(Brush.radialGradient(listOf(Color(0x169C7AEF), Color.Transparent), center = lavender, radius = radius), radius, lavender)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (!large) {
            Image(painterResource(R.drawable.char_green), null,
                Modifier.align(Alignment.BottomStart).size(64.dp), contentScale = ContentScale.Fit)
            Image(painterResource(R.drawable.char_water), null,
                Modifier.align(Alignment.BottomEnd).size(64.dp), contentScale = ContentScale.Fit)
        }
        Box(
            Modifier.then(microphonePress.modifier).size(if (large) 212.dp else 170.dp)
                .shadow(14.dp, CircleShape, clip = false, ambientColor = if (V2C.highContrast) Color.Black else Color(0x266D61B5), spotColor = V2C.Shadow)
                .background(Color.White, CircleShape)
                .v2PorcelainFinish(CircleShape)
                .padding(9.dp)
                .background(Brush.linearGradient(if (V2C.highContrast) listOf(V2C.GradStart, V2C.GradEnd) else listOf(Color(0xFF5C85E5), Color(0xFF8B5CE7), Color(0xFFBA58D8))), CircleShape)
                .clip(CircleShape)
                .then(if (useMicSurfaceAsset) Modifier else Modifier.v2MicrophoneResin())
                .clickable(interactionSource = microphonePress.interaction, indication = androidx.compose.material3.ripple(), role = Role.Button, onClick = onSpeak)
                .semantics { contentDescription = "눌러서 말하기" },
            contentAlignment = Alignment.Center,
        ) {
            if (useMicSurfaceAsset) {
                Image(
                    painterResource(R.drawable.mic_surface_c), contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape).v2MicAssetWash(),
                    contentScale = ContentScale.Fit,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.ic_mic_white), null, Modifier.size(42.dp))
                Spacer(Modifier.height(10.dp))
                Text(if (large) "말하기" else "눌러서 말하기", style = jua(21, Color.White))
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(
        "그림으로 뜻 고르기",
        style = jua(20, V2C.Ink),
        modifier = Modifier.padding(horizontal = 24.dp),
    )
    Spacer(Modifier.height(12.dp))
    // 의미 카드 2열 격자
    HomeMeaningsOrder.personalFirst(meanings).chunked(if (large) 1 else 2).forEach { rowItems ->
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            rowItems.forEach { m -> MeaningCard(m, Modifier.weight(1f)) { onCard(m) } }
            if (!large && rowItems.size == 1) Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
    }
    Spacer(Modifier.height(6.dp))
    V2SecondaryButton("⊕  지원자와 함께 등록") { onRegister() }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun MeaningCard(m: MeaningEntity, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val press = rememberV2Press()
    Column(
        modifier.then(press.modifier)
            .shadow(6.dp, RoundedCornerShape(24.dp), clip = false, ambientColor = if (V2C.highContrast) Color.Black else Color(0x1A5F5277), spotColor = if (V2C.highContrast) V2C.Shadow else Color(0x2E5F5277))
            .background(V2C.Surface, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .v2PorcelainFinish(RoundedCornerShape(24.dp), lavenderBase = true)
            .clickable(interactionSource = press.interaction, indication = androidx.compose.material3.ripple(), role = Role.Button, onClick = onClick)
            .padding(10.dp)
            .semantics { contentDescription = m.text },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MeaningPicture(m.imageKey, Modifier.fillMaxWidth().aspectRatio(1.42f).clip(RoundedCornerShape(18.dp)))
        Spacer(Modifier.height(8.dp))
        Text(
            m.text, style = jua(17, V2C.Ink), textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        if (m.isSample) Text("연습용 예시", style = jua(13, V2C.Muted), modifier = Modifier.padding(bottom = 4.dp))
    }
}

/** U01-E · 아직 확인한 뜻이 없는 홈. */
@Composable
private fun EmptyHome(onRegister: () -> Unit, onGuide: () -> Unit, onSpeak: () -> Unit, onDirectInput: () -> Unit) {
    Spacer(Modifier.height(4.dp))
    V2Title("내 뜻을 전해요")
    V2SubTitle("말하거나 직접 입력해 시작합니다.")
    Spacer(Modifier.height(16.dp))
    V2PrimaryButton("말하기", onClick = onSpeak)
    Spacer(Modifier.height(12.dp))
    V2SecondaryButton("문장 직접 입력", onClick = onDirectInput)
    Spacer(Modifier.height(24.dp))
    V2Card {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFFFBF2F8), Color(0xFFF0F3FC))))
                .padding(24.dp),
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .shadow(3.dp, CircleShape, clip = false, spotColor = V2C.Shadow)
                    .background(V2C.Surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.AddCircle, null, tint = V2C.BannerIcon, modifier = Modifier.size(26.dp)) }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(12.dp))
                Image(
                    painterResource(R.drawable.v2_char_robot), contentDescription = null,
                    modifier = Modifier
                        .size(110.dp)
                        .background(V2C.CardInner, RoundedCornerShape(28.dp))
                        .padding(14.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    V2Banner("자주 쓰는 표현은 지원자와 함께 등록할 수 있습니다.", leading = { CardGlyph() })
    Spacer(Modifier.height(18.dp))
    V2SecondaryButton("표현 함께 등록하기") { onRegister() }
    Spacer(Modifier.height(12.dp))
    V2SecondaryButton("처음 화면 안내") { onGuide() }
    Spacer(Modifier.height(16.dp))
}
