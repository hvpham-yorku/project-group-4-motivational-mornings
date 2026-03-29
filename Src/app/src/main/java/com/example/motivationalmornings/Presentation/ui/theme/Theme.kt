package com.example.motivationalmornings.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DawnPrimary,
    onPrimary = DawnOnPrimary,
    primaryContainer = DawnPrimaryContainer,
    onPrimaryContainer = DawnOnPrimaryContainer,
    secondary = DawnSecondary,
    onSecondary = DawnOnSecondary,
    secondaryContainer = DawnSecondaryContainer,
    onSecondaryContainer = DawnOnSecondaryContainer,
    tertiary = DawnTertiary,
    onTertiary = DawnOnTertiary,
    tertiaryContainer = DawnTertiaryContainer,
    onTertiaryContainer = DawnOnTertiaryContainer,
    background = DawnBackground,
    onBackground = DawnOnBackground,
    surface = DawnSurface,
    onSurface = DawnOnSurface,
)

private val LightColorScheme = lightColorScheme(
    primary = MorningPrimary,
    onPrimary = MorningOnPrimary,
    primaryContainer = MorningPrimaryContainer,
    onPrimaryContainer = MorningOnPrimaryContainer,
    secondary = MorningSecondary,
    onSecondary = MorningOnSecondary,
    secondaryContainer = MorningSecondaryContainer,
    onSecondaryContainer = MorningOnSecondaryContainer,
    tertiary = MorningTertiary,
    onTertiary = MorningOnTertiary,
    tertiaryContainer = MorningTertiaryContainer,
    onTertiaryContainer = MorningOnTertiaryContainer,
    background = MorningSky,
    onBackground = MorningOnPrimaryContainer,
    surface = MorningSurface,
    onSurface = MorningOnPrimaryContainer,
    outline = MorningOutline
)

@Composable
fun MotivationalMorningsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}