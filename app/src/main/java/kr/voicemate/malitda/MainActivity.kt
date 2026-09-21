package kr.voicemate.malitda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.voicemate.malitda.ui.figma.FigmaApp
import kr.voicemate.malitda.ui.theme.A11yPrefs
import kr.voicemate.malitda.ui.theme.MalitdaTheme
import kr.voicemate.malitda.ui.vm.SessionViewModel

class MainActivity : ComponentActivity() {
    private val vm: SessionViewModel by viewModels { SessionViewModel.factory((application as MalitdaApp).container) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by vm.settings.collectAsStateWithLifecycle()
            MalitdaTheme(
                fontScale = settings.fontScale,
                a11y = A11yPrefs(visualEmphasis = settings.visualEmphasis, haptics = settings.haptics, ttsRate = settings.ttsRate),
            ) {
                // Figma 목업 이미지를 그대로 쓰는 이미지 기반 화면(디자이너 프로토타입 방식)
                FigmaApp(vm, start = "S01")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        vm.stopSpeaking()
    }
}
