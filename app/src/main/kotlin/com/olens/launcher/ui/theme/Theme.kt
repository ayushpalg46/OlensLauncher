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

private val PureAmoledColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    secondary = Color(0xFF38BDF8),
    tertiary = Color(0xFFA78BFA),
    background = Color(0xFF000000),
    surface = Color(0xFF080808),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9)
)

@Composable
fun OlensLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    pureAmoled: Boolean = false,
    content: @Composable () -> Unit
) {
    OlensTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        pureAmoled = pureAmoled,
        content = content
    )
}

@Composable
fun OlensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    pureAmoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val colorScheme = if (pureAmoled && darkTheme) {
        baseScheme.copy(
            background = Color.Black,
            surface = Color(0xFF060606)
        )
    } else {
        baseScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

