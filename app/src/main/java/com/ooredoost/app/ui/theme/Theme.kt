package com.ooredoost.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val OoreDoostColorScheme = darkColorScheme(
    primary = OoredooRed,
    onPrimary = TextOnRed,
    primaryContainer = OoredooRedDark,
    onPrimaryContainer = TextPrimary,
    secondary = CyberBlue,
    onSecondary = Color.White,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = CyberGreen,
    onTertiary = Color.Black,
    tertiaryContainer = DarkSurfaceVariant,
    onTertiaryContainer = TextPrimary,
    error = StatusError,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    outlineVariant = Color(0xFF1E1E35),
    inverseSurface = TextPrimary,
    inverseOnSurface = DarkBackground,
    inversePrimary = OoredooRedDark,
    surfaceTint = OoredooRed
)

@Composable
fun OoreDoostTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = OoreDoostColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBackground.toArgb()
            window.navigationBarColor = DarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OoreDoostTypography,
        content = content
    )
}
