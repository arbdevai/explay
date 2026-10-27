package com.explay.audio.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.explay.audio.ui.theme.PixelBorderSubtle
import com.explay.audio.ui.theme.PixelMint
import com.explay.audio.ui.theme.PixelMintNeon
import com.explay.audio.ui.theme.PixelSurfaceDefault
import kotlin.math.sin

@Composable
fun PixelAudioVisualizer(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 28
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audioWaveTransition")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f, // 2*PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseAnim"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    val randomMultipliers = remember {
        List(barCount) { i ->
            0.4f + 0.6f * sin((i.toFloat() / barCount) * Math.PI).toFloat()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PixelSurfaceDefault)
            .border(1.dp, PixelBorderSubtle, RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val totalSpacing = (barCount + 1) * 3.dp.toPx()
            val barWidth = ((width - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())

            val gradientBrush = Brush.verticalGradient(
                colors = listOf(
                    PixelMintNeon,
                    PixelMint,
                    Color(0xFF00381C)
                )
            )

            for (i in 0 until barCount) {
                val x = 3.dp.toPx() + i * (barWidth + 3.dp.toPx())

                val dynamicHeight = if (isActive) {
                    val waveFactor = sin(phase + i * 0.45f) * 0.5f + 0.5f
                    val waveFactor2 = sin(phase * 1.5f + i * 0.25f) * 0.3f + 0.7f
                    val rawH = (waveFactor * waveFactor2 * randomMultipliers[i] * pulse)
                    (rawH * (height - 14.dp.toPx())).coerceIn(4.dp.toPx(), height - 10.dp.toPx())
                } else {
                    3.dp.toPx()
                }

                val topY = (height - dynamicHeight) / 2f

                // Draw Bar
                drawRoundRect(
                    brush = if (isActive) gradientBrush else Brush.linearGradient(listOf(Color(0xFF2E2E36), Color(0xFF1E1E24))),
                    topLeft = Offset(x, topY),
                    size = Size(barWidth, dynamicHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )

                // Glowing Cap on Top
                if (isActive && dynamicHeight > 8.dp.toPx()) {
                    drawCircle(
                        color = PixelMintNeon,
                        radius = (barWidth / 2f).coerceAtMost(2.5.dp.toPx()),
                        center = Offset(x + barWidth / 2f, topY)
                    )
                }
            }
        }
    }
}
