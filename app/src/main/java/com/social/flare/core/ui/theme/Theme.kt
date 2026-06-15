package com.social.flare.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = FlareOrange,
    onPrimary = FlareOnPrimary,
    background = FlareDarkBackground,
    onBackground = FlareDarkOnBackground,
    surface = FlareDarkSurface,
    onSurface = FlareDarkOnSurface,
    surfaceVariant = FlareDarkSurfaceVariant,
    onSurfaceVariant = FlareDarkOnSurfaceVariant,
    outline = FlareDarkOutline,
    error = FlareError,
    onError = FlareOnError
)

private val LightColorScheme = lightColorScheme(
    primary = FlareOrange,
    onPrimary = FlareOnPrimary,
    background = FlareLightBackground,
    onBackground = FlareLightOnBackground,
    surface = FlareLightSurface,
    onSurface = FlareLightOnSurface,
    surfaceVariant = FlareLightSurfaceVariant,
    onSurfaceVariant = FlareLightOnSurfaceVariant,
    outline = FlareLightOutline,
    error = FlareError,
    onError = FlareOnError
)

@Composable
fun FlareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
