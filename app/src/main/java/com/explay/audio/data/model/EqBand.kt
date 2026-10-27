package com.explay.audio.data.model

import kotlinx.serialization.Serializable

@Serializable
data class EqBand(
    val index: Int = 0,
    val frequencyHz: Float = 1000f,
    val gainDb: Float = 0f,
    val label: String = "1k"
) {
    companion object {
        val DEFAULT_16_BANDS: List<EqBand> = listOf(
            EqBand(0, 32f, 0f, "32Hz"),
            EqBand(1, 45f, 0f, "45Hz"),
            EqBand(2, 63f, 0f, "63Hz"),
            EqBand(3, 90f, 0f, "90Hz"),
            EqBand(4, 125f, 0f, "125Hz"),
            EqBand(5, 250f, 0f, "250Hz"),
            EqBand(6, 500f, 0f, "500Hz"),
            EqBand(7, 1000f, 0f, "1kHz"),
            EqBand(8, 2000f, 0f, "2kHz"),
            EqBand(9, 4000f, 0f, "4kHz"),
            EqBand(10, 6000f, 0f, "6kHz"),
            EqBand(11, 8000f, 0f, "8kHz"),
            EqBand(12, 11000f, 0f, "11kHz"),
            EqBand(13, 14000f, 0f, "14kHz"),
            EqBand(14, 16000f, 0f, "16kHz"),
            EqBand(15, 20000f, 0f, "20kHz")
        )
    }
}
