package kr.voicemate.malitda.ui.figma

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * 온보딩 흐름 데모: S01(시작) → S02(안내) → S03(동의).
 * 구현정본 네이티브 재현 검증용. 실제 내비게이션·엔진 연결은 다음 단계.
 */
@Composable
fun OnboardingDemo() {
    var screen by rememberSaveable { mutableStateOf("S01") }
    var notice by rememberSaveable { mutableStateOf(false) }
    var privacy by rememberSaveable { mutableStateOf(false) }
    val ctx = LocalContext.current

    when (screen) {
        "S01" -> S01Splash(onStart = { screen = "S02" })
        "S02" -> S02Guide(onNext = { screen = "S03" }, onSkip = { screen = "S03" })
        "S03" -> S03Consent(
            checkedNotice = notice,
            checkedPrivacy = privacy,
            onToggleNotice = { notice = !notice },
            onTogglePrivacy = { privacy = !privacy },
            onContinue = {
                if (notice && privacy) {
                    Toast.makeText(ctx, "온보딩 완료 · 홈으로 이동", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(ctx, "두 항목에 모두 동의해야 시작할 수 있어요", Toast.LENGTH_SHORT).show()
                }
            },
            onLater = { screen = "S01" },
            onViewPolicy = { Toast.makeText(ctx, "개인정보 처리방침 보기", Toast.LENGTH_SHORT).show() },
        )
    }
}
