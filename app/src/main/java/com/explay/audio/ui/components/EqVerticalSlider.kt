package com.explay.audio.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.explay.audio.ui.theme.PixelBorderSubtle
import com.explay.audio.ui.theme.PixelMint
import com.explay.audio.ui.theme.PixelMintNeon
import com.explay.audio.ui.theme.PixelSurfaceContainer
import com.explay.audio.ui.theme.PixelSurfaceDefault
import com.explay.audio.ui.theme.TextGooglePrimary
import com.explay.audio.ui.theme.TextGoogleSecondary
import kotlin.math.abs
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
    val haptic = LocalHapticFeedback.current
    val dbRange = maxDb - minDb
    var isDragging by remember { mutableStateOf(false) }
    var lastHapticDb by remember { mutableFloatStateOf(gainDb) }

    // Spring expansion saat disentuh (Google Material Expressive Motion)
    val trackWidth by animateDpAsState(
        targetValue = if (isDragging) 42.dp else 36.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "trackWidthAnim"
    )

    val thumbHeight by animateDpAsState(
        targetValue = if (isDragging) 26.dp else 20.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "thumbHeightAnim"
    )

    Column(
        modifier = modifier
            .width(54.dp)
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Label dB di Atas Slider
        val sign = if (gainDb > 0) "+" else ""
        val dbText = "$sign${(gainDb * 10).roundToInt() / 10f}dB"
        Text(
            text = dbText,
            color = if (isEnabled) {
                if (gainDb != 0f) PixelMintNeon else TextGoogleSecondary
            } else Color(0xFF5F6368),
            fontSize = 10.sp,
            fontWeight = if (gainDb != 0f) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        // Slot Slider Sentuh Interaktif
        Box(
            modifier = Modifier
                .width(trackWidth)
                .height(184.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(PixelSurfaceDefault)
                .pointerInput(isEnabled) {
                    if (!isEnabled) return@pointerInput
                    detectTapGestures(
                        onPress = {
                            isDragging = true
                            tryAwaitRelease()
                            isDragging = false
                        },
                        onTap = { offset ->
                            val h = size.height
                            val normalized = 1f - (offset.y / h).coerceIn(0f, 1f)
                            val newGain = minDb + (normalized * dbRange)
                            onGainChange(newGain)
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        }
                    )
                }
                .pointerInput(isEnabled) {
                    if (!isEnabled) return@pointerInput
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false }
                    ) { change, _ ->
                        change.consume()
                        val h = size.height
                        val normalized = 1f - (change.position.y / h).coerceIn(0f, 1f)
                        val newGain = minDb + (normalized * dbRange)

                        if (abs(newGain - lastHapticDb) >= 1.0f) {
                            lastHapticDb = newGain
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        }

                        onGainChange(newGain)
                    }
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height
                val midX = width / 2f
                val midY = height / 2f

                // Track Slot Tengah
                drawRoundRect(
                    color = PixelSurfaceContainer,
                    topLeft = Offset(midX - 3.dp.toPx(), 12.dp.toPx()),
                    size = Size(6.dp.toPx(), height - 24.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )

                // Garis Center Nol dB
                drawLine(
                    color = PixelBorderSubtle,
                    start = Offset(midX - 10.dp.toPx(), midY),
                    end = Offset(midX + 10.dp.toPx(), midY),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Posisi Y Thumb
                val normalizedVal = ((gainDb - minDb) / dbRange).coerceIn(0f, 1f)
                val thumbY = (height - 24.dp.toPx()) * (1f - normalizedVal) + 12.dp.toPx()

                // Active Gradient Fill dari 0dB ke Thumb
                val activeFillTop = if (thumbY < midY) thumbY else midY
                val activeFillHeight = abs(thumbY - midY)
                val activeColor = if (isEnabled) PixelMint else Color(0xFF5F6368)

                if (activeFillHeight > 0) {
                    drawRoundRect(
                        color = activeColor,
                        topLeft = Offset(midX - 3.dp.toPx(), activeFillTop),
                        size = Size(6.dp.toPx(), activeFillHeight),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }

                // Google Pixel Expressive Thumb Capsule
                val tWidthPx = (trackWidth - 10.dp).toPx()
                val tHeightPx = thumbHeight.toPx()
                val thumbTopLeft = Offset(midX - tWidthPx / 2f, thumbY - tHeightPx / 2f)

                // Thumb Glow
                if (isEnabled && isDragging) {
                    drawRoundRect(
                        color = PixelMintNeon.copy(alpha = 0.25f),
                        topLeft = Offset(thumbTopLeft.x - 4.dp.toPx(), thumbTopLeft.y - 4.dp.toPx()),
                        size = Size(tWidthPx + 8.dp.toPx(), tHeightPx + 8.dp.toPx()),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                    )
                }

                // Thumb Body
                drawRoundRect(
                    color = if (isEnabled) PixelMint else Color(0xFF757575),
                    topLeft = thumbTopLeft,
                    size = Size(tWidthPx, tHeightPx),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )

                // Inner Grip Handle Line
                drawLine(
                    color = Color(0xFF000000),
                    start = Offset(midX - 5.dp.toPx(), thumbY),
                    end = Offset(midX + 5.dp.toPx(), thumbY),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Label Frekuensi di Bawah
        Text(
            text = label,
            color = if (isEnabled) TextGooglePrimary else Color(0xFF5F6368),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
