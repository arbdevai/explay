package com.explay.audio.audio.visualizer

import android.content.Context
import android.media.audiofx.Visualizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.max

data class VisualizerFrame(
    val magnitudes: FloatArray = FloatArray(24) { 0f },
    val peaks: FloatArray = FloatArray(24) { 0f },
    val hasAudioSignal: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VisualizerFrame
        if (!magnitudes.contentEquals(other.magnitudes)) return false
        if (!peaks.contentEquals(other.peaks)) return false
        if (hasAudioSignal != other.hasAudioSignal) return false
        return true
    }

    override fun hashCode(): Int {
        var result = magnitudes.contentHashCode()
        result = 31 * result + peaks.contentHashCode()
        result = 31 * result + hasAudioSignal.hashCode()
        return result
    }
}

class RealTimeAudioVisualizer(private val context: Context) {

    private var visualizer: Visualizer? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private var decayJob: Job? = null

    private val binCount = 24
    private val rawMagnitudes = FloatArray(binCount) { 0f }
    private val peakMagnitudes = FloatArray(binCount) { 0f }

    private val _visualizerFlow = MutableStateFlow(VisualizerFrame())
    val visualizerFlow: StateFlow<VisualizerFrame> = _visualizerFlow.asStateFlow()

    private var activeSessionId: Int = 0
    private var isRunning: Boolean = false

    fun start(sessionId: Int = 0) {
        this.activeSessionId = sessionId
        release()
        try {
            val captureRate = Visualizer.getMaxCaptureRate()
            val capSize = Visualizer.getCaptureSizeRange()[1].coerceAtMost(512)

            visualizer = Visualizer(sessionId).apply {
                captureSize = capSize
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        vis: Visualizer?,
                        waveform: ByteArray?,
                        samplingRate: Int
                    ) {
                        // Tidak digunakan untuk FFT frequency bar
                    }

                    override fun onFftDataCapture(
                        vis: Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {
                        if (fft != null) {
                            processFftData(fft)
                        }
                    }
                }, captureRate / 2, false, true)
                enabled = true
            }
            isRunning = true
            startDecayLoop()
            Log.i(TAG, "RealTime Visualizer aktif pada audio session: $sessionId")
        } catch (e: Exception) {
            Log.w(TAG, "Gagal menginisiasi Visualizer: ${e.message}")
            isRunning = false
            startDecayLoop()
        }
    }

    private fun processFftData(fft: ByteArray) {
        val n = fft.size / 2
        if (n <= 0) return

        var sumMagnitude = 0f
        val step = max(1, n / binCount)

        for (i in 0 until binCount) {
            val startIndex = (i * step).coerceAtMost(n - 1)
            val endIndex = ((i + 1) * step).coerceAtMost(n)
            var binMax = 0f

            for (j in startIndex until endIndex) {
                val rk = fft[2 * j].toFloat()
                val ik = fft[2 * j + 1].toFloat()
                val magnitude = hypot(rk, ik)
                if (magnitude > binMax) binMax = magnitude
            }

            // Normalisasi 0.0f .. 1.0f dengan kurva logaritmik lembut
            val normalized = (binMax / 75f).coerceIn(0f, 1f)
            rawMagnitudes[i] = normalized
            sumMagnitude += normalized

            // Update Peak Hold
            if (normalized >= peakMagnitudes[i]) {
                peakMagnitudes[i] = normalized
            }
        }

        val hasSignal = sumMagnitude > 0.08f
        _visualizerFlow.value = VisualizerFrame(
            magnitudes = rawMagnitudes.copyOf(),
            peaks = peakMagnitudes.copyOf(),
            hasAudioSignal = hasSignal
        )
    }

    private fun startDecayLoop() {
        decayJob?.cancel()
        decayJob = scope.launch {
            var phase = 0f
            while (isActive) {
                delay(33) // ~30 FPS untuk gravity decay & ambient wave

                val currentFrame = _visualizerFlow.value
                val hasSignal = currentFrame.hasAudioSignal

                if (!hasSignal) {
                    // Ambient breathing wave saat audio sedang jeda/diam
                    phase += 0.12f
                    for (i in 0 until binCount) {
                        val ambient = 0.08f + 0.12f * kotlin.math.sin(phase + i * 0.35f).toFloat()
                        rawMagnitudes[i] = max(0.04f, ambient)
                        peakMagnitudes[i] = max(rawMagnitudes[i] + 0.05f, peakMagnitudes[i] * 0.92f)
                    }
                } else {
                    // Decay normal untuk audio riil
                    for (i in 0 until binCount) {
                        rawMagnitudes[i] = max(0f, rawMagnitudes[i] * 0.82f)
                        peakMagnitudes[i] = max(rawMagnitudes[i], peakMagnitudes[i] - 0.025f)
                    }
                }

                _visualizerFlow.value = VisualizerFrame(
                    magnitudes = rawMagnitudes.copyOf(),
                    peaks = peakMagnitudes.copyOf(),
                    hasAudioSignal = hasSignal
                )
            }
        }
    }

    fun release() {
        decayJob?.cancel()
        decayJob = null
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (_: Exception) {}
        visualizer = null
        isRunning = false
    }

    companion object {
        private const val TAG = "RealTimeVisualizer"

        @Volatile
        private var instance: RealTimeAudioVisualizer? = null

        fun getInstance(context: Context): RealTimeAudioVisualizer {
            return instance ?: synchronized(this) {
                instance ?: RealTimeAudioVisualizer(context.applicationContext).also { instance = it }
            }
        }
    }
}
