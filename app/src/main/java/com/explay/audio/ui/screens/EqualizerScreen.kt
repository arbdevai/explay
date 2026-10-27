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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.explay.audio.data.model.EqBand
import com.explay.audio.ui.components.EqVerticalSlider
import com.explay.audio.ui.components.FrequencyCurveCanvas
import com.explay.audio.ui.theme.CardBackground
import com.explay.audio.ui.theme.EmeraldPrimary
import com.explay.audio.ui.theme.EmeraldPrimaryGlow
import com.explay.audio.ui.theme.EmeraldTertiary
import com.explay.audio.ui.theme.SurfaceBorder
import com.explay.audio.ui.theme.TextPrimary
import com.explay.audio.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun EqualizerScreen(
    currentProfile: AudioProfile,
    customProfiles: List<AudioProfile>,
    isEngineEnabled: Boolean,
    onBandGainChange: (Int, Float) -> Unit,
    onPreGainChange: (Float) -> Unit,
    onSelectPreset: (AudioProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val sliderRowScrollState = rememberScrollState()
    val allPresets = AudioProfile.PRESETS + customProfiles

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Kurva Respon Frekuensi Visual
        FrequencyCurveCanvas(
            bands = currentProfile.bands,
            isEnabled = isEngineEnabled
        )

        // 2. Chip Carousel Pemilihan Preset Cepat
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Preset Equalizer",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allPresets.forEach { preset ->
                    val isSelected = currentProfile.id == preset.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectPreset(preset) },
                        label = {
                            Text(
                                text = preset.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CardBackground,
                            labelColor = TextSecondary,
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color(0xFF0A0F0D)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = SurfaceBorder,
                            selectedBorderColor = EmeraldPrimaryGlow,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }
        }

        // 3. 16 Slider Vertikal Presisi
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                .padding(vertical = 14.dp, horizontal = 8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "16-Band Precision Equalizer (±15dB)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Geser ke samping ➔",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(sliderRowScrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EqBand.DEFAULT_16_BANDS.forEachIndexed { index, bandInfo ->
                        val currentGain = currentProfile.bands.getOrElse(index) { 0f }
                        EqVerticalSlider(
                            label = bandInfo.label,
                            gainDb = currentGain,
                            onGainChange = { newDb -> onBandGainChange(index, newDb) },
                            isEnabled = isEngineEnabled
                        )
                    }
                }
            }
        }

        // 4. Input Gain / Pre-Amp Controller
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
                        text = "Pre-Amp / Input Gain",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    val sign = if (currentProfile.preGainDb > 0) "+" else ""
                    val preDb = "$sign${(currentProfile.preGainDb * 10).roundToInt() / 10f} dB"
                    Text(
                        text = preDb,
                        color = EmeraldTertiary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = currentProfile.preGainDb,
                    onValueChange = onPreGainChange,
                    valueRange = -12f..6f,
                    enabled = isEngineEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = EmeraldPrimaryGlow,
                        activeTrackColor = EmeraldPrimary,
                        inactiveTrackColor = Color(0xFF1E2D25)
                    )
                )

                Text(
                    text = "Gunakan nilai minus (-1 s/d -3 dB) saat meningkatkan bass agar terhindar dari digital clipping.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
