package com.explay.audio.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class AudioDeviceType(val displayName: String) {
    AUTO("Otomatis (Sesuai Output Aktif)"),
    SPEAKER("Speaker HP"),
    BLUETOOTH("Bluetooth A2DP"),
    WIRED_HEADSET("Headset Kabel"),
    USB_DAC("USB Audio / DAC")
}

@Serializable
enum class ReverbPresetMode(val id: Short, val displayName: String) {
    NONE(0, "Nonaktif"),
    SMALL_ROOM(1, "Small Room"),
    MEDIUM_ROOM(2, "Medium Room"),
    LARGE_ROOM(3, "Large Room"),
    MEDIUM_HALL(4, "Medium Hall"),
    LARGE_HALL(5, "Large Hall"),
    PLATE(6, "Plate Reverb")
}
