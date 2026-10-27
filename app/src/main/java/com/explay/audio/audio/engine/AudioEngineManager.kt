package com.explay.audio.audio.engine

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.os.Build
import android.util.Log
import com.explay.audio.data.model.AudioDeviceType
import com.explay.audio.data.model.AudioProfile
import com.explay.audio.data.model.EqBand
import com.explay.audio.data.model.ReverbPresetMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioEngineManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var dynamicsProcessing: DynamicsProcessing? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private var activeSessionId: Int = 0
    private var currentProfile: AudioProfile = AudioProfile.PRESETS.first()
    private var isEngineEnabled: Boolean = true

    private val _currentDeviceType = MutableStateFlow(AudioDeviceType.SPEAKER)
    val currentDeviceType: StateFlow<AudioDeviceType> = _currentDeviceType.asStateFlow()

    private val _isProcessingActive = MutableStateFlow(false)
    val isProcessingActive: StateFlow<Boolean> = _isProcessingActive.asStateFlow()

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            updateConnectedAudioDevice()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            updateConnectedAudioDevice()
        }
    }

    init {
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
        updateConnectedAudioDevice()
    }

    fun startEngine(sessionId: Int = 0) {
        this.activeSessionId = sessionId
        initializeEffects(sessionId)
        applyProfile(currentProfile)
        _isProcessingActive.value = true
        Log.i(TAG, "Audio Engine dimulai pada AudioSession ID: $sessionId")
    }

    fun stopEngine() {
        releaseEffects()
        _isProcessingActive.value = false
        Log.i(TAG, "Audio Engine dinonaktifkan")
    }

    fun setEngineEnabled(enabled: Boolean) {
        this.isEngineEnabled = enabled
        if (enabled) {
            if (!_isProcessingActive.value) {
                startEngine(activeSessionId)
            } else {
                setAllEffectsEnabled(true)
            }
        } else {
            setAllEffectsEnabled(false)
        }
    }

    fun onSessionOpened(sessionId: Int) {
        Log.i(TAG, "Audio Session baru dibuka: $sessionId")
        this.activeSessionId = sessionId
        if (isEngineEnabled) {
            initializeEffects(sessionId)
            applyProfile(currentProfile)
        }
    }

    fun onSessionClosed(sessionId: Int) {
        Log.i(TAG, "Audio Session ditutup: $sessionId")
        if (activeSessionId == sessionId) {
            // Fallback kembali ke Global Session (0)
            activeSessionId = 0
            if (isEngineEnabled) {
                initializeEffects(0)
                applyProfile(currentProfile)
            }
        }
    }

    @Synchronized
    private fun initializeEffects(sessionId: Int) {
        releaseEffects()

        try {
            // 1. Dynamics Processing (16-Band Multiband EQ + Limiter)
            val dpConfig = buildDynamicsProcessingConfig()
            dynamicsProcessing = DynamicsProcessing(1000, sessionId, dpConfig).apply {
                enabled = isEngineEnabled
            }
        } catch (e: Exception) {
            Log.w(TAG, "DynamicsProcessing tidak didukung pada session $sessionId: ${e.message}")
        }

        try {
            // 2. Bass Boost
            bassBoost = BassBoost(1000, sessionId).apply {
                enabled = isEngineEnabled
            }
        } catch (e: Exception) {
            Log.w(TAG, "BassBoost init error: ${e.message}")
        }

        try {
            // 3. Virtualizer (3D Surround)
            virtualizer = Virtualizer(1000, sessionId).apply {
                enabled = isEngineEnabled
            }
        } catch (e: Exception) {
            Log.w(TAG, "Virtualizer init error: ${e.message}")
        }

        try {
            // 4. Preset Reverb / Echo
            presetReverb = PresetReverb(1000, sessionId).apply {
                enabled = isEngineEnabled
            }
        } catch (e: Exception) {
            Log.w(TAG, "PresetReverb init error: ${e.message}")
        }

        try {
            // 5. Loudness Enhancer (Preamp Gain)
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                enabled = isEngineEnabled
            }
        } catch (e: Exception) {
            Log.w(TAG, "LoudnessEnhancer init error: ${e.message}")
        }
    }

    private fun buildDynamicsProcessingConfig(): DynamicsProcessing.Config {
        val channelCount = 2 // Stereo
        val frequencies = EqBand.DEFAULT_16_BANDS.map { it.frequencyHz }.toFloatArray()
        val bandCount = frequencies.size

        val builder = DynamicsProcessing.Config.Builder(
            DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
            channelCount,
            true, // preEqInUse
            bandCount,
            true, // mbcInUse
            bandCount,
            false, // postEqInUse
            0,
            true  // limiterInUse
        )

        // Konfigurasi Pre-EQ per-channel
        for (ch in 0 until channelCount) {
            val preEq = DynamicsProcessing.Eq(true, true, bandCount)
            for (i in 0 until bandCount) {
                val band = DynamicsProcessing.EqBand(true, frequencies[i], 0f)
                preEq.setBand(i, band)
            }
            builder.setPreEqByChannelIndex(ch, preEq)

            // Limiter Anti-Kliping
            val limiter = DynamicsProcessing.Limiter(
                true,   // inUse
                true,   // enabled
                0,      // linkGroup
                1.0f,   // attackTime ms
                50.0f,  // releaseTime ms
                10.0f,  // ratio
                -1.5f,  // threshold dB
                0.0f    // postGain dB
            )
            builder.setLimiterByChannelIndex(ch, limiter)
        }

        return builder.build()
    }

    @Synchronized
    fun applyProfile(profile: AudioProfile) {
        this.currentProfile = profile
        val bands = profile.bands

        // Periksa batasan output perangkat jika forceSpeaker / forceBluetooth tidak aktif
        val isSpeaker = _currentDeviceType.value == AudioDeviceType.SPEAKER
        val isBluetooth = _currentDeviceType.value == AudioDeviceType.BLUETOOTH
        if (isSpeaker && !profile.forceSpeaker) {
            setAllEffectsEnabled(false)
            return
        }
        if (isBluetooth && !profile.forceBluetooth) {
            setAllEffectsEnabled(false)
            return
        }

        setAllEffectsEnabled(isEngineEnabled)

        // 1. Terapkan Band Gains pada DynamicsProcessing Pre-EQ
        dynamicsProcessing?.let { dp ->
            try {
                for (ch in 0 until 2) {
                    for (i in 0 until minOf(bands.size, 16)) {
                        dp.setPreEqBandByChannelIndex(ch, i, DynamicsProcessing.EqBand(true, EqBand.DEFAULT_16_BANDS[i].frequencyHz, bands[i]))
                    }
                    // Limiter Threshold
                    val limiterThreshold = if (profile.limiterEnabled) profile.limiterThresholdDb else 0.0f
                    val limiter = DynamicsProcessing.Limiter(
                        profile.limiterEnabled,
                        profile.limiterEnabled,
                        0,
                        1.0f,
                        50.0f,
                        if (profile.limiterEnabled) 10.0f else 1.0f,
                        limiterThreshold,
                        profile.preGainDb
                    )
                    dp.setLimiterByChannelIndex(ch, limiter)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gagal mengaplikasikan gain DynamicsProcessing: ${e.message}")
            }
        }

        // 2. Bass Boost
        bassBoost?.let { bb ->
            try {
                if (bb.strengthSupported) {
                    bb.setStrength(profile.bassBoostStrength.coerceIn(0, 1000).toShort())
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gagal set BassBoost: ${e.message}")
            }
        }

        // 3. Virtualizer (3D Spatial)
        virtualizer?.let { virt ->
            try {
                if (virt.strengthSupported) {
                    virt.setStrength(profile.virtualizerStrength.coerceIn(0, 1000).toShort())
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gagal set Virtualizer: ${e.message}")
            }
        }

        // 4. Preset Reverb / Echo
        presetReverb?.let { rev ->
            try {
                if (profile.reverbPreset != ReverbPresetMode.NONE) {
                    rev.preset = profile.reverbPreset.id
                    rev.enabled = isEngineEnabled
                } else {
                    rev.enabled = false
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gagal set PresetReverb: ${e.message}")
            }
        }

        // 5. Loudness Enhancer
        loudnessEnhancer?.let { le ->
            try {
                val mB = (profile.loudnessEnhancerDb * 100).toInt().coerceIn(0, 2000)
                le.setTargetGain(mB)
            } catch (e: Exception) {
                Log.w(TAG, "Gagal set LoudnessEnhancer: ${e.message}")
            }
        }
    }

    private fun setAllEffectsEnabled(enabled: Boolean) {
        try { dynamicsProcessing?.enabled = enabled } catch (_: Exception) {}
        try { bassBoost?.enabled = enabled } catch (_: Exception) {}
        try { virtualizer?.enabled = enabled } catch (_: Exception) {}
        try {
            presetReverb?.enabled = enabled && currentProfile.reverbPreset != ReverbPresetMode.NONE
        } catch (_: Exception) {}
        try { loudnessEnhancer?.enabled = enabled } catch (_: Exception) {}
    }

    private fun updateConnectedAudioDevice() {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        var detectedType = AudioDeviceType.SPEAKER

        for (device in devices) {
            when (device.type) {
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                AudioDeviceInfo.TYPE_BLE_HEADSET,
                AudioDeviceInfo.TYPE_BLE_SPEAKER -> {
                    detectedType = AudioDeviceType.BLUETOOTH
                    break
                }
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> {
                    detectedType = AudioDeviceType.WIRED_HEADSET
                    break
                }
                AudioDeviceInfo.TYPE_USB_DEVICE,
                AudioDeviceInfo.TYPE_USB_HEADSET -> {
                    detectedType = AudioDeviceType.USB_DAC
                    break
                }
                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> {
                    detectedType = AudioDeviceType.SPEAKER
                }
            }
        }
        _currentDeviceType.value = detectedType
        Log.i(TAG, "Perangkat output audio aktif: ${detectedType.displayName}")
    }

    @Synchronized
    fun releaseEffects() {
        try { dynamicsProcessing?.release() } catch (_: Exception) {}
        try { bassBoost?.release() } catch (_: Exception) {}
        try { virtualizer?.release() } catch (_: Exception) {}
        try { presetReverb?.release() } catch (_: Exception) {}
        try { loudnessEnhancer?.release() } catch (_: Exception) {}

        dynamicsProcessing = null
        bassBoost = null
        virtualizer = null
        presetReverb = null
        loudnessEnhancer = null
    }

    fun destroy() {
        try {
            audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        } catch (_: Exception) {}
        releaseEffects()
    }

    companion object {
        private const val TAG = "AudioEngineManager"

        @Volatile
        private var instance: AudioEngineManager? = null

        fun getInstance(context: Context): AudioEngineManager {
            return instance ?: synchronized(this) {
                instance ?: AudioEngineManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
