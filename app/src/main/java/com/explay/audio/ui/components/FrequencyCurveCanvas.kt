package com.explay.audio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.explay.audio.ui.theme.CardBackground
import com.explay.audio.ui.theme.EmeraldPrimary
import com.explay.audio.ui.theme.EmeraldPrimaryGlow
import com.explay.audio.ui.theme.SurfaceBorder

@Composable
fun FrequencyCurveCanvas(
    bands: List<Float>,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val midY = height / 2f
            val maxDb = 15f
            val minDb = -15f

            // 1. Gambar Garis Grid Referensi (Horizontal)
            val gridColor = Color(0xFF1E3027)
            drawLine(
                color = gridColor,
                start = Offset(0f, height * 0.2f),
                end = Offset(width, height * 0.2f),
                strokeWidth = 1f
            )
            drawLine(
                color = Color(0xFF284437),
                start = Offset(0f, midY),
                end = Offset(width, midY),
                strokeWidth = 1.5f
            )
            drawLine(
                color = gridColor,
                start = Offset(0f, height * 0.8f),
                end = Offset(width, height * 0.8f),
                strokeWidth = 1f
            )

            if (bands.isEmpty()) return@Canvas

            // 2. Hitung Titik Kurva
            val points = mutableListOf<Offset>()
            val stepX = width / (bands.size - 1).coerceAtLeast(1)

            for (i in bands.indices) {
                val db = bands[i].coerceIn(minDb, maxDb)
                val normalizedY = (db - minDb) / (maxDb - minDb) // 0..1 (0 = -15dB, 1 = +15dB)
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

            // 4. Fill Area di bawah kurva dengan Emerald Gradient
            val fillPath = Path().apply {
                addPath(curvePath)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            val primaryColor = if (isEnabled) EmeraldPrimary else Color(0xFF4B5563)
            val glowColor = if (isEnabled) EmeraldPrimaryGlow else Color(0xFF6B7280)

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        glowColor.copy(alpha = if (isEnabled) 0.35f else 0.08f),
                        primaryColor.copy(alpha = if (isEnabled) 0.15f else 0.03f),
                        Color.Transparent
                    )
                )
            )

            // 5. Gambar Garis Kurva Utama
            drawPath(
                path = curvePath,
                color = if (isEnabled) primaryColor else Color(0xFF6B7280),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // 6. Gambar Node Bulat di setiap titik frekuensi
            for (pt in points) {
                drawCircle(
                    color = CardBackground,
                    radius = 4.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = if (isEnabled) glowColor else Color(0xFF9CA3AF),
                    radius = 2.5.dp.toPx(),
                    center = pt
                )
            }
        }
    }
}
