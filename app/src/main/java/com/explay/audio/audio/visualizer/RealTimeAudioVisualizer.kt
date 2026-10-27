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
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

data class VisualizerFrame(
    val magnitudes: FloatArray = FloatArray(24) { 0f },
    val peaks: FloatArray = FloatArray(24) { 0f },
    val hasAudioSignal: Boolean = false,
    val rmsLevelDb: Float = -40f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VisualizerFrame
        if (!magnitudes.contentEquals(other.magnitudes)) return false
        if (!peaks.contentEquals(other.peaks)) return false
        if (hasAudioSignal != other.hasAudioSignal) return false
        if (rmsLevelDb != other.rmsLevelDb) return false
        return true
    }

    override fun hashCode(): Int {
        var result = magnitudes.contentHashCode()
        result = 31 * result + peaks.contentHashCode()
        result = 31 * result + hasAudioSignal.hashCode()
        result = 31 * result + rmsLevelDb.hashCode()
        return result
    }
}

class RealTimeAudioVisualizer(private val context: Context) {

    private var visualizer: Visualizer? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private var decayJob: Job? = null

    private val binCount = 24
    private val currentMagnitudes = FloatArray(binCount) { 0f }
    private val peakMagnitudes = FloatArray(binCount) { 0f }

    private val _visualizerFlow = MutableStateFlow(VisualizerFrame())
    val visualizerFlow: StateFlow<VisualizerFrame> = _visualizerFlow.asStateFlow()

    private var activeSessionId: Int = 0
    private var isAudioStreaming: Boolean = false
    private var currentRmsDb: Float = -40f
    var sensitivityFactor: Float = 1.0f

    // Pemetaan Logaritmik 24-Band Presisi (FFT 512, 256 Complex Bins, Fs 48kHz)
    private val bandBinRanges = arrayOf(
        1 to 2,    // Band 0:  Sub-Bass 30Hz - 40Hz
        1 to 2,    // Band 1:  Sub-Bass 40Hz - 50Hz
        1 to 2,    // Band 2:  Bass 50Hz - 67Hz
        1 to 2,    // Band 3:  Bass 67Hz - 88Hz
        1 to 2,    // Band 4:  Bass 88Hz - 116Hz
        1 to 3,    // Band 5:  Bass Punch 116Hz - 152Hz
        2 to 3,    // Band 6:  Low Mid 152Hz - 200Hz
        2 to 4,    // Band 7:  Low Mid 200Hz - 262Hz
        3 to 5,    // Band 8:  Low Mid 262Hz - 343Hz
        3 to 6,    // Band 9:  Mid 343Hz - 450Hz
        4 to 8,    // Band 10: Mid 450Hz - 590Hz
        6 to 10,   // Band 11: Mid Vocal 590Hz - 774Hz
        8 to 13,   // Band 12: Mid Vocal 774Hz - 1015Hz
        10 to 17,  // Band 13: High Mid 1015Hz - 1330Hz
        14 to 22,  // Band 14: High Mid 1330Hz - 1750Hz
        18 to 28,  // Band 15: High Mid 1750Hz - 2290Hz
        24 to 36,  // Band 16: Presence 2290Hz - 3000Hz
        32 to 47,  // Band 17: Presence 3000Hz - 3930Hz
        41 to 61,  // Band 18: Treble 3930Hz - 5160Hz
        54 to 80,  // Band 19: Treble 5160Hz - 6760Hz
        71 to 105, // Band 20: High Treble 6760Hz - 8870Hz
        93 to 138, // Band 21: High Treble 8870Hz - 11600Hz
        123 to 180,// Band 22: Air 11600Hz - 15250Hz
        160 to 230 // Band 23: Air 15250Hz - 20000Hz
    )

