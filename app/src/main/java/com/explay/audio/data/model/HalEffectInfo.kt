package com.explay.audio.data.model

data class HalEffectInfo(
    val type: String,
    val name: String,
    val implementor: String,
    val uuid: String,
    val isNativeEngine: Boolean = true
)
