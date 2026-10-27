package com.explay.audio.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material.icons.filled.SpatialAudio
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.explay.audio.data.model.AudioProfile
import com.explay.audio.data.model.ReverbPresetMode
import com.explay.audio.ui.components.PixelRotaryKnob
import com.explay.audio.ui.theme.PixelBorderSubtle
import com.explay.audio.ui.theme.PixelMint
import com.explay.audio.ui.theme.PixelMintNeon
import com.explay.audio.ui.theme.PixelOledBlack
import com.explay.audio.ui.theme.PixelSurfaceContainer
import com.explay.audio.ui.theme.PixelSurfaceDefault
import com.explay.audio.ui.theme.TextGooglePrimary
import com.explay.audio.ui.theme.TextGoogleSecondary
import kotlin.math.roundToInt

@Composable
fun EffectsScreen(
    currentProfile: AudioProfile,
    isEngineEnabled: Boolean,
    onBassBoostChange: (Int) -> Unit,
    onVirtualizerChange: (Int) -> Unit,
    onLoudnessChange: (Float) -> Unit,
    onLimiterChange: (Boolean, Float?) -> Unit,
    onReverbPresetChange: (ReverbPresetMode) -> Unit,
    onReverbDecayChange: (Int) -> Unit,
    onSubwooferChange: (Boolean, Float?, Float?, Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Studio Knob Console (Bass Boost, 3D Virtualizer, Loudness Gain)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(PixelSurfaceDefault)
                .border(1.dp, PixelBorderSubtle, RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Studio Acoustic Dials",
                        color = TextGooglePrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Putar untuk Menyesuaikan",
                        color = Color(0xFF5F6368),
                        fontSize = 11.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Knob 1: Bass Boost
                    PixelRotaryKnob(
                        label = "Bass Boost",
                        value = currentProfile.bassBoostStrength.toFloat(),
                        onValueChange = { onBassBoostChange(it.roundToInt()) },
                        valueRange = 0f..1000f,
                        displayFormatter = { "${(it / 10f).roundToInt()}%" },
                        isEnabled = isEngineEnabled
                    )

                    // Knob 2: 3D Spatial Virtualizer
                    PixelRotaryKnob(
                        label = "3D Spatial",
                        value = currentProfile.virtualizerStrength.toFloat(),
                        onValueChange = { onVirtualizerChange(it.roundToInt()) },
                        valueRange = 0f..1000f,
                        displayFormatter = { "${(it / 10f).roundToInt()}%" },
                        isEnabled = isEngineEnabled
                    )

                    // Knob 3: Loudness Enhancer
                    PixelRotaryKnob(
                        label = "Loudness",
                        value = currentProfile.loudnessEnhancerDb,
                        onValueChange = onLoudnessChange,
                        valueRange = 0f..15f,
                        displayFormatter = { "+${(it * 10).roundToInt() / 10f}dB" },
                        isEnabled = isEngineEnabled
                    )
                }
            }
        }

        // 2. Hardware Subwoofer & Ultra-Low Bass Extension Console
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(if (currentProfile.subwooferEnabled) Color(0xFF0C1D18) else PixelSurfaceDefault)
                .border(
                    1.dp,
                    if (currentProfile.subwooferEnabled) PixelMintNeon.copy(alpha = 0.6f) else PixelBorderSubtle,
                    RoundedCornerShape(24.dp)
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SpeakerGroup,
                            contentDescription = null,
                            tint = if (currentProfile.subwooferEnabled) PixelMintNeon else TextGoogleSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Subwoofer Studio Engine",
                                color = TextGooglePrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentProfile.subwooferEnabled) "Sub-Bass Punch & Rumble Aktif" else "Ekstensi Subwoofer Nonaktif",
                                color = if (currentProfile.subwooferEnabled) PixelMintNeon else TextGoogleSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = currentProfile.subwooferEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onSubwooferChange(it, null, null, null)
                        },
                        enabled = isEngineEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PixelOledBlack,
                            checkedTrackColor = PixelMint,
                            uncheckedThumbColor = TextGoogleSecondary,
                            uncheckedTrackColor = PixelSurfaceContainer
                        )
                    )
                }

                AnimatedVisibility(
                    visible = currentProfile.subwooferEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Subwoofer Gain Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sub-Bass Gain (<80Hz)",
                                color = TextGoogleSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "+${(currentProfile.subwooferGainDb * 10).roundToInt() / 10f} dB",
                                color = PixelMintNeon,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = currentProfile.subwooferGainDb,
                            onValueChange = { onSubwooferChange(true, it, null, null) },
                            valueRange = 0f..12f,
                            enabled = isEngineEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = PixelMint,
                                activeTrackColor = PixelMintNeon,
                                inactiveTrackColor = PixelSurfaceContainer
                            )
                        )

                        // Crossover Frequency Selector Chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Frekuensi Crossover Subwoofer:",
                                color = TextGoogleSecondary,
                                fontSize = 11.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(40f to "40 Hz", 60f to "60 Hz", 80f to "80 Hz", 120f to "120 Hz").forEach { (freq, label) ->
                                    val isSelected = currentProfile.subwooferCutoffHz == freq
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                            onSubwooferChange(true, null, freq, null)
                                        },
                                        enabled = isEngineEnabled,
                                        label = { Text(text = label, fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = PixelSurfaceContainer,
                                            labelColor = TextGoogleSecondary,
                                            selectedContainerColor = PixelMint,
                                            selectedLabelColor = PixelOledBlack
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Mono-Summing Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PixelSurfaceContainer)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Phase Lock Mono Summing",
                                    color = TextGooglePrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Cegah pembatalan fasa stereo di subwoofer / audio mobil",
                                    color = Color(0xFF9E9E9E),
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = currentProfile.subwooferMonoSum,
                                onCheckedChange = { onSubwooferChange(true, null, null, it) },
                                enabled = isEngineEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PixelOledBlack,
                                    checkedTrackColor = PixelMint,
                                    uncheckedThumbColor = TextGoogleSecondary,
                                    uncheckedTrackColor = PixelSurfaceDefault
                                )
                            )
                        }
                    }
                }
            }
        }

        // 3. Limiter & Anti-Distortion Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(PixelSurfaceDefault)
                .border(1.dp, PixelBorderSubtle, RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = PixelMint,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Dynamics Limiter & MBC",
                                color = TextGooglePrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentProfile.limiterEnabled) "Anti-Kliping Aktif" else "Proteksi Dinonaktifkan",
                                color = if (currentProfile.limiterEnabled) PixelMintNeon else TextGoogleSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = currentProfile.limiterEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onLimiterChange(it, null)
                        },
                        enabled = isEngineEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PixelOledBlack,
                            checkedTrackColor = PixelMint,
                            uncheckedThumbColor = TextGoogleSecondary,
                            uncheckedTrackColor = PixelSurfaceContainer
                        )
                    )
                }

                AnimatedVisibility(
                    visible = currentProfile.limiterEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Threshold Ambang Batas",
                                color = TextGoogleSecondary,
                                fontSize = 12.sp
                            )
                            val tDb = "${(currentProfile.limiterThresholdDb * 10).roundToInt() / 10f} dB"
                            Text(
                                text = tDb,
                                color = PixelMintNeon,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = currentProfile.limiterThresholdDb,
                            onValueChange = { onLimiterChange(true, it) },
                            valueRange = -12f..0f,
                            enabled = isEngineEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = PixelMint,
                                activeTrackColor = PixelMintNeon,
                                inactiveTrackColor = PixelSurfaceContainer
                            )
                        )
                    }
                }

                Text(
                    text = "Mencegah speaker internal HP dan headset dari suara pecah saat bass di-boost maksimal.",
                    color = Color(0xFF5F6368),
                    fontSize = 11.sp
                )
            }
        }

        // 3. Echo & Reverb Engine
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(PixelSurfaceDefault)
                .border(1.dp, PixelBorderSubtle, RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SurroundSound,
                            contentDescription = null,
                            tint = PixelMint,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Echo & Reverb Akustik",
                            color = TextGooglePrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = currentProfile.reverbPreset.displayName,
                        color = PixelMintNeon,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReverbPresetMode.values().forEach { mode ->
                        val isSelected = currentProfile.reverbPreset == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                onReverbPresetChange(mode)
                            },
                            enabled = isEngineEnabled,
                            label = { Text(text = mode.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = PixelSurfaceContainer,
                                labelColor = TextGoogleSecondary,
                                selectedContainerColor = PixelMint,
                                selectedLabelColor = PixelOledBlack
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = PixelBorderSubtle,
                                selectedBorderColor = PixelMintNeon,
                                enabled = true,
                                selected = isSelected
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = currentProfile.reverbPreset != ReverbPresetMode.NONE,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Decay Time (Waktu Pantulan)",
                                color = TextGoogleSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${currentProfile.reverbDecayMs} ms",
                                color = PixelMintNeon,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = currentProfile.reverbDecayMs.toFloat(),
                            onValueChange = { onReverbDecayChange(it.roundToInt()) },
                            valueRange = 200f..4000f,
                            enabled = isEngineEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = PixelMint,
                                activeTrackColor = PixelMintNeon,
                                inactiveTrackColor = PixelSurfaceContainer
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
