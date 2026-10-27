package com.explay.audio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.explay.audio.audio.visualizer.VisualizerFrame
import com.explay.audio.ui.theme.PixelBorderSubtle
import com.explay.audio.ui.theme.PixelSurfaceDefault
import com.explay.audio.ui.theme.TextGoogleSecondary
import kotlin.math.roundToInt

@Composable
fun TealLedSpectrumVisualizer(
    frame: VisualizerFrame,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    autoMasterLabel: String? = null
) {
    val totalSegments = 10
    val columns = frame.magnitudes.size // 24 bands

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(118.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(PixelSurfaceDefault)
            .border(1.dp, PixelBorderSubtle, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.matchParentSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Baris Visualizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (frame.hasAudioSignal) Color(0xFF00F5D4) else Color(0xFF1E2D2B))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (frame.hasAudioSignal) "STUDIO VU" else "STANDBY",
                            color = if (frame.hasAudioSignal) Color(0xFF000000) else Color(0xFF5F6368),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = autoMasterLabel ?: "24-Band Matrix (-36 s/d 0 dBFS)",
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                val rmsText = if (frame.hasAudioSignal) "${frame.rmsLevelDb.roundToInt()} dB" else "-- dB"
                Text(
                    text = "RMS: $rmsText",
                    color = TextGoogleSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Canvas LED Matrix
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                val canvasW = size.width
                val canvasH = size.height
                val colGap = 2.5.dp.toPx()
                val totalColGap = (columns - 1) * colGap
                val colWidth = (canvasW - totalColGap) / columns

                val segGap = 2.dp.toPx()
                val totalSegGap = (totalSegments - 1) * segGap
                val segHeight = (canvasH - totalSegGap) / totalSegments
                val corner = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())

                for (col in 0 until columns) {
                    val colLeft = col * (colWidth + colGap)
                    val mag = if (isEnabled) frame.magnitudes[col].coerceIn(0f, 1f) else 0f
                    val peak = if (isEnabled) frame.peaks[col].coerceIn(0f, 1f) else 0f

                    val activeSegments = (mag * totalSegments).roundToInt().coerceIn(0, totalSegments)
                    val peakSegmentIndex = (peak * (totalSegments - 1)).roundToInt().coerceIn(0, totalSegments - 1)

                    for (seg in 0 until totalSegments) {
                        // Segment 0 adalah paling bawah (-36dB), Segment 9 adalah paling atas (0dB Peak)
                        val segY = canvasH - (seg + 1) * segHeight - seg * segGap
                        val isLit = seg < activeSegments

                        val activeColor = when {
                            seg == 9 -> Color(0xFFEF4444) // 0 dB Clip Warning (Red)
                            seg >= 7 -> Color(0xFFFBBF24) // -6 dB Headroom Warning (Amber)
                            seg >= 4 -> Color(0xFF00F5D4) // -14 dB Mid Signal (Neon Teal)
                            else -> Color(0xFF00B4D8)     // -36 dB Low Signal (Cyan Teal)
                        }

                        val inactiveColor = Color(0xFF101918) // Redup jika belum menyala

                        drawRoundRect(
                            color = if (isLit) activeColor else inactiveColor,
                            topLeft = Offset(colLeft, segY),
                            size = Size(colWidth, segHeight),
                            cornerRadius = corner
                        )
                    }

                    // Floating Peak Hold LED di Puncak
                    if (isEnabled && peakSegmentIndex > 0) {
                        val peakY = canvasH - (peakSegmentIndex + 1) * segHeight - peakSegmentIndex * segGap
                        val peakColor = when {
                            peakSegmentIndex == 9 -> Color(0xFFEF4444)
                            peakSegmentIndex >= 7 -> Color(0xFFFBBF24)
                            else -> Color(0xFFFFFFFF) // White floating dot
                        }
                        drawRoundRect(
                            color = peakColor,
                            topLeft = Offset(colLeft, peakY),
                            size = Size(colWidth, segHeight),
                            cornerRadius = corner
                        )
                    }
                }
            }

            // Frequency Labels di Bawah LED (32Hz ... 16kHz)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("32", "64", "125", "250", "500", "1k", "2k", "4k", "8k", "16k").forEach { label ->
                    Text(
                        text = label,
                        color = Color(0xFF64748B),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
