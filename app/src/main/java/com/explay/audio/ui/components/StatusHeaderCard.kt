package com.explay.audio.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.explay.audio.data.model.AudioDeviceType
import com.explay.audio.ui.theme.PixelBorderSubtle
import com.explay.audio.ui.theme.PixelMint
import com.explay.audio.ui.theme.PixelMintNeon
import com.explay.audio.ui.theme.PixelOledBlack
import com.explay.audio.ui.theme.PixelSurfaceContainer
import com.explay.audio.ui.theme.PixelSurfaceDefault
import com.explay.audio.ui.theme.TextGooglePrimary
import com.explay.audio.ui.theme.TextGoogleSecondary

@Composable
fun StatusHeaderCard(
    isEngineEnabled: Boolean,
    onToggleEngine: (Boolean) -> Unit,
    deviceType: AudioDeviceType,
    profileName: String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val statusColor by animateColorAsState(
        targetValue = if (isEngineEnabled) PixelMintNeon else Color(0xFF5F6368),
        animationSpec = tween(300),
        label = "statusDotColor"
    )

    val pulseTransition = rememberInfiniteTransition(label = "pulseTransition")
    val dotScale by pulseTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScaleAnim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(PixelSurfaceDefault)
            .border(1.dp, PixelBorderSubtle, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Baris Atas: Judul App & Master Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .scale(if (isEngineEnabled) dotScale else 1f)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Column {
                        Text(
                            text = "ExPlay Audio Engine",
                            color = TextGooglePrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            text = if (isEngineEnabled) "16-Band Dynamics Processing Aktif" else "Equalizer Siaga",
                            color = if (isEngineEnabled) PixelMintNeon else TextGoogleSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = isEngineEnabled,
                    onCheckedChange = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onToggleEngine(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PixelOledBlack,
                        checkedTrackColor = PixelMint,
                        uncheckedThumbColor = TextGoogleSecondary,
                        uncheckedTrackColor = PixelSurfaceContainer
                    )
                )
            }

            // Real-Time Audio Visualizer Wave Canvas
            PixelAudioVisualizer(
                isActive = isEngineEnabled,
                modifier = Modifier.fillMaxWidth()
            )

            // Baris Bawah: Status Output Device & Preset Aktif
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chip Output Device
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PixelSurfaceContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val deviceIcon = when (deviceType) {
                        AudioDeviceType.BLUETOOTH -> Icons.Default.Bluetooth
                        AudioDeviceType.WIRED_HEADSET -> Icons.Default.Headphones
                        AudioDeviceType.USB_DAC -> Icons.Default.Usb
                        else -> Icons.Default.Speaker
                    }
                    Icon(
                        imageVector = deviceIcon,
                        contentDescription = null,
                        tint = PixelMint,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = deviceType.displayName,
                        color = TextGooglePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Badge Preset Aktif
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PixelSurfaceContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Preset: $profileName",
                        color = PixelMintNeon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
