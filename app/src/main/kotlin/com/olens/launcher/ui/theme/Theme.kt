package com.olens.launcher.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    secondary = Color(0xFF38BDF8),
    tertiary = Color(0xFFA78BFA),
    background = Color(0xCC0B0F17),
    surface = Color(0xCC131A26),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),
    secondary = Color(0xFF0284C7),
    tertiary = Color(0xFF7C3AED),
    background = Color(0xCCF8FAFC),
    surface = Color(0xCCE2E8F0),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

@Composable
fun OlensLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    OlensTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

@Composable
fun OlensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
        content = content
    )
}

