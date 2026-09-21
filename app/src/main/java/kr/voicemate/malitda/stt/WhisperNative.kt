package kr.voicemate.malitda.stt

/** whisper.cpp JNI(app/src/main/cpp/whisper_jni.cpp). */
object WhisperNative {
    @Volatile private var loaded = false

    @Synchronized fun load(): Boolean {
        if (loaded) return true
        return runCatching { System.loadLibrary("malitda_whisper"); loaded = true; true }.getOrDefault(false)
    }

    @JvmStatic external fun init(modelPath: String): Long
    @JvmStatic external fun release(handle: Long)
    @JvmStatic external fun version(): String
    @JvmStatic external fun systemInfo(): String
    @JvmStatic external fun transcribe(handle: Long, pcm: FloatArray, lang: String, nThreads: Int, noSpeechThold: Float): String
}
