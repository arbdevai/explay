package com.explay.audio.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AudioProfile(
    val id: String = "default_flat",
    val name: String = "Flat Studio",
    val isSystemPreset: Boolean = true,
    val bands: List<Float> = List(16) { 0f },
    val preGainDb: Float = 0f,
    val bassBoostStrength: Int = 0,         // 0 - 1000
    val virtualizerStrength: Int = 0,       // 0 - 1000
    val loudnessEnhancerDb: Float = 0f,     // 0 - 15 dB
    val limiterEnabled: Boolean = true,
    val limiterThresholdDb: Float = -1.5f,  // -12dB to 0dB
    val reverbPreset: ReverbPresetMode = ReverbPresetMode.NONE,
    val reverbDecayMs: Int = 1000,
    val forceSpeaker: Boolean = true,
    val forceBluetooth: Boolean = true
) {
    companion object {
        val PRESETS: List<AudioProfile> = listOf(
            AudioProfile(
                id = "preset_flat",
                name = "Flat Studio",
                isSystemPreset = true,
                bands = List(16) { 0f },
                preGainDb = 0f,
                bassBoostStrength = 0,
                virtualizerStrength = 0,
                loudnessEnhancerDb = 0f,
                limiterEnabled = true,
                limiterThresholdDb = -1.0f
            ),
            AudioProfile(
                id = "preset_deep_bass",
                name = "Deep Bass Punch",
                isSystemPreset = true,
                bands = listOf(
                    6.5f, 6.0f, 5.5f, 4.5f, 3.0f, 1.5f, 0.5f, 0f,
                    0f, 0.5f, 1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f
                ),
                preGainDb = -2.0f, // Headroom for bass boost
                bassBoostStrength = 650,
                virtualizerStrength = 200,
                loudnessEnhancerDb = 2.0f,
                limiterEnabled = true,
                limiterThresholdDb = -1.5f
            ),
            AudioProfile(
                id = "preset_vocal",
                name = "Vocal Clarity",
                isSystemPreset = true,
                bands = listOf(
                    -2.0f, -2.0f, -1.0f, 0f, 1.0f, 2.5f, 4.0f, 4.5f,
                    3.5f, 2.0f, 1.5f, 1.0f, 0.5f, 0f, 0f, 0f
                ),
                preGainDb = 0f,
                bassBoostStrength = 0,
                virtualizerStrength = 150,
                loudnessEnhancerDb = 1.0f,
                limiterEnabled = true,
                limiterThresholdDb = -1.0f
            ),
            AudioProfile(
                id = "preset_speaker_protector",
                name = "Speaker HP Optimizer",
                isSystemPreset = true,
                // Cut frequencies under 90Hz to protect tiny smartphone speakers from rattling
                bands = listOf(
                    -12.0f, -8.0f, -4.0f, 0.5f, 2.0f, 2.5f, 3.0f, 3.0f,
                    2.5f, 2.0f, 2.5f, 3.0f, 2.5f, 1.5f, 0.5f, 0f
                ),
                preGainDb = -1.0f,
                bassBoostStrength = 100,
                virtualizerStrength = 300,
                loudnessEnhancerDb = 3.0f,
                limiterEnabled = true,
                limiterThresholdDb = -2.0f,
                forceSpeaker = true
            ),
            AudioProfile(
                id = "preset_rock",
                name = "Rock & Metal",
                isSystemPreset = true,
                bands = listOf(
                    4.5f, 4.0f, 3.5f, 2.0f, -0.5f, -1.5f, 0.5f, 2.0f,
                    3.0f, 3.5f, 4.0f, 4.5f, 4.0f, 3.5f, 3.0f, 2.5f
                ),
                preGainDb = -1.5f,
                bassBoostStrength = 400,
                virtualizerStrength = 250,
                loudnessEnhancerDb = 1.5f,
                limiterEnabled = true,
                limiterThresholdDb = -1.2f
            ),
            AudioProfile(
                id = "preset_electronic",
                name = "EDM & Club",
                isSystemPreset = true,
                bands = listOf(
                    7.0f, 6.5f, 5.5f, 4.0f, 1.5f, 0f, -1.0f, -0.5f,
                    1.0f, 2.5f, 4.0f, 5.0f, 5.5f, 5.0f, 4.5f, 4.0f
                ),
                preGainDb = -2.5f,
                bassBoostStrength = 750,
                virtualizerStrength = 400,
                loudnessEnhancerDb = 2.5f,
                limiterEnabled = true,
                limiterThresholdDb = -2.0f
            ),
            AudioProfile(
                id = "preset_spatial_echo",
                name = "Spatial Ambient & Echo",
                isSystemPreset = true,
                bands = listOf(
                    2.0f, 2.0f, 1.5f, 1.0f, 0.5f, 0.5f, 1.0f, 1.5f,
                    2.0f, 2.5f, 3.0f, 3.5f, 4.0f, 3.5f, 3.0f, 2.5f
                ),
                preGainDb = -1.0f,
                bassBoostStrength = 300,
                virtualizerStrength = 700,
                loudnessEnhancerDb = 1.0f,
                limiterEnabled = true,
                limiterThresholdDb = -1.0f,
                reverbPreset = ReverbPresetMode.LARGE_HALL,
                reverbDecayMs = 2500
            )
        )
    }
}
