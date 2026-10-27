package com.explay.audio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.explay.audio.ui.theme.PixelBorderSubtle
import com.explay.audio.ui.theme.PixelSurfaceContainer
import com.explay.audio.ui.theme.PixelSurfaceDefault
import com.explay.audio.ui.theme.TextGooglePrimary
import com.explay.audio.ui.theme.TextGoogleSecondary
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PixelRotaryKnob(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    displayFormatter: (Float) -> String = { "${it.toInt()}%" },
    stepSize: Float = (valueRange.endInclusive - valueRange.start) / 20f,
    isEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val rangeSpan = valueRange.endInclusive - valueRange.start
    var currentValue by remember(value) { mutableFloatStateOf(value) }

    val normalized = ((currentValue - valueRange.start) / rangeSpan).coerceIn(0f, 1f)

    // Sudut Knop: Mulai dari 135° hingga 405° (Total rentang 270°)
    val startAngle = 135f
    val sweepTotal = 270f
    val activeSweep = normalized * sweepTotal

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Lingkaran Dial Utama Interaktif
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(PixelSurfaceDefault)
                .border(1.dp, PixelBorderSubtle, CircleShape)
                .pointerInput(isEnabled, valueRange) {
                    if (!isEnabled) return@pointerInput
                    detectTapGestures { offset ->
                        // Ketuk atas/kanan untuk naik, bawah/kiri untuk turun
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val dy = offset.y - cy
                        val change = if (dy < 0) stepSize else -stepSize
                        val updated = (currentValue + change).coerceIn(valueRange.start, valueRange.endInclusive)
                        currentValue = updated
                        onValueChange(updated)
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    }
                }
                .pointerInput(isEnabled, valueRange) {
                    if (!isEnabled) return@pointerInput
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Drag vertikal ke atas (-dy) atau horisontal ke kanan (+dx) menaikkan nilai
                        val delta = -dragAmount.y + dragAmount.x
                        val deltaNormalized = delta / 140f // Sensitivitas geser alami
                        val newValue = (currentValue + deltaNormalized * rangeSpan).coerceIn(valueRange.start, valueRange.endInclusive)

                        if (abs(newValue - currentValue) >= (rangeSpan * 0.015f)) {
                            currentValue = newValue
                            onValueChange(newValue)
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(76.dp)) {
                val strokeW = 6.dp.toPx()
                val radius = (size.minDimension - strokeW) / 2f
                val arcSize = Size(radius * 2, radius * 2)
                val topLeft = Offset((size.width - radius * 2) / 2f, (size.height - radius * 2) / 2f)

                // Background Track Arc
                drawArc(
                    color = Color(0xFF1E2625),
                    startAngle = startAngle,
                    sweepAngle = sweepTotal,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )

                // Active Arc Neon Teal
                if (activeSweep > 0f) {
                    val activeBrush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF00B4D8),
                            Color(0xFF00F5D4),
                            Color(0xFF00E5FF),
                            Color(0xFF00B4D8)
                        ),
                        center = center
                    )
                    drawArc(
                        brush = if (isEnabled) activeBrush else Brush.linearGradient(listOf(Color(0xFF5F6368), Color(0xFF5F6368))),
                        startAngle = startAngle,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    // Indicator Dot di Ujung Arc
                    val currentRad = Math.toRadians((startAngle + activeSweep).toDouble())
                    val dotCenter = Offset(
                        center.x + radius * cos(currentRad).toFloat(),
                        center.y + radius * sin(currentRad).toFloat()
                    )
                    drawCircle(
                        color = if (isEnabled) Color(0xFFFFFFFF) else Color(0xFF9AA0A6),
                        radius = 4.dp.toPx(),
                        center = dotCenter
                    )
                }

                // Inner Dial Body
                drawCircle(
                    color = PixelSurfaceContainer,
                    radius = radius - 8.dp.toPx(),
                    center = center
                )
            }

            // Teks Angka di Tengah Dial
            Text(
                text = displayFormatter(currentValue),
                color = if (isEnabled) Color(0xFF00F5D4) else TextGoogleSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Label Nama Dial
        Text(
            text = label,
            color = if (isEnabled) TextGooglePrimary else Color(0xFF5F6368),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Tombol Quick Adjust [-] dan [+] Supaya 100% Presisi
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val updated = (currentValue - stepSize).coerceIn(valueRange.start, valueRange.endInclusive)
                    currentValue = updated
                    onValueChange(updated)
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                },
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(PixelSurfaceContainer),
                enabled = isEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Kurang",
                    tint = if (isEnabled) TextGoogleSecondary else Color(0xFF5F6368),
                    modifier = Modifier.size(14.dp)
                )
            }

            IconButton(
                onClick = {
                    val updated = (currentValue + stepSize).coerceIn(valueRange.start, valueRange.endInclusive)
                    currentValue = updated
                    onValueChange(updated)
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                },
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(PixelSurfaceContainer),
                enabled = isEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah",
                    tint = if (isEnabled) Color(0xFF00F5D4) else Color(0xFF5F6368),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
