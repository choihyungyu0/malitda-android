package kr.voicemate.malitda.metrics

import android.content.Context
import android.net.TrafficStats
import android.os.Debug
import android.os.Process
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** 기획서의 단말 지표: 처리시간·최대 메모리·모델 크기·송수신량. 개인정보는 담지 않는다. */
data class DeviceMetrics(
    val rxBytes: Long,
    val txBytes: Long,
    val currentPssKb: Long,
    val peakPssKb: Long,
    val lastProcessingMs: Long?,
    val lastAudioMs: Long?,
    val modelLoadMs: Long?,
    val modelBytes: Long?,
    val sttRuns: Int,
    val withinAudioLength: Int,
)

class Metrics(context: Context) {
    private val uid = Process.myUid()
    private var peakPss = 0L
    private var lastProcessing: Long? = null
    private var lastAudio: Long? = null
    private var runs = 0
    private var within = 0
    var modelLoadMs: Long? = null
    var modelBytes: Long? = null

    private val _snapshot = MutableStateFlow(snapshot())
    val snapshot: StateFlow<DeviceMetrics> = _snapshot

    fun onRecognition(processingMs: Long, audioMs: Long) {
        lastProcessing = processingMs; lastAudio = audioMs; runs += 1
        if (processingMs <= audioMs) within += 1
        refresh()
    }

    fun refresh() {
        val pss = runCatching { Debug.getPss() }.getOrDefault(0L)
        if (pss > peakPss) peakPss = pss
        _snapshot.value = snapshot()
    }

    private fun snapshot(): DeviceMetrics {
        val rx = TrafficStats.getUidRxBytes(uid).let { if (it == TrafficStats.UNSUPPORTED.toLong()) 0L else it }
        val tx = TrafficStats.getUidTxBytes(uid).let { if (it == TrafficStats.UNSUPPORTED.toLong()) 0L else it }
        val pss = runCatching { Debug.getPss() }.getOrDefault(0L)
        return DeviceMetrics(rx, tx, pss, maxOf(peakPss, pss), lastProcessing, lastAudio, modelLoadMs, modelBytes, runs, within)
    }
}

fun formatBytes(b: Long): String = when {
    b < 1024 -> "$b B"
    b < 1024 * 1024 -> "%.1f KB".format(b / 1024.0)
    b < 1024L * 1024 * 1024 -> "%.1f MB".format(b / (1024.0 * 1024))
    else -> "%.2f GB".format(b / (1024.0 * 1024 * 1024))
}
