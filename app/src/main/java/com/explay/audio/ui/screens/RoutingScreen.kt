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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.explay.audio.data.model.AudioDeviceType
import com.explay.audio.data.model.AudioProfile
import com.explay.audio.ui.theme.PixelBorderSubtle
import com.explay.audio.ui.theme.PixelMint
import com.explay.audio.ui.theme.PixelMintNeon
import com.explay.audio.ui.theme.PixelOledBlack
import com.explay.audio.ui.theme.PixelSurfaceContainer
import com.explay.audio.ui.theme.PixelSurfaceDefault
import com.explay.audio.ui.theme.StatusDanger
import com.explay.audio.ui.theme.TextGooglePrimary
import com.explay.audio.ui.theme.TextGoogleSecondary

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
    val haptic = LocalHapticFeedback.current
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
                .clip(RoundedCornerShape(24.dp))
                .background(PixelSurfaceDefault)
                .border(1.dp, PixelBorderSubtle, RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Perutean Output Perangkat",
                    color = TextGooglePrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PixelSurfaceContainer)
                        .padding(14.dp),
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
                        tint = PixelMintNeon,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Output Audio Aktif:",
                            color = TextGoogleSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = currentDeviceType.displayName,
                            color = TextGooglePrimary,
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
                .clip(RoundedCornerShape(24.dp))
                .background(PixelSurfaceDefault)
                .border(1.dp, PixelBorderSubtle, RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Konfigurasi Audio Bebas Hambatan",
                    color = TextGooglePrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // Toggle Speaker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Paksa Efek di Speaker HP",
                            color = TextGooglePrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Efek audio aktif tanpa perlu colok headset fisik.",
                            color = TextGoogleSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = forceSpeaker,
                        onCheckedChange = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onForceSpeakerChange(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PixelOledBlack,
                            checkedTrackColor = PixelMint,
                            uncheckedThumbColor = TextGoogleSecondary,
                            uncheckedTrackColor = PixelSurfaceContainer
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
                            text = "Paksa Efek di Bluetooth Audio (A2DP)",
                            color = TextGooglePrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Kompensasi sinyal otomatis ke TWS dan speaker Bluetooth.",
                            color = TextGoogleSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = forceBluetooth,
                        onCheckedChange = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onForceBluetoothChange(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PixelOledBlack,
                            checkedTrackColor = PixelMint,
                            uncheckedThumbColor = TextGoogleSecondary,
                            uncheckedTrackColor = PixelSurfaceContainer
                        )
                    )
                }
            }
        }

        // 3. Manajemen Preset Kustom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(PixelSurfaceDefault)
                .border(1.dp, PixelBorderSubtle, RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Preset Pengguna",
                        color = TextGooglePrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            presetNameInput = ""
                            showSaveDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PixelMint),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = PixelOledBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Simpan Profil",
                            color = PixelOledBlack,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                if (customProfiles.isEmpty()) {
                    Text(
                        text = "Belum ada preset kustom yang disimpan. Sesuaikan equalizer di tab pertama lalu klik 'Simpan Profil'.",
                        color = Color(0xFF5F6368),
                        fontSize = 12.sp
                    )
                } else {
                    customProfiles.forEach { profile ->
                        val isSelected = currentProfile.id == profile.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(0xFF132D1F) else PixelSurfaceContainer)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.name,
                                    color = if (isSelected) PixelMintNeon else TextGooglePrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = if (isSelected) "Sedang Aktif" else "Ketuk untuk Memuat",
                                    color = TextGoogleSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (!isSelected) {
                                    TextButton(onClick = { onSelectPreset(profile) }) {
                                        Text(text = "Pakai", color = PixelMint, fontSize = 12.sp)
                                    }
                                }
                                IconButton(onClick = { onDeleteCustomPreset(profile.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = StatusDanger.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Dialog Simpan Preset Google Pixel Style
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(text = "Simpan Profil Kustom", color = TextGooglePrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Masukkan nama profil untuk konfigurasi audio saat ini:",
                        color = TextGoogleSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        placeholder = { Text("Contoh: Rock Hard / Audiophile Master") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PixelMint,
                            unfocusedBorderColor = PixelBorderSubtle,
                            focusedTextColor = TextGooglePrimary,
                            unfocusedTextColor = TextGooglePrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = PixelMint),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Simpan", color = PixelOledBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(text = "Batal", color = TextGoogleSecondary)
                }
            },
            containerColor = PixelSurfaceDefault,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
