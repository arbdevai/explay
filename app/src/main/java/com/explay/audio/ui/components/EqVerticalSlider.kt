package com.explay.audio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.explay.audio.ui.theme.CardBackground
import com.explay.audio.ui.theme.EmeraldPrimary
import com.explay.audio.ui.theme.EmeraldPrimaryGlow
import com.explay.audio.ui.theme.TextPrimary
import com.explay.audio.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun EqVerticalSlider(
    label: String,
    gainDb: Float,
    onGainChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    minDb: Float = -15f,
    maxDb: Float = 15f
) {
    val dbRange = maxDb - minDb
    var currentGain by remember(gainDb) { mutableFloatStateOf(gainDb) }

    Column(
        modifier = modifier
            .width(52.dp)
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Indikator dB di atas
        val sign = if (currentGain > 0) "+" else ""
        val dbText = "$sign${(currentGain * 10).roundToInt() / 10f}dB"
        Text(
            text = dbText,
            color = if (isEnabled) {
                if (currentGain != 0f) EmeraldPrimaryGlow else TextSecondary
            } else Color(0xFF6B7280),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        // Area Slider Vertikal (Canvas Interaktif)
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(CardBackground)
                .pointerInput(isEnabled) {
                    if (!isEnabled) return@pointerInput
                    detectTapGestures { offset ->
                        val h = size.height
                        val normalized = 1f - (offset.y / h).coerceIn(0f, 1f)
                        val newGain = minDb + (normalized * dbRange)
                        currentGain = newGain
                        onGainChange(newGain)
                    }
                }
                .pointerInput(isEnabled) {
                    if (!isEnabled) return@pointerInput
                    detectVerticalDragGestures { change, _ ->
                        change.consume()
                        val h = size.height
                        val normalized = 1f - (change.position.y / h).coerceIn(0f, 1f)
                        val newGain = minDb + (normalized * dbRange)
                        currentGain = newGain
                        onGainChange(newGain)
                    }
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height
                val midX = width / 2f
                val midY = height / 2f

                // Track Background (Slot Vertikal)
                drawRoundRect(
                    color = Color(0xFF1E2D25),
                    topLeft = Offset(midX - 3.dp.toPx(), 10.dp.toPx()),
                    size = Size(6.dp.toPx(), height - 20.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )

                // Garis Center / 0 dB Reference
                drawLine(
                    color = Color(0xFF2E463A),
                    start = Offset(midX - 8.dp.toPx(), midY),
                    end = Offset(midX + 8.dp.toPx(), midY),
                    strokeWidth = 2.dp.toPx()
                )

                // Hitung Posisi Thumb berdasarkan Gain saat ini
                val normalizedVal = ((currentGain - minDb) / dbRange).coerceIn(0f, 1f)
                val thumbY = (height - 20.dp.toPx()) * (1f - normalizedVal) + 10.dp.toPx()

                // Active Fill dari 0dB ke Posisi Thumb
                val activeFillTop = if (thumbY < midY) thumbY else midY
                val activeFillHeight = kotlin.math.abs(thumbY - midY)
                val activeColor = if (isEnabled) EmeraldPrimary else Color(0xFF4B5563)

                if (activeFillHeight > 0) {
                    drawRoundRect(
                        color = activeColor,
                        topLeft = Offset(midX - 3.dp.toPx(), activeFillTop),
                        size = Size(6.dp.toPx(), activeFillHeight),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }

                // Thumb Capsule Glow
                val thumbRadius = 10.dp.toPx()
                if (isEnabled) {
                    drawCircle(
                        color = EmeraldPrimaryGlow.copy(alpha = 0.35f),
                        radius = thumbRadius + 4.dp.toPx(),
                        center = Offset(midX, thumbY)
                    )
                }
                drawCircle(
                    color = if (isEnabled) EmeraldPrimaryGlow else Color(0xFF9CA3AF),
                    radius = thumbRadius,
                    center = Offset(midX, thumbY)
                )
                drawCircle(
                    color = CardBackground,
                    radius = thumbRadius - 3.dp.toPx(),
                    center = Offset(midX, thumbY)
                )
                drawCircle(
                    color = if (isEnabled) EmeraldPrimary else Color(0xFF6B7280),
                    radius = 3.dp.toPx(),
                    center = Offset(midX, thumbY)
                )
            }
        }

        // Label Frekuensi di Bawah (e.g. 32Hz, 1kHz)
        Text(
            text = label,
            color = if (isEnabled) TextPrimary else Color(0xFF6B7280),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
