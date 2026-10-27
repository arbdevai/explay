package com.explay.audio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.explay.audio.ui.theme.PixelMint
import com.explay.audio.ui.theme.PixelMintNeon
import com.explay.audio.ui.theme.PixelSurfaceContainer
import com.explay.audio.ui.theme.PixelSurfaceDefault
import com.explay.audio.ui.theme.TextGooglePrimary
import com.explay.audio.ui.theme.TextGoogleSecondary
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
    isEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    var lastValueStep by remember { mutableFloatStateOf(value) }

    val rangeSpan = valueRange.endInclusive - valueRange.start
    val normalized = ((value - valueRange.start) / rangeSpan).coerceIn(0f, 1f)

    // Sudut Knop: Mulai dari 135 derajat hingga 405 derajat (Total 270 derajat rentang)
    val startAngle = 135f
    val sweepTotal = 270f
    val activeSweep = normalized * sweepTotal

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(PixelSurfaceDefault)
                .pointerInput(isEnabled) {
                    if (!isEnabled) return@pointerInput
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Geser ke atas / kanan = naikkan nilai, ke bawah / kiri = turunkan nilai
                        val delta = -dragAmount.y + dragAmount.x
                        val sensitivity = 0.005f
                        val newNormalized = (normalized + delta * sensitivity).coerceIn(0f, 1f)
                        val newValue = valueRange.start + newNormalized * rangeSpan

                        if (kotlin.math.abs(newValue - lastValueStep) > (rangeSpan * 0.04f)) {
                            lastValueStep = newValue
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        }

                        onValueChange(newValue)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(80.dp)) {
                val strokeW = 6.dp.toPx()
                val radius = (size.minDimension - strokeW) / 2f
                val arcSize = Size(radius * 2, radius * 2)
                val topLeft = Offset((size.width - radius * 2) / 2f, (size.height - radius * 2) / 2f)

                // Background Track Arc
                drawArc(
                    color = Color(0xFF222228),
                    startAngle = startAngle,
                    sweepAngle = sweepTotal,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )

                // Active Mint Arc
                if (activeSweep > 0f) {
                    val activeBrush = Brush.sweepGradient(
                        colors = listOf(
                            PixelMint,
                            PixelMintNeon,
                            PixelMint
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

                    // Indicator Dot at End of Arc
                    val currentRad = Math.toRadians((startAngle + activeSweep).toDouble())
                    val dotCenter = Offset(
                        center.x + radius * cos(currentRad).toFloat(),
                        center.y + radius * sin(currentRad).toFloat()
                    )
                    drawCircle(
                        color = if (isEnabled) PixelMintNeon else Color(0xFF9AA0A6),
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

            // Text Nilai di Tengah Dial
            Text(
                text = displayFormatter(value),
                color = if (isEnabled) TextGooglePrimary else TextGoogleSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            color = if (isEnabled) TextGoogleSecondary else Color(0xFF5F6368),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
