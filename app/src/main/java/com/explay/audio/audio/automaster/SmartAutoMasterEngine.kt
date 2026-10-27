package com.explay.audio.audio.automaster

import android.content.Context
import android.util.Log
import com.explay.audio.audio.engine.AudioEngineManager
import com.explay.audio.audio.visualizer.VisualizerFrame
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
import kotlin.math.max

data class AutoMasterStatus(
    val isEnabled: Boolean = false,
    val detectedQuality: String = "Normal",
    val autoGainCompensationDb: Float = 0f,
    val trebleExciterActive: Boolean = false,
    val bassWarmthActive: Boolean = false,
    val deHarshActive: Boolean = false
)

class SmartAutoMasterEngine(private val context: Context) {

    private val audioEngine = AudioEngineManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Default)
    private var analysisJob: Job? = null

    private val _statusFlow = MutableStateFlow(AutoMasterStatus())
    val statusFlow: StateFlow<AutoMasterStatus> = _statusFlow.asStateFlow()

    private var isEnabled: Boolean = false
    private var smoothedAutoGain = 0f

    fun setEnabled(enabled: Boolean) {
        this.isEnabled = enabled
        if (enabled) {
            startAnalysisLoop()
        } else {
            stopAnalysisLoop()
        }
        _statusFlow.value = _statusFlow.value.copy(isEnabled = enabled)
    }

    private fun startAnalysisLoop() {
        analysisJob?.cancel()
        analysisJob = scope.launch {
            while (isActive) {
                delay(250) // Analisis adaptif setiap 250ms (halus, hemat baterai)
                if (!isEnabled) continue

                val visualizer = com.explay.audio.audio.visualizer.RealTimeAudioVisualizer.getInstance(context)
                val frame = visualizer.visualizerFlow.value

                if (frame.hasAudioSignal) {
                    analyzeAndCompensate(frame)
                } else {
                    // Audio hening / idle: kembalikan kompensasi ke normal secara bertahap
                    smoothedAutoGain *= 0.85f
                    if (abs(smoothedAutoGain) < 0.1f) {
                        smoothedAutoGain = 0f
                        audioEngine.resetAutoMasterOffsets()
                    } else {
                        audioEngine.applyAutoMasterOffsets(0f, 0f, 0f, smoothedAutoGain)
                    }
                    _statusFlow.value = AutoMasterStatus(
                        isEnabled = true,
                        detectedQuality = "Siaga / Hening",
                        autoGainCompensationDb = (smoothedAutoGain * 10).toInt() / 10f
                    )
                }
            }
        }
    }

    private fun analyzeAndCompensate(frame: VisualizerFrame) {
        val mags = frame.magnitudes
        if (mags.isEmpty()) return

        // 1. Hitung Energi Per Zona Spektral (Low, Mid, High)
        val lowEnergy = (0..6).map { mags[it] }.average().toFloat()
        val midEnergy = (7..15).map { mags[it] }.average().toFloat()
        val highEnergy = (16..23).map { mags[it] }.average().toFloat()
        val totalAvg = mags.average().toFloat()

        // 2. Deteksi Audio Terlalu Pelan atau Terlalu Keras (Auto-Leveler)
        val targetEnergy = 0.48f
        val energyDelta = targetEnergy - totalAvg

        val targetGain = (energyDelta * 6.0f).coerceIn(-4.0f, 5.0f)
        // Interpolasi lembut (Smoothing Ramp) agar perubahan volume tidak kaget
        smoothedAutoGain = smoothedAutoGain * 0.75f + targetGain * 0.25f

        // 3. Deteksi Kualitas Audio Berdasarkan Distribusi Spektral
        val isMuffled = highEnergy < 0.12f && (lowEnergy > 0.30f || midEnergy > 0.30f) // Kurang treble (MP3 128kbps)
        val isThin = lowEnergy < 0.10f && midEnergy > 0.25f                             // Cempreng / kurang bass
        val isHarsh = midEnergy > 0.65f && highEnergy > 0.55f                           // Terlalu tajam / sibilansi

        val qualityLabel = when {
            isMuffled -> "Audio Terkompresi (Mendem)"
            isThin -> "Audio Tipis (Kurang Bass)"
            isHarsh -> "Audio Kasar (Terlalu Tajam)"
            else -> "Audio Seimbang (Studio Mastered)"
        }

        val autoTreble = if (isMuffled) 2.2f else 0f
        val autoBass = if (isThin) 2.0f else 0f
        val autoDeHarsh = if (isHarsh) 2.0f else 0f

        // 4. Update Status Diagnosa Real-Time untuk UI
        _statusFlow.value = AutoMasterStatus(
            isEnabled = true,
            detectedQuality = qualityLabel,
            autoGainCompensationDb = (smoothedAutoGain * 10).toInt() / 10f,
            trebleExciterActive = isMuffled,
            bassWarmthActive = isThin,
            deHarshActive = isHarsh
        )

        // 5. Injeksi Kompensasi DSP Nyata ke DynamicsProcessing & Loudness
        try {
            audioEngine.applyAutoMasterOffsets(
                autoTreble = autoTreble,
                autoBass = autoBass,
                autoDeHarsh = autoDeHarsh,
                autoGain = smoothedAutoGain
            )
        } catch (e: Exception) {
            Log.w(TAG, "Gagal mengaplikasikan Auto-Master DSP: ${e.message}")
        }
    }

    private fun stopAnalysisLoop() {
        analysisJob?.cancel()
        analysisJob = null
        smoothedAutoGain = 0f
        try {
            audioEngine.resetAutoMasterOffsets()
        } catch (_: Exception) {}
        _statusFlow.value = AutoMasterStatus(isEnabled = false)
    }

    companion object {
        private const val TAG = "SmartAutoMasterEngine"

        @Volatile
        private var instance: SmartAutoMasterEngine? = null

        fun getInstance(context: Context): SmartAutoMasterEngine {
            return instance ?: synchronized(this) {
                instance ?: SmartAutoMasterEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}
