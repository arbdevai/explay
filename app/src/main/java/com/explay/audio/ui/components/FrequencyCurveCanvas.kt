package com.explay.audio.ui.components

import androidx.compose.animation.core.Animatable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.explay.audio.ui.theme.PixelBorderSubtle
import com.explay.audio.ui.theme.PixelMint
import com.explay.audio.ui.theme.PixelMintNeon
import com.explay.audio.ui.theme.PixelSurfaceContainer
import com.explay.audio.ui.theme.PixelSurfaceDefault

@Composable
fun FrequencyCurveCanvas(
    bands: List<Float>,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    val animatedBands = remember { bands.map { Animatable(it) } }

    LaunchedEffect(bands) {
        bands.forEachIndexed { index, targetVal ->
            if (index < animatedBands.size) {
                animatedBands[index].animateTo(
                    targetValue = targetVal,
                    animationSpec = tween(durationMillis = 320, easing = LinearEasing)
                )
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "curveShimmer")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerPhaseAnim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(138.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(PixelSurfaceDefault)
            .border(1.dp, PixelBorderSubtle, RoundedCornerShape(20.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val midY = height / 2f
            val maxDb = 15f
            val minDb = -15f

            // 1. Garis Grid Subtel Google Style
            val gridColor = PixelSurfaceContainer
            val activeGridColor = Color(0xFF2A2A34)

            // +10 dB
            val y10 = height - ((10f - minDb) / (maxDb - minDb) * height)
            drawLine(gridColor, Offset(0f, y10), Offset(width, y10), 1f)

            // 0 dB (Tengah)
            drawLine(activeGridColor, Offset(0f, midY), Offset(width, midY), 1.5.dp.toPx())

            // -10 dB
            val yMinus10 = height - ((-10f - minDb) / (maxDb - minDb) * height)
            drawLine(gridColor, Offset(0f, yMinus10), Offset(width, yMinus10), 1f)

            if (animatedBands.isEmpty()) return@Canvas

            // 2. Hitung Titik Kurva Spline
            val points = mutableListOf<Offset>()
            val stepX = width / (animatedBands.size - 1).coerceAtLeast(1)

            for (i in animatedBands.indices) {
                val db = animatedBands[i].value.coerceIn(minDb, maxDb)
                val normalizedY = (db - minDb) / (maxDb - minDb)
                val y = height - (normalizedY * height)
                val x = i * stepX
                points.add(Offset(x, y))
            }

            // 3. Bangun Path Bezier Halus (Spline Interpolation)
            val curvePath = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX1 = p0.x + (p1.x - p0.x) / 2f
                    val controlY1 = p0.y
                    val controlX2 = p0.x + (p1.x - p0.x) / 2f
                    val controlY2 = p1.y
                    cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                }
            }

            // 4. Fill Area Bawah Kurva dengan Mint Neon Ambient Glow
            val fillPath = Path().apply {
                addPath(curvePath)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            val primaryColor = if (isEnabled) PixelMint else Color(0xFF5F6368)
            val glowColor = if (isEnabled) PixelMintNeon else Color(0xFF757575)

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        glowColor.copy(alpha = if (isEnabled) 0.30f else 0.05f),
                        primaryColor.copy(alpha = if (isEnabled) 0.12f else 0.02f),
                        Color.Transparent
                    )
                )
            )

            // 5. Garis Spline Utama dengan Shimmering Light
            drawPath(
                path = curvePath,
                color = if (isEnabled) primaryColor else Color(0xFF5F6368),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // 6. Titik Node Frekuensi
            for (pt in points) {
                drawCircle(
                    color = PixelSurfaceDefault,
                    radius = 4.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = if (isEnabled) glowColor else Color(0xFF9AA0A6),
                    radius = 2.dp.toPx(),
                    center = pt
                )
            }
        }
    }
}
