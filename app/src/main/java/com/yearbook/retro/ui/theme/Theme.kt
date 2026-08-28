package com.yearbook.retro.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RetroColorScheme = lightColorScheme(
    primary = SaddleLeather,
    onPrimary = ParchmentBackground,
    primaryContainer = ParchmentCardSurface,
    onPrimaryContainer = DarkSepiaText,
    secondary = MutedSepiaText,
    onSecondary = ParchmentBackground,
    secondaryContainer = ParchmentCardSurface,
    onSecondaryContainer = DarkSepiaText,
    tertiary = DateStampAmber,
    onTertiary = ParchmentBackground,
    background = ParchmentBackground,
    onBackground = DarkSepiaText,
    surface = ParchmentCardSurface,
    onSurface = DarkSepiaText,
    surfaceVariant = ParchmentBackground,
    onSurfaceVariant = MutedSepiaText,
    outline = AntiqueBorder,
    error = WaxSealRed,
    onError = ParchmentBackground
)

@Composable
fun RetroYearbookTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = RetroColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ParchmentBackground.toArgb()
            window.navigationBarColor = ParchmentCardSurface.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = true
            insetsController.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RetroTypography,
        shapes = RetroShapes,
        content = content
    )
}
