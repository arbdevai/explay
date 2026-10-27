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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.explay.audio.audio.automaster.AutoMasterStatus
import com.explay.audio.data.model.AudioProfile
import com.explay.audio.data.model.EqBand
import com.explay.audio.ui.components.EqVerticalSlider
import com.explay.audio.ui.components.FrequencyCurveCanvas
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
fun EqualizerScreen(
    currentProfile: AudioProfile,
    customProfiles: List<AudioProfile>,
    isEngineEnabled: Boolean,
    isAutoMasterEnabled: Boolean,
    autoMasterStatus: AutoMasterStatus,
    onToggleAutoMaster: (Boolean) -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onBandGainChange: (Int, Float) -> Unit,
    onPreGainChange: (Float) -> Unit,
    onSelectPreset: (AudioProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val sliderRowScrollState = rememberScrollState()
    val allPresets = AudioProfile.PRESETS + customProfiles
    var vuSensitivity by remember { mutableFloatStateOf(1.0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Kurva Respon Frekuensi Visual dengan Animasi Bezier
        FrequencyCurveCanvas(
            bands = currentProfile.bands,
            isEnabled = isEngineEnabled
        )

        // 2. Fitur Unggulan: Smart Auto-Master Console Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(if (isAutoMasterEnabled) Color(0xFF0B1F19) else PixelSurfaceDefault)
                .border(
                    1.dp,
                    if (isAutoMasterEnabled) PixelMint.copy(alpha = 0.5f) else PixelBorderSubtle,
                    RoundedCornerShape(24.dp)
                )
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
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isAutoMasterEnabled) PixelMintNeon else TextGoogleSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Smart Auto-Master",
                                color = TextGooglePrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isAutoMasterEnabled) "Adaptasi Cerdas Audio Random Aktif" else "Menormalkan audio acak secara otomatis",
                                color = if (isAutoMasterEnabled) PixelMintNeon else TextGoogleSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = isAutoMasterEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onToggleAutoMaster(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PixelOledBlack,
                            checkedTrackColor = PixelMint,
                            uncheckedThumbColor = TextGoogleSecondary,
                            uncheckedTrackColor = PixelSurfaceContainer
                        )
                    )
                }

                AnimatedVisibility(
                    visible = isAutoMasterEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(PixelSurfaceContainer)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Baris 1: Kualitas Terdeteksi (Fixed 1-baris dengan overflow ellipsis agar tidak melar)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kualitas Terdeteksi:",
                                color = TextGoogleSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(0.45f)
                            )
                            Text(
                                text = autoMasterStatus.detectedQuality,
                                color = PixelMintNeon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(0.55f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }

                        // Baris 2: Auto-Loudness Leveler Target Gain
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auto-Loudness Leveler:",
                                color = TextGoogleSecondary,
                                fontSize = 11.sp
                            )
                            val sign = if (autoMasterStatus.autoGainCompensationDb > 0) "+" else ""
                            Text(
                                text = "$sign${autoMasterStatus.autoGainCompensationDb} dB Dynamic Gain",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Baris 3: Fixed Telemetry Badges (Tinggi Tetap - Anti Jitter / Zero Shift)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AutoMasterBadge(
                                label = "TREBLE EXCITER",
                                isActive = autoMasterStatus.trebleExciterActive,
                                modifier = Modifier.weight(1f)
                            )
                            AutoMasterBadge(
                                label = "BASS WARMTH",
                                isActive = autoMasterStatus.bassWarmthActive,
                                modifier = Modifier.weight(1f)
                            )
                            AutoMasterBadge(
                                label = "DE-HARSH",
                                isActive = autoMasterStatus.deHarshActive,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 3. Chip Carousel Pemilihan Preset Cepat (Pixel Style)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Preset Equalizer",
                    color = TextGoogleSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${allPresets.size} Profil",
                    color = Color(0xFF5F6368),
                    fontSize = 11.sp
                )
            }

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
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            onSelectPreset(preset)
                        },
                        label = {
                            Text(
                                text = preset.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = PixelSurfaceDefault,
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
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        }

        // 4. 16 Slider Vertikal Presisi Google Pixel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(PixelSurfaceDefault)
                .border(1.dp, PixelBorderSubtle, RoundedCornerShape(24.dp))
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = PixelMint,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "16-Band Multiband Processing",
                            color = TextGooglePrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Geser horisontal ➔",
                        color = Color(0xFF5F6368),
                        fontSize = 11.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(sliderRowScrollState),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
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

        // 5. Kalibrasi Sensitivitas LED VU Visualizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(PixelSurfaceDefault)
                .border(1.dp, PixelBorderSubtle, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sensitivitas Spectrum LED VU",
                        color = TextGooglePrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${(vuSensitivity * 10).roundToInt() / 10f}x",
                        color = PixelMintNeon,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = vuSensitivity,
                    onValueChange = {
                        vuSensitivity = it
                        onSensitivityChange(it)
                    },
                    valueRange = 0.4f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = PixelMint,
                        activeTrackColor = PixelMintNeon,
                        inactiveTrackColor = PixelSurfaceContainer
                    )
                )

                Text(
                    text = "Sesuaikan kepekaan balok LED agar memantul pas dengan volume lagu yang sedang Anda putar.",
                    color = Color(0xFF5F6368),
                    fontSize = 11.sp
                )
            }
        }

        // 6. Pre-Amp / Input Gain Controller
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(PixelSurfaceDefault)
                .border(1.dp, PixelBorderSubtle, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pre-Amp / Input Headroom",
                        color = TextGooglePrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val sign = if (currentProfile.preGainDb > 0) "+" else ""
                        val preDb = "$sign${(currentProfile.preGainDb * 10).roundToInt() / 10f} dB"
                        Text(
                            text = preDb,
                            color = PixelMintNeon,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        AnimatedVisibility(
                            visible = currentProfile.preGainDb != 0f,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            IconButton(
                                onClick = { onPreGainChange(0f) },
                                modifier = Modifier.size(24.dp).padding(start = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(PixelSurfaceContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("0", fontSize = 9.sp, color = TextGoogleSecondary)
                                }
                            }
                        }
                    }
                }

                Slider(
                    value = currentProfile.preGainDb,
                    onValueChange = onPreGainChange,
                    valueRange = -12f..6f,
                    enabled = isEngineEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = PixelMint,
                        activeTrackColor = PixelMintNeon,
                        inactiveTrackColor = PixelSurfaceContainer
                    )
                )

                Text(
                    text = "Gunakan nilai minus (-1 s/d -3 dB) saat meningkatkan frekuensi bass untuk mencegah kliping.",
                    color = Color(0xFF5F6368),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun AutoMasterBadge(
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) Color(0xFF0D2E25) else PixelSurfaceDefault)
            .border(
                1.dp,
                if (isActive) PixelMintNeon else PixelBorderSubtle,
                RoundedCornerShape(8.dp)
            )
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (isActive) PixelMintNeon else Color(0xFF5F6368))
            )
            Text(
                text = label,
                color = if (isActive) PixelMintNeon else Color(0xFF757575),
                fontSize = 9.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}
