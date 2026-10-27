package com.explay.audio.data.diagnostic

import android.media.audiofx.AudioEffect
import android.os.Build
import com.explay.audio.data.model.HalEffectInfo

object HalInspector {

    fun queryAvailableEffects(): List<HalEffectInfo> {
        val result = mutableListOf<HalEffectInfo>()
        try {
            val descriptors = AudioEffect.queryEffects() ?: emptyArray()
            for (desc in descriptors) {
                val typeUuid = desc.type?.toString() ?: "Unknown"
                val typeName = resolveEffectTypeName(typeUuid)
                val isNative = desc.implementor?.contains("Android", ignoreCase = true) == true ||
                               desc.implementor?.contains("Google", ignoreCase = true) == true ||
                               desc.implementor?.contains("AOSP", ignoreCase = true) == true

                result.add(
                    HalEffectInfo(
                        type = typeName,
                        name = desc.name ?: "Unnamed Effect",
                        implementor = desc.implementor ?: "Generic Vendor",
                        uuid = desc.uuid?.toString() ?: "N/A",
                        isNativeEngine = isNative
                    )
                )
            }
        } catch (_: Exception) {
            // Audio effect query fallback
        }
        return result
    }

    private fun resolveEffectTypeName(typeUuid: String): String {
        return when (typeUuid.lowercase()) {
            "0b485f80-0587-11e5-86f7-0002a5d5c51b",
            "e0e6539b-1781-7261-676f-6d7573696340" -> "Dynamics Processing (Multiband / Limiter)"
            "0bed4300-ddd6-11db-8f34-0002a5d5c51b" -> "Equalizer (AOSP Bundle)"
            "0634f200-ddd5-11db-a0fc-0002a5d5c51b" -> "Bass Boost"
            "37cc2c00-dddd-11db-8577-0002a5d5c51b" -> "Virtualizer (3D Surround)"
            "47382d60-ddd8-11db-bf3a-0002a5d5c51b" -> "Environmental Reverb"
            "c2e5d5f0-94bd-4763-afae-4e42f66cb57d" -> "Preset Reverb / Echo"
            "fe344237-2dab-4346-b889-c4a30e854090" -> "Loudness Enhancer"
            "7b491460-8d4d-11e0-bd61-0002a5d5c51b" -> "Acoustic Echo Canceler (AEC)"
            "58b407a0-8d4d-11e0-85d6-0002a5d5c51b" -> "Noise Suppression (NS)"
            else -> "Audio Effect ($typeUuid)"
        }
    }

    fun getDeviceAudioDiagnostics(): Map<String, String> {
        return linkedMapOf(
            "Perangkat" to "${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})",
            "Versi Android" to "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            "Build ID" to Build.DISPLAY,
            "Hardware SoC" to Build.HARDWARE,
            "DynamicsProcessing Support" to (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P).toString(),
            "Audio HAL Mode" to "Direct PCM + AudioFlinger Session Mix"
        )
    }
}
