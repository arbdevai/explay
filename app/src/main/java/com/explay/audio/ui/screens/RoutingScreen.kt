package com.explay.audio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.explay.audio.data.model.AudioDeviceType
import com.explay.audio.data.model.AudioProfile
import com.explay.audio.ui.theme.CardBackground
import com.explay.audio.ui.theme.DeepBackground
import com.explay.audio.ui.theme.EmeraldPrimary
import com.explay.audio.ui.theme.EmeraldPrimaryGlow
import com.explay.audio.ui.theme.EmeraldTertiary
import com.explay.audio.ui.theme.StatusError
import com.explay.audio.ui.theme.SurfaceBorder
import com.explay.audio.ui.theme.TextPrimary
import com.explay.audio.ui.theme.TextSecondary

@Composable
fun RoutingScreen(
    currentDeviceType: AudioDeviceType,
    forceSpeaker: Boolean,
    forceBluetooth: Boolean,
    customProfiles: List<AudioProfile>,
    currentProfile: AudioProfile,
    onForceSpeakerChange: (Boolean) -> Unit,
    onForceBluetoothChange: (Boolean) -> Unit,
    onSaveCustomPreset: (String) -> Unit,
    onDeleteCustomPreset: (String) -> Unit,
    onSelectPreset: (AudioProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showSaveDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Output Device Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Perutean Output Perangkat (Routing)",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF16251E))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val icon = when (currentDeviceType) {
                        AudioDeviceType.BLUETOOTH -> Icons.Default.Bluetooth
                        AudioDeviceType.WIRED_HEADSET -> Icons.Default.Headphones
                        AudioDeviceType.USB_DAC -> Icons.Default.Usb
                        else -> Icons.Default.Speaker
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = EmeraldPrimaryGlow,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Output Audio Aktif:",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = currentDeviceType.displayName,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Anti-Gimik Toggles (Speaker HP & Bluetooth)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Konfigurasi Efek Bebas Gimik",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Toggle Speaker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Terapkan Efek di Speaker HP",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Efek tetap aktif tanpa perlu colok headset fisik.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = forceSpeaker,
                        onCheckedChange = onForceSpeakerChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepBackground,
                            checkedTrackColor = EmeraldPrimaryGlow,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = Color(0xFF1E2D25)
                        )
                    )
                }

                // Toggle Bluetooth
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Terapkan Efek di Bluetooth Audio (A2DP)",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Kompensasi otomatis saat terhubung ke TWS/Speaker Bluetooth.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = forceBluetooth,
                        onCheckedChange = onForceBluetoothChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepBackground,
                            checkedTrackColor = EmeraldPrimaryGlow,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = Color(0xFF1E2D25)
                        )
                    )
                }
            }
        }

        // 3. Manajemen Preset Kustom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Preset Pengguna",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Button(
                        onClick = {
                            presetNameInput = ""
                            showSaveDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = DeepBackground,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Simpan Setting",
                            color = DeepBackground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                if (customProfiles.isEmpty()) {
                    Text(
                        text = "Belum ada preset kustom yang disimpan. Atur 16-band EQ dan klik 'Simpan Setting' untuk membuat profil baru.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                } else {
                    customProfiles.forEach { profile ->
                        val isSelected = currentProfile.id == profile.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF1B3026) else Color(0xFF13201A))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.name,
                                    color = if (isSelected) EmeraldTertiary else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = if (isSelected) "Sedang Digunakan" else "Ketuk untuk Memuat",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (!isSelected) {
                                    TextButton(onClick = { onSelectPreset(profile) }) {
                                        Text(text = "Pakai", color = EmeraldPrimary, fontSize = 12.sp)
                                    }
                                }
                                IconButton(onClick = { onDeleteCustomPreset(profile.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = StatusError.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Dialog Simpan Preset
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(text = "Simpan Preset Kustom", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Masukkan nama untuk profil equalizer saat ini:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        placeholder = { Text("Nama Preset (misal: Heavy Metal / Warm Bass)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimaryGlow,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetNameInput.isNotBlank()) {
                            onSaveCustomPreset(presetNameInput.trim())
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(text = "Simpan", color = DeepBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(text = "Batal", color = TextSecondary)
                }
            },
            containerColor = CardBackground
        )
    }
}
