package kr.voicemate.malitda.stt

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * assets/<assetRoot> → filesDir/<target> 복사. 버전 표식 파일이 일치하면 건너뛴다.
 * 네트워크를 전혀 쓰지 않는다(모델은 APK에 동봉).
 */
class ModelInstaller(
    private val context: Context,
    private val assetRoot: String,
    private val target: String,
    private val version: String,
    private val checkFile: String,
) {
    val targetDir: File get() = File(context.filesDir, target)

    fun isInstalled(): Boolean {
        val marker = File(targetDir, MARKER)
        return marker.exists() && marker.readText().trim() == version && File(targetDir, checkFile).exists()
    }

    fun installedBytes(): Long = if (targetDir.exists()) targetDir.walkTopDown().filter { it.isFile && it.name != MARKER }.sumOf { it.length() } else 0L

    suspend fun install(onProgress: (done: Int, total: Int) -> Unit) = withContext(Dispatchers.IO) {
        if (isInstalled()) return@withContext
        val am = context.assets
        val files = ArrayList<String>()
        fun walk(path: String) {
            val children = am.list(path) ?: emptyArray()
            if (children.isEmpty()) { files += path; return }
            for (c in children) walk("$path/$c")
        }
        walk(assetRoot)
        if (files.isEmpty()) throw IllegalStateException("APK 안에 모델($assetRoot)이 없어요")
        val tmp = File(context.filesDir, "$target.tmp")
        tmp.deleteRecursively(); tmp.mkdirs()
        files.forEachIndexed { i, asset ->
            val rel = asset.removePrefix("$assetRoot/")
            val out = File(tmp, rel); out.parentFile?.mkdirs()
            am.open(asset).use { input -> out.outputStream().use { output -> input.copyTo(output, 1 shl 16) } }
            onProgress(i + 1, files.size)
        }
        File(tmp, MARKER).writeText(version)
        targetDir.deleteRecursively()
        if (!tmp.renameTo(targetDir)) throw IllegalStateException("모델 폴더 이동 실패")
    }

    companion object {
        const val MARKER = ".version"
        const val VOSK_MODEL_VERSION = "vosk-model-small-ko-0.22"
        const val WHISPER_MODEL_VERSION = "ggml-base-q5_1"
        const val WHISPER_MODEL_FILE = "ggml-base-q5_1.bin"

        fun vosk(context: Context) = ModelInstaller(context, "model-ko", "model-ko", VOSK_MODEL_VERSION, "am/final.mdl")
        fun whisper(context: Context) = ModelInstaller(context, "whisper", "whisper", WHISPER_MODEL_VERSION, WHISPER_MODEL_FILE)
    }
}
