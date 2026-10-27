package com.explay.audio.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.explay.audio.data.model.AudioDeviceType
import com.explay.audio.ui.theme.CardBackground
import com.explay.audio.ui.theme.DeepBackground
import com.explay.audio.ui.theme.EmeraldPrimary
import com.explay.audio.ui.theme.EmeraldPrimaryGlow
import com.explay.audio.ui.theme.EmeraldTertiary
import com.explay.audio.ui.theme.SurfaceBorder
import com.explay.audio.ui.theme.TextPrimary
import com.explay.audio.ui.theme.TextSecondary

@Composable
fun StatusHeaderCard(
    isEngineEnabled: Boolean,
    onToggleEngine: (Boolean) -> Unit,
    deviceType: AudioDeviceType,
    profileName: String,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = if (isEngineEnabled) EmeraldPrimaryGlow else Color(0xFF6B7280),
        animationSpec = tween(400),
        label = "statusColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
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
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Column {
                        Text(
                            text = "ExPlay Audio Engine",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEngineEnabled) "16-Band Dynamics Processing Aktif" else "Equalizer Nonaktif",
                            color = if (isEngineEnabled) EmeraldTertiary else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = isEngineEnabled,
                    onCheckedChange = onToggleEngine,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DeepBackground,
                        checkedTrackColor = EmeraldPrimaryGlow,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = Color(0xFF1E2D25)
                    )
                )
            }

            // Baris Bawah: Status Output Device & Preset Aktif
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chip Output Device
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF182820))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
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
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = deviceType.displayName,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Badge Preset Aktif
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF13231B))
                        .border(1.dp, Color(0xFF233B2E), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Preset: $profileName",
                        color = EmeraldTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
