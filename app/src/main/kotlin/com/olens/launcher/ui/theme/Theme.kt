package com.olens.launcher.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

// Niagara-style signature accent palettes
val ACCENT_PALETTES = mapOf(
    "Niagara Cyan" to Pair(Color(0xFF38BDF8), Color(0xFF0284C7)),
    "Emerald Green" to Pair(Color(0xFF10B981), Color(0xFF059669)),
    "Sunset Orange" to Pair(Color(0xFFF97316), Color(0xFFEA580C)),
    "Amethyst Purple" to Pair(Color(0xFFA855F7), Color(0xFF9333EA)),
    "Crimson Red" to Pair(Color(0xFFEF4444), Color(0xFFDC2626)),
    "Sunflower Gold" to Pair(Color(0xFFEAB308), Color(0xFFCA8A04)),
    "Pure White" to Pair(Color(0xFFF8FAFC), Color(0xFF94A3B8))
)

val FONT_OPTIONS = listOf(
    "Default",
    "Sans-Serif",
    "Serif",
    "Monospace",
    "Cursive"
)

val ICON_SHAPES = listOf(
    "Circle",
    "Rounded Square",
    "Squircle",
    "Teardrop"
)

fun resolveFontFamily(name: String): FontFamily {
    return when (name.lowercase()) {
        "sans-serif", "sansserif" -> FontFamily.SansSerif
        "serif" -> FontFamily.Serif
        "monospace", "mono" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        else -> FontFamily.Default
    }
}

fun createCustomTypography(fontFamily: FontFamily): Typography {
    val default = Typography()
    return Typography(
        displayLarge = default.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = default.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = default.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = default.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = default.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = default.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = default.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = default.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = default.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = default.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = default.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = default.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = default.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = default.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = default.labelSmall.copy(fontFamily = fontFamily)
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    secondary = Color(0xFF0284C7),
    tertiary = Color(0xFFA78BFA),
    background = Color(0xCC0B0F17),
    surface = Color(0xCC131A26),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    secondary = Color(0xFF0369A1),
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
    dynamicColor: Boolean = false,
    pureAmoled: Boolean = false,
    accentColorName: String = "Niagara Cyan",
    fontFamilyName: String = "Default",
    content: @Composable () -> Unit
) {
    OlensTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        pureAmoled = pureAmoled,
        accentColorName = accentColorName,
        fontFamilyName = fontFamilyName,
        content = content
    )
}

@Composable
fun OlensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    pureAmoled: Boolean = false,
    accentColorName: String = "Niagara Cyan",
    fontFamilyName: String = "Default",
    content: @Composable () -> Unit
) {
    val selectedAccent = ACCENT_PALETTES[accentColorName] ?: Pair(Color(0xFF38BDF8), Color(0xFF0284C7))

    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme.copy(
            primary = selectedAccent.first,
            secondary = selectedAccent.second
        )
        else -> LightColorScheme.copy(
            primary = selectedAccent.first,
            secondary = selectedAccent.second
        )
    }

    val colorScheme = if (pureAmoled && darkTheme) {
        baseScheme.copy(
            background = Color.Black,
            surface = Color(0xFF060606)
        )
    } else {
        baseScheme
    }

    val selectedFont = resolveFontFamily(fontFamilyName)
    val typography = createCustomTypography(selectedFont)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
