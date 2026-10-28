package kr.voicemate.malitda.ui.v2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kr.voicemate.malitda.data.settings.ConfirmationMode
import kr.voicemate.malitda.domain.CommunicationSafety
import kr.voicemate.malitda.R
import kr.voicemate.malitda.data.db.MeaningEntity
import kr.voicemate.malitda.ui.vm.SessionViewModel.V2Origin

/** 출처 배지 문구. */
fun originBadge(origin: V2Origin) = if (origin == V2Origin.CARD) "카드로 고른 뜻" else "말로 전한 뜻"

/** 연보라 배지 줄(카드로 고른 뜻 / 말로 전한 뜻). */
@Composable
fun OriginBadge(origin: V2Origin) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(3.dp, RoundedCornerShape(22.dp), clip = false,
                ambientColor = V2C.Shadow.copy(alpha = 0.07f), spotColor = V2C.Shadow.copy(alpha = 0.12f))
            .background(Brush.horizontalGradient(if (V2C.highContrast) listOf(V2C.BannerBg, V2C.BannerBg)
                else listOf(V2C.BannerBg, V2C.CardInner, V2C.GreenBg.copy(alpha = 0.45f))), RoundedCornerShape(22.dp))
            .border(if (V2C.highContrast) 2.dp else 0.75.dp,
                if (V2C.highContrast) V2C.Ink else Color.White.copy(alpha = 0.85f), RoundedCornerShape(22.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (origin == V2Origin.CARD) CardGlyph(size = 20)
        else Image(painterResource(R.drawable.ic_consent_mic), null, Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(originBadge(origin), style = jua(17, V2C.Title))
    }
}

/** 큰 뜻 카드: 그림(있으면) + 문장. 그림이 없으면 U03-I 모습. */
@Composable
fun MeaningBigCard(m: MeaningEntity, noImageCaption: String? = null, imageAspect: Float = 1.42f, showPicture: Boolean = true) {
    V2Card {
        Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (showPicture && meaningImageAvailable(m.imageKey)) {
                MeaningPicture(m.imageKey, Modifier.fillMaxWidth().aspectRatio(imageAspect).clip(RoundedCornerShape(24.dp)))
                Spacer(Modifier.height(22.dp))
                Text(m.text, style = jua(30, V2C.Ink), textAlign = TextAlign.Center)
                Spacer(Modifier.height(26.dp))
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(V2C.CardInner, RoundedCornerShape(24.dp))
                        .padding(vertical = 44.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CardGlyph(size = 44)
                    Spacer(Modifier.height(30.dp))
                    Text(m.text, style = jua(30, V2C.Ink), textAlign = TextAlign.Center)
                }
                // 일부러 그림 없이 등록한 뜻에는 '불러올 수 없어요'를 띄우지 않는다.
                if (showPicture && noImageCaption != null && !m.imageKey.isNullOrBlank()) {
                    Spacer(Modifier.height(18.dp))
                    Text(noImageCaption, style = jua(16, V2C.Muted, 1.5f), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

/** U03 · 이 뜻이 맞나요?(화면 04·07·09·11, 그림 없으면 17). */
@Composable
fun U03Confirm(
    meaning: MeaningEntity,
    origin: V2Origin,
    studentName: String,
    confirming: Boolean = false,
    preferredMode: ConfirmationMode = ConfirmationMode.PICTURE,
    heard: String? = null,
    onEdit: () -> Unit = {},
    onListen: () -> Unit,
    onYes: () -> Unit,
    onOther: () -> Unit,
    onUnsure: () -> Unit,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onEasyEdit: ((String) -> Unit)? = null,
    onListenText: (String) -> Unit = {},
    onStopReading: () -> Unit = {},
) {
    V2Screen {
        V2Header(onBack = onBack, onHelp = onHelp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            V2Title("이 뜻이 맞나요?")
            Spacer(Modifier.height(6.dp))
            OriginBadge(origin)
            Spacer(Modifier.height(14.dp))
            MeaningBigCard(meaning, noImageCaption = "그림을 불러올 수 없습니다. 문장이나 소리로 확인합니다.", showPicture = preferredMode == ConfirmationMode.PICTURE)
            heard?.let { V2Banner("들은 말 · 원문\n$it") }
            if (CommunicationSafety.sensitiveChange(heard, meaning.text)) V2Banner("숫자나 부정 표현이 달라졌습니다. 원하는 뜻인지 다시 확인합니다.")
            if (preferredMode == ConfirmationMode.TTS) V2SubTitle("소리로 확인하기를 눌러 먼저 들을 수 있습니다.")
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .padding(horizontal = 20.dp)
                    .background(Brush.horizontalGradient(if (V2C.highContrast) listOf(V2C.CardInner, V2C.CardInner)
                        else listOf(V2C.CardInner, V2C.BannerBg)), RoundedCornerShape(16.dp))
                    .border(if (V2C.highContrast) 2.dp else 0.75.dp,
                        if (V2C.highContrast) V2C.Ink else Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) { Text(if (meaning.isSample) "연습용 예시 · 개인 확인 전" else "${withJosa(studentName)} 함께 등록한 뜻", style = jua(14, Color(0xFF6B6485))) }
            Spacer(Modifier.height(14.dp))
            V2SecondaryButton("소리로 확인하기") { onListen() }
            V2TextButton("문장 직접 고치기", onClick = onEdit)
            if (onEasyEdit != null) EasyTextOffer(meaning.text, onUse = onEasyEdit, onListen = onListenText, enabled = !confirming, onStop = onStopReading)
            Spacer(Modifier.height(16.dp))
        }
        V2PrimaryButton(if (confirming) "확인 중…" else "맞아요", icon = "✓", enabled = !confirming) { onYes() }
        Spacer(Modifier.height(12.dp))
        V2SecondaryButton("다른 뜻이에요") { onOther() }
        V2TextButton("잘 모르겠어요") { onUnsure() }
        Spacer(Modifier.height(8.dp))
    }
}

/** 판단을 유보해도 전달되지 않는다. 도움을 받아 다시 보거나 다른 카드·종료를 선택한다. */
@Composable
fun U03Unsure(meaning: MeaningEntity?, onListen: () -> Unit, onRecheck: () -> Unit,
    onPick: () -> Unit, onStop: () -> Unit, onHelp: () -> Unit, literalText: String? = null) {
    V2Screen {
        V2Header(onBack = onRecheck, onHelp = onHelp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            V2Title("천천히 다시 확인해요")
            V2Banner("이번 뜻 확인을 잠시 미뤘습니다.\n그림이나 소리로 다시 확인할 수 있습니다.")
            Spacer(Modifier.height(16.dp))
            meaning?.let { MeaningBigCard(it, imageAspect = 2f) }
            literalText?.takeIf { it.isNotBlank() }?.let {
                V2Card { Text(it,style=jua(26,V2C.Ink),modifier=Modifier.padding(20.dp)) }
            }
            Spacer(Modifier.height(16.dp))
            V2SecondaryButton("소리 다시 듣기", onClick = onListen)
            Spacer(Modifier.height(14.dp))
            V2SubTitle("도움이 필요하면 익숙한 사람과 함께 확인해요.\n모르겠다면 여기서 멈춰도 괜찮아요.")
        }
        V2PrimaryButton("함께 다시 확인", onClick = onRecheck)
        Spacer(Modifier.height(10.dp))
        V2SecondaryButton("다른 카드 고르기", onClick = onPick)
        V2TextButton("그만하기", onClick = onStop)
    }
}

/** U03-E · 잘 듣지 못했어요(화면 15: 인식 결과 없음·오류 공용). */
@Composable
fun U03NoHear(
    onRespeak: () -> Unit,
    onPickCards: () -> Unit,
    onCancel: () -> Unit,
    onHelp: () -> Unit,
    onDirect: () -> Unit = {},
) {
    V2Screen {
        V2Header(onBack = onCancel, onHelp = onHelp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            V2Title("잘 듣지 못했어요")
            Spacer(Modifier.height(12.dp))
            CharacterCard(
                chip = { Image(painterResource(R.drawable.ic_consent_mic), null, Modifier.size(24.dp)) },
                caption = null,
            )
            Spacer(Modifier.height(16.dp))
            V2Banner("다시 말하거나,\n카드로 뜻을 골라 주세요.", leading = { CardGlyph() })
            Spacer(Modifier.height(16.dp))
        }
        V2PrimaryButton("다시 말하기", icon = "🎙") { onRespeak() }
        Spacer(Modifier.height(12.dp))
        V2SecondaryButton("카드로 고르기") { onPickCards() }
        V2TextButton("문장 직접 입력", onClick = onDirect)
        V2TextButton("취소") { onCancel() }
        Spacer(Modifier.height(8.dp))
    }
}

/** U03-M · 이 말의 뜻을 아직 모르겠어요(화면 16). */
@Composable
fun U03NoMeaning(
    origin: V2Origin,
    heard: String?,
    onPickCards: () -> Unit,
    onRegister: () -> Unit,
    onRespeak: () -> Unit,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onDirect: () -> Unit = {},
) {
    V2Screen {
        V2Header(onBack = onBack, onHelp = onHelp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            V2Title("이 말의 뜻을\n아직 모르겠어요")
            Spacer(Modifier.height(6.dp))
            OriginBadge(origin)
            if (!heard.isNullOrBlank()) {
                // 앱이 알아들은 말을 보여 준다 — 지원자가 어떤 표현을 등록할지 판단할 수 있게.
                Spacer(Modifier.height(10.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .background(V2C.Surface, RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    Text("들은 말", style = jua(14, V2C.Muted))
                    Spacer(Modifier.height(4.dp))
                    Text("“$heard”", style = jua(22, V2C.Ink))
                }
            }
            Spacer(Modifier.height(14.dp))
            V2Card {
                Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painterResource(R.drawable.v2_char_purple), null,
                        Modifier.size(170.dp).clip(RoundedCornerShape(22.dp)),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(Modifier.height(18.dp))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                            .background(Color(0xFFF2EFF9), RoundedCornerShape(22.dp))
                            .padding(22.dp),
                    ) {
                        CardGlyph(size = 26)
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "함께 등록한 뜻만 보여드려요.\n지원자와 뜻을 등록하거나,\n다른 카드로 골라 주세요.",
                            style = jua(18, V2C.Ink, 1.6f),
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        V2PrimaryButton("카드로 고르기") { onPickCards() }
        Spacer(Modifier.height(12.dp))
        V2SecondaryButton("함께 뜻 등록하기") { onRegister() }
        V2TextButton("문장 직접 입력", onClick = onDirect)
        V2TextButton("다시 말하기") { onRespeak() }
        Spacer(Modifier.height(8.dp))
    }
}

/** U03-V · 뜻이 바뀌었어요(화면 18). */
@Composable
fun U03Changed(
    onRecheck: () -> Unit,
    onHome: () -> Unit,
    onHelp: () -> Unit,
) {
    V2Screen {
        V2Header(onBack = onHome, onHelp = onHelp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            V2Title("뜻이 바뀌었어요")
            Spacer(Modifier.height(12.dp))
            CharacterCard(
                chip = { Icon(Icons.Rounded.Refresh, null, tint = V2C.BannerIcon, modifier = Modifier.size(26.dp)) },
                caption = null,
            )
            Spacer(Modifier.height(16.dp))
            V2Banner("등록한 뜻이 수정되었어요.\n이전 선택은 사용할 수 없어요.", icon = Icons.Rounded.Refresh)
            Spacer(Modifier.height(18.dp))
            Text(
                "바뀐 그림과 문장을\n다시 확인해 주세요.",
                style = jua(20, V2C.Ink, 1.5f), textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
        }
        V2PrimaryButton("다시 뜻 확인하기", icon = "⟳") { onRecheck() }
        Spacer(Modifier.height(12.dp))
        V2SecondaryButton("홈으로 가기") { onHome() }
        Spacer(Modifier.height(16.dp))
    }
}
