package com.example.loafofdream.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueContainer,
    onPrimaryContainer = OnPrimaryDark,
    secondary = BluePrimaryDark,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = Color(0xFF1C1C1E),
    surface = SurfaceLight,
    onSurface = Color(0xFF1C1C1E),
    error = ErrorRed,
)

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryLight,
    onPrimary = OnPrimaryDark,
    primaryContainer = BlueContainerDark,
    onPrimaryContainer = BlueContainer,
    secondary = BlueContainer,
    onSecondary = OnPrimaryDark,
    background = DarkBackground,
    onBackground = Color(0xFFDCE8FF),
    surface = DarkSurface,
    onSurface = Color(0xFFDCE8FF),
    error = ExpiredRed,
)

@Composable
fun LoafOfDreamTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
