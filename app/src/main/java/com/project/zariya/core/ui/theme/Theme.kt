package com.project.zariya.core.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ZariyaLightColorScheme = lightColorScheme(
    primary = ZariyaPrimary,
    onPrimary = ZariyaTextOnPrimary,
    primaryContainer = ZariyaPrimaryContainer,
    onPrimaryContainer = ZariyaPrimary,
    secondary = ZariyaSecondary,
    onSecondary = ZariyaSurface,
    secondaryContainer = ZariyaSecondaryContainer,
    onSecondaryContainer = ZariyaSecondary,
    background = ZariyaBackground,
    onBackground = ZariyaTextPrimary,
    surface = ZariyaSurface,
    onSurface = ZariyaTextPrimary,
    surfaceVariant = ZariyaSurfaceVariant,
    onSurfaceVariant = ZariyaTextSecondary,
    error = ZariyaError,
    onError = ZariyaSurface,
    errorContainer = ZariyaErrorContainer,
    onErrorContainer = ZariyaError,
    outline = ZariyaSurfaceBright
)

@Composable
fun ZariyaTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = ZariyaBackground.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = ZariyaLightColorScheme,
        typography = ZariyaTypography,
        shapes = ZariyaShapes,
        content = content
    )
}
