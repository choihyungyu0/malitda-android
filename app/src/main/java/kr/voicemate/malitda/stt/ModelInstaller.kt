package kr.voicemate.malitda.stt

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * assets/model-ko → filesDir/model-ko 복사. 버전 표식 파일이 일치하면 건너뛴다.
 * 네트워크를 전혀 쓰지 않는다(모델은 APK에 동봉).
 */
class ModelInstaller(private val context: Context) {
    val targetDir: File get() = File(context.filesDir, TARGET)

    fun isInstalled(): Boolean {
        val marker = File(targetDir, MARKER)
        return marker.exists() && marker.readText().trim() == MODEL_VERSION && File(targetDir, "am/final.mdl").exists()
    }

    fun installedBytes(): Long = if (targetDir.exists()) targetDir.walkTopDown().filter { it.isFile }.sumOf { it.length() } else 0L

    suspend fun install(onProgress: (done: Int, total: Int) -> Unit) = withContext(Dispatchers.IO) {
        if (isInstalled()) return@withContext
        val am = context.assets
        val files = ArrayList<String>()
        fun walk(path: String) {
            val children = am.list(path) ?: emptyArray()
            if (children.isEmpty()) { files += path; return }
            for (c in children) walk("$path/$c")
        }
        walk(ASSET_ROOT)
        val tmp = File(context.filesDir, "$TARGET.tmp")
        tmp.deleteRecursively(); tmp.mkdirs()
        files.forEachIndexed { i, asset ->
            val rel = asset.removePrefix("$ASSET_ROOT/")
            val out = File(tmp, rel); out.parentFile?.mkdirs()
            am.open(asset).use { input -> out.outputStream().use { output -> input.copyTo(output, 1 shl 16) } }
            onProgress(i + 1, files.size)
        }
        File(tmp, MARKER).writeText(MODEL_VERSION)
        targetDir.deleteRecursively()
        if (!tmp.renameTo(targetDir)) throw IllegalStateException("모델 폴더 이동 실패")
    }

    companion object {
        const val ASSET_ROOT = "model-ko"
        const val TARGET = "model-ko"
        const val MARKER = ".version"
        const val MODEL_VERSION = "vosk-model-small-ko-0.22"
    }
}
