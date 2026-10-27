package com.explay.audio.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PixelDarkColorScheme = darkColorScheme(
    primary = PixelMint,
    onPrimary = PixelOledBlack,
    primaryContainer = PixelMintContainer,
    onPrimaryContainer = PixelMintOnContainer,
    secondary = PixelMintNeon,
    onSecondary = PixelOledBlack,
    secondaryContainer = PixelSurfaceContainer,
    onSecondaryContainer = TextGooglePrimary,
    tertiary = PixelCyanAccent,
    background = PixelOledBlack,
    onBackground = TextGooglePrimary,
    surface = PixelSurfaceDefault,
    onSurface = TextGooglePrimary,
    surfaceVariant = PixelSurfaceContainer,
    onSurfaceVariant = TextGoogleSecondary,
    outline = PixelBorderSubtle,
    outlineVariant = PixelBorderActive,
    error = StatusDanger
)

@Composable
fun ExPlayAudioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = PixelDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PixelOledBlack.toArgb()
            window.navigationBarColor = PixelOledBlack.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
