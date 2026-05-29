package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    secondary = GoldAccent,
    tertiary = LightGreenBadge,
    background = CozyDarkBG,
    surface = CozyDarkSurface,
    onPrimary = Color.White,
    onSecondary = CozyDarkBG,
    onBackground = Color(0xFFE2EBE5),
    onSurface = Color(0xFFE2EBE5),
    surfaceContainer = CozyDarkCard
)

private val LightColorScheme = lightColorScheme(
    primary = DarkGreenPrimary,
    secondary = GoldAccent,
    tertiary = LightGreenBadge,
    background = CozyLightBG,
    surface = CozyLightSurface,
    onPrimary = Color.White,
    onSecondary = DarkGreenPrimary,
    onBackground = Color(0xFF132019),
    onSurface = Color(0xFF132019),
    surfaceContainer = CozyLightCard
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
