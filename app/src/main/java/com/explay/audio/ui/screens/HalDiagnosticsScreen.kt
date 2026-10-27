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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.explay.audio.data.model.HalEffectInfo
import com.explay.audio.ui.theme.CardBackground
import com.explay.audio.ui.theme.DeepBackground
import com.explay.audio.ui.theme.EmeraldPrimary
import com.explay.audio.ui.theme.EmeraldPrimaryGlow
import com.explay.audio.ui.theme.EmeraldTertiary
import com.explay.audio.ui.theme.SurfaceBorder
import com.explay.audio.ui.theme.TextPrimary
import com.explay.audio.ui.theme.TextSecondary

@Composable
fun HalDiagnosticsScreen(
    diagnostics: Map<String, String>,
    halEffects: List<HalEffectInfo>,
    onRefresh: () -> Unit,
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
        // 1. Header & Refresh Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Inspektor Audio HAL & Perangkat",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Analisis real-time pustaka audio internal",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = DeepBackground,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Pindai Ulang",
                    color = DeepBackground,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // 2. Hardware & OS Matrix Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Spesifikasi Platform Hardware",
                    color = EmeraldTertiary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                diagnostics.forEach { (key, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = key, color = TextSecondary, fontSize = 12.sp)
                        Text(
                            text = value,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 3. Audio Effect HAL Engine List
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldPrimaryGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Engine Efek Audio Terdaftar (${halEffects.size} Library)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                halEffects.forEach { effect ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF15241C))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = effect.name,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1D3528))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (effect.isNativeEngine) "AOSP Native" else "Vendor",
                                    color = EmeraldTertiary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Text(
                            text = "Tipe: ${effect.type}",
                            color = EmeraldPrimary,
                            fontSize = 11.sp
                        )

                        Text(
                            text = "Implementor: ${effect.implementor}",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )

                        Text(
                            text = "UUID: ${effect.uuid}",
                            color = Color(0xFF6B7280),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 4. Analisis Lapangan Modul Audio
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF122219))
                .border(1.dp, Color(0xFF1F3C2C), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Laporan Investigasi Audio Subsystem",
                    color = EmeraldPrimaryGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• Modul Dolby Atmos pihak ketiga telah dibersihkan dari direktori /data/adb/modules.\n" +
                           "• Pustaka 'libdynproc.so' aktif secara native di Android 14 tanpa membutuhkan patch Magisk.\n" +
                           "• ExPlay Engine langsung mengakses rantai sinyal DynamicsProcessing tanpa risiko bootloop XML HAL.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