    fun start(sessionId: Int = 0) {
        this.activeSessionId = sessionId
        release()
        try {
            val captureRate = Visualizer.getMaxCaptureRate()
            val capSize = 512

            visualizer = Visualizer(sessionId).apply {
                captureSize = capSize
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        vis: Visualizer?,
                        waveform: ByteArray?,
                        samplingRate: Int
                    ) {
                        if (waveform != null) {
                            checkAudioPresenceAndRms(waveform)
                        }
                    }

                    override fun onFftDataCapture(
                        vis: Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {
                        if (fft != null) {
                            processCalibratedDecibelFft(fft)
                        }
                    }
                }, captureRate / 2, true, true)
                enabled = true
            }
            startVerticalDecayLoop()
            Log.i(TAG, "RealTime Visualizer terhubung pada AudioSession ID: $sessionId")
        } catch (e: Exception) {
            Log.w(TAG, "Visualizer init warning: ${e.message}")
            startVerticalDecayLoop()
        }
    }

    private fun checkAudioPresenceAndRms(waveform: ByteArray) {
        var sumSquares = 0.0
        val sampleStep = max(1, waveform.size / 64)
        var count = 0
        for (i in 0 until waveform.size step sampleStep) {
            val unsignedVal = waveform[i].toInt() and 0xFF
            val centered = (unsignedVal - 128) / 128.0
            sumSquares += centered * centered
            count++
        }
        val rms = kotlin.math.sqrt(sumSquares / max(1, count))
        currentRmsDb = if (rms > 0.0001) (20 * log10(rms)).toFloat().coerceIn(-60f, 0f) else -60f
        isAudioStreaming = currentRmsDb > -45f
    }

    private fun processCalibratedDecibelFft(fft: ByteArray) {
        val n = fft.size / 2
        if (n <= 0) return

        var hasSignificantSignal = false
        val noiseFloorDb = -36f // Rentang dinamis VU Studio: -36 dBFS s/d 0 dBFS

        for (bandIdx in 0 until binCount) {
            val (startBin, endBin) = bandBinRanges[bandIdx]
            val actualStart = min(startBin, n - 1)
            val actualEnd = min(endBin, n)
            var maxEnergy = 0.01f

            for (k in actualStart until actualEnd) {
                val real = fft[2 * k].toFloat()
                val imag = fft[2 * k + 1].toFloat()
                val energy = hypot(real, imag)
                if (energy > maxEnergy) maxEnergy = energy
            }

            // Kompensasi frekuensi tinggi alami (tilt weighting manusia)
            val tiltFactor = 0.90f + (bandIdx / binCount.toFloat()) * 0.45f
            val scaledEnergy = (maxEnergy * tiltFactor * sensitivityFactor)

            // Hitung dalam skala Desibel (dBFS)
            val db = (20 * log10(scaledEnergy.coerceAtLeast(0.01f) / 160f)).coerceIn(noiseFloorDb, 0f)

            // Normalisasi 0.0f .. 1.0f dengan kalibrasi headroom yang pas
            val targetNormalized = ((db - noiseFloorDb) / -noiseFloorDb).coerceIn(0f, 1f)

            if (targetNormalized > 0.08f) {
                hasSignificantSignal = true
            }

            // Studio Ballistics: Fast Attack (lompatan gesit mengikuti nada)
            val smoothed = currentMagnitudes[bandIdx] * 0.25f + targetNormalized * 0.75f
            currentMagnitudes[bandIdx] = smoothed

            // Peak Hold melayang di titik tertinggi
            if (smoothed >= peakMagnitudes[bandIdx]) {
                peakMagnitudes[bandIdx] = smoothed
            }
        }

        if (hasSignificantSignal) {
            isAudioStreaming = true
        }

        _visualizerFlow.value = VisualizerFrame(
            magnitudes = currentMagnitudes.copyOf(),
            peaks = peakMagnitudes.copyOf(),
            hasAudioSignal = isAudioStreaming,
            rmsLevelDb = currentRmsDb
        )
    }

    private fun startVerticalDecayLoop() {
        decayJob?.cancel()
        decayJob = scope.launch {
            while (isActive) {
                delay(30) // ~33 FPS simulasi fisika vertikal (Gravity Fall)

                if (isAudioStreaming) {
                    // Audio Aktif: Decay lembut realistis (Weighted Release)
                    for (i in 0 until binCount) {
                        currentMagnitudes[i] = max(0f, currentMagnitudes[i] * 0.84f)
                        peakMagnitudes[i] = max(currentMagnitudes[i], peakMagnitudes[i] - 0.024f)
                    }
                } else {
                    // Audio Hening: Seluruh balok turun ke nol secara perlahan, tanpa gelombang!
                    for (i in 0 until binCount) {
                        currentMagnitudes[i] = max(0f, currentMagnitudes[i] * 0.72f)
                        peakMagnitudes[i] = max(0f, peakMagnitudes[i] - 0.040f)
                    }
                }

                _visualizerFlow.value = VisualizerFrame(
                    magnitudes = currentMagnitudes.copyOf(),
                    peaks = peakMagnitudes.copyOf(),
                    hasAudioSignal = isAudioStreaming,
                    rmsLevelDb = currentRmsDb
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
