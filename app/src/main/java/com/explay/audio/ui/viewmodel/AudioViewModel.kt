package com.explay.audio.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.explay.audio.audio.engine.AudioEngineManager
import com.explay.audio.audio.service.AudioEngineService
import com.explay.audio.audio.visualizer.RealTimeAudioVisualizer
import com.explay.audio.audio.visualizer.VisualizerFrame
import com.explay.audio.data.diagnostic.HalInspector
import com.explay.audio.data.model.AudioDeviceType
import com.explay.audio.data.model.AudioProfile
import com.explay.audio.data.model.HalEffectInfo
import com.explay.audio.data.model.ReverbPresetMode
import com.explay.audio.data.repository.AudioPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AudioPreferencesRepository(application.applicationContext)
    private val audioEngine = AudioEngineManager.getInstance(application.applicationContext)
    private val realTimeVisualizer = RealTimeAudioVisualizer.getInstance(application.applicationContext)

    val visualizerFrame: StateFlow<VisualizerFrame> = realTimeVisualizer.visualizerFlow

    val isEngineEnabled: StateFlow<Boolean> = repository.isEngineEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val currentProfile: StateFlow<AudioProfile> = repository.currentProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AudioProfile.PRESETS.first()
    )

    val customProfiles: StateFlow<List<AudioProfile>> = repository.customProfiles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentDeviceType: StateFlow<AudioDeviceType> = audioEngine.currentDeviceType

    val forceSpeaker: StateFlow<Boolean> = repository.forceSpeaker.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val forceBluetooth: StateFlow<Boolean> = repository.forceBluetooth.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    private val _halEffects = MutableStateFlow<List<HalEffectInfo>>(emptyList())
    val halEffects: StateFlow<List<HalEffectInfo>> = _halEffects.asStateFlow()

    private val _deviceDiagnostics = MutableStateFlow<Map<String, String>>(emptyMap())
    val deviceDiagnostics: StateFlow<Map<String, String>> = _deviceDiagnostics.asStateFlow()

    init {
        // Mulai Foreground Service Audio Engine & Real-Time Visualizer
        AudioEngineService.start(application.applicationContext, 0)
        realTimeVisualizer.start(0)

        // Sinkronisasi engine dengan profil tersimpan
        viewModelScope.launch {
            currentProfile.collect { profile ->
                audioEngine.applyProfile(profile)
            }
        }

        viewModelScope.launch {
            isEngineEnabled.collect { enabled ->
                audioEngine.setEngineEnabled(enabled)
            }
        }

        refreshDiagnostics()
    }

    fun refreshDiagnostics() {
        _halEffects.value = HalInspector.queryAvailableEffects()
        _deviceDiagnostics.value = HalInspector.getDeviceAudioDiagnostics()
    }

    fun toggleEngine(enabled: Boolean) {
        viewModelScope.launch {
            repository.setEngineEnabled(enabled)
            audioEngine.setEngineEnabled(enabled)
        }
    }

    fun setBandGain(index: Int, gainDb: Float) {
        val active = currentProfile.value
        val updatedBands = active.bands.toMutableList()
        if (index in updatedBands.indices) {
            updatedBands[index] = gainDb.coerceIn(-15f, 15f)
            val updated = active.copy(bands = updatedBands)
            updateProfile(updated)
        }
    }

    fun setPreGain(preGainDb: Float) {
        val updated = currentProfile.value.copy(preGainDb = preGainDb.coerceIn(-12f, 6f))
        updateProfile(updated)
    }

    fun setBassBoost(strength: Int) {
        val updated = currentProfile.value.copy(bassBoostStrength = strength.coerceIn(0, 1000))
        updateProfile(updated)
    }

    fun setVirtualizer(strength: Int) {
        val updated = currentProfile.value.copy(virtualizerStrength = strength.coerceIn(0, 1000))
        updateProfile(updated)
    }

    fun setLoudnessEnhancer(gainDb: Float) {
        val updated = currentProfile.value.copy(loudnessEnhancerDb = gainDb.coerceIn(0f, 15f))
        updateProfile(updated)
    }

    fun setLimiter(enabled: Boolean, thresholdDb: Float? = null) {
        val active = currentProfile.value
        val updated = active.copy(
            limiterEnabled = enabled,
            limiterThresholdDb = thresholdDb ?: active.limiterThresholdDb
        )
        updateProfile(updated)
    }

    fun setReverbPreset(preset: ReverbPresetMode) {
        val updated = currentProfile.value.copy(reverbPreset = preset)
        updateProfile(updated)
    }

    fun setReverbDecay(decayMs: Int) {
        val updated = currentProfile.value.copy(reverbDecayMs = decayMs.coerceIn(100, 5000))
        updateProfile(updated)
    }

    fun setForceSpeaker(enabled: Boolean) {
        viewModelScope.launch {
            repository.setForceSpeaker(enabled)
            val updated = currentProfile.value.copy(forceSpeaker = enabled)
            updateProfile(updated)
        }
    }

    fun setForceBluetooth(enabled: Boolean) {
        viewModelScope.launch {
            repository.setForceBluetooth(enabled)
            val updated = currentProfile.value.copy(forceBluetooth = enabled)
            updateProfile(updated)
        }
    }

    fun selectPreset(profile: AudioProfile) {
        updateProfile(profile)
    }

    fun saveAsCustomPreset(name: String) {
        viewModelScope.launch {
            val custom = currentProfile.value.copy(
                id = "custom_${System.currentTimeMillis()}",
                name = name.trim().ifEmpty { "Preset Kustom" },
                isSystemPreset = false
            )
            repository.saveCustomProfile(custom)
            audioEngine.applyProfile(custom)
        }
    }

    fun deleteCustomPreset(profileId: String) {
        viewModelScope.launch {
            repository.deleteCustomProfile(profileId)
            if (currentProfile.value.id == profileId) {
                val fallback = AudioProfile.PRESETS.first()
                updateProfile(fallback)
            }
        }
    }

    private fun updateProfile(profile: AudioProfile) {
        viewModelScope.launch {
            repository.saveCurrentProfile(profile)
            audioEngine.applyProfile(profile)
        }
    }

    override fun onCleared() {
        super.onCleared()
        realTimeVisualizer.release()
    }
}
