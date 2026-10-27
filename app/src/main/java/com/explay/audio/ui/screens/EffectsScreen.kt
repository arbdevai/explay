package com.explay.audio.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.explay.audio.data.model.AudioProfile
import com.explay.audio.data.model.ReverbPresetMode
import com.explay.audio.ui.theme.CardBackground
import com.explay.audio.ui.theme.DeepBackground
import com.explay.audio.ui.theme.EmeraldPrimary
import com.explay.audio.ui.theme.EmeraldPrimaryGlow
import com.explay.audio.ui.theme.EmeraldTertiary
import com.explay.audio.ui.theme.SurfaceBorder
import com.explay.audio.ui.theme.TextPrimary
import com.explay.audio.ui.theme.TextSecondary
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
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Bass Boost Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bass Boost Engine",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    val percent = (currentProfile.bassBoostStrength / 10f).roundToInt()
                    Text(
                        text = "$percent%",
                        color = EmeraldTertiary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = currentProfile.bassBoostStrength.toFloat(),
                    onValueChange = { onBassBoostChange(it.roundToInt()) },
                    valueRange = 0f..1000f,
                    enabled = isEngineEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = EmeraldPrimaryGlow,
                        activeTrackColor = EmeraldPrimary,
                        inactiveTrackColor = Color(0xFF1E2D25)
                    )
                )

                Text(
                    text = "Meningkatkan bobot frekuensi sub-bass (40Hz - 100Hz) secara terukur.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // 2. 3D Virtualizer / Spatial Surround Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3D Virtualizer (Spatial Sound)",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    val percent = (currentProfile.virtualizerStrength / 10f).roundToInt()
                    Text(
                        text = "$percent%",
                        color = EmeraldTertiary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = currentProfile.virtualizerStrength.toFloat(),
                    onValueChange = { onVirtualizerChange(it.roundToInt()) },
                    valueRange = 0f..1000f,
                    enabled = isEngineEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = EmeraldPrimaryGlow,
                        activeTrackColor = EmeraldPrimary,
                        inactiveTrackColor = Color(0xFF1E2D25)
                    )
                )

                Text(
                    text = "Memperlebar panggung suara (soundstage) untuk pengalaman audio sinematik.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // 3. Limiter & Anti-Distortion Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Limiter & Multi-Band Compressor",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (currentProfile.limiterEnabled) "Anti-Kliping Aktif" else "Limiter Dimatikan",
                            color = if (currentProfile.limiterEnabled) EmeraldTertiary else TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = currentProfile.limiterEnabled,
                        onCheckedChange = { onLimiterChange(it, null) },
                        enabled = isEngineEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepBackground,
                            checkedTrackColor = EmeraldPrimaryGlow,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = Color(0xFF1E2D25)
                        )
                    )
                }

                if (currentProfile.limiterEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Threshold Ambang Batas",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            val tDb = "${(currentProfile.limiterThresholdDb * 10).roundToInt() / 10f} dB"
                            Text(
                                text = tDb,
                                color = EmeraldTertiary,
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
                                thumbColor = EmeraldPrimaryGlow,
                                activeTrackColor = EmeraldPrimary,
                                inactiveTrackColor = Color(0xFF1E2D25)
                            )
                        )
                    }
                }

                Text(
                    text = "Mencegah speaker internal HP dan headset dari suara sember/pecah saat volume maksimal.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // 4. Loudness Enhancer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Loudness Enhancer (Master Gain)",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    val lDb = "+${(currentProfile.loudnessEnhancerDb * 10).roundToInt() / 10f} dB"
                    Text(
                        text = lDb,
                        color = EmeraldTertiary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = currentProfile.loudnessEnhancerDb,
                    onValueChange = onLoudnessChange,
                    valueRange = 0f..15f,
                    enabled = isEngineEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = EmeraldPrimaryGlow,
                        activeTrackColor = EmeraldPrimary,
                        inactiveTrackColor = Color(0xFF1E2D25)
                    )
                )

                Text(
                    text = "Menaikkan volume audio secara keseluruhan melampaui level standar Android.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // 5. Echo & Reverb Engine
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Echo & Reverb Akustik",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = currentProfile.reverbPreset.displayName,
                        color = EmeraldTertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ReverbPresetMode.values().forEach { mode ->
                        val isSelected = currentProfile.reverbPreset == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { onReverbPresetChange(mode) },
                            enabled = isEngineEnabled,
                            label = { Text(text = mode.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = CardBackground,
                                labelColor = TextSecondary,
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color(0xFF0A0F0D)
                            )
                        )
                    }
                }

                if (currentProfile.reverbPreset != ReverbPresetMode.NONE) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Decay Time (Waktu Pantulan)",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${currentProfile.reverbDecayMs} ms",
                                color = EmeraldTertiary,
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
                                thumbColor = EmeraldPrimaryGlow,
                                activeTrackColor = EmeraldPrimary,
                                inactiveTrackColor = Color(0xFF1E2D25)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
