package com.langxiancheng.kiosk.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Brand colors
val OrangePrimary = androidx.compose.ui.graphics.Color(0xFFFF6B1A)
val OrangeLight = androidx.compose.ui.graphics.Color(0xFFFF9A5C)
val OrangeDark = androidx.compose.ui.graphics.Color(0xFFCC5500)
val BackgroundWarm = androidx.compose.ui.graphics.Color(0xFFFFF8F0)
val SurfaceWhite = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
val TextPrimary = androidx.compose.ui.graphics.Color(0xFF1A1A1A)
val TextSecondary = androidx.compose.ui.graphics.Color(0xFF666666)
val TextHint = androidx.compose.ui.graphics.Color(0xFF999999)
val SuccessGreen = androidx.compose.ui.graphics.Color(0xFF4CAF50)
val ErrorRed = androidx.compose.ui.graphics.Color(0xFFF44336)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = SurfaceWhite,
    primaryContainer = OrangeLight,
    onPrimaryContainer = TextPrimary,
    secondary = OrangeLight,
    onSecondary = TextPrimary,
    secondaryContainer = BackgroundWarm,
    onSecondaryContainer = TextPrimary,
    tertiary = OrangeDark,
    onTertiary = SurfaceWhite,
    background = BackgroundWarm,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundWarm,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = SurfaceWhite,
    outline = TextHint,
    outlineVariant = TextSecondary,
)

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = TextPrimary,
    primaryContainer = OrangeDark,
    onPrimaryContainer = BackgroundWarm,
    secondary = OrangeLight,
    onSecondary = TextPrimary,
    secondaryContainer = OrangeDark,
    onSecondaryContainer = BackgroundWarm,
    tertiary = OrangeLight,
    onTertiary = TextPrimary,
    background = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
    onBackground = BackgroundWarm,
    surface = androidx.compose.ui.graphics.Color(0xFF2A2A2A),
    onSurface = BackgroundWarm,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF333333),
    onSurfaceVariant = OrangeLight,
    error = ErrorRed,
    onError = SurfaceWhite,
    outline = TextHint,
    outlineVariant = TextSecondary,
)

/**
 * Theme composable for the LangXianCheng Kiosk application.
 * Uses brand orange color scheme by default (kiosk always uses light theme).
 */
@Composable
fun LangXianChengKioskTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KioskTypography,
        shapes = KioskShapes,
        content = content
    )
}
