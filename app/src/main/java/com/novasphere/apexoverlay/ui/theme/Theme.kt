package com.novasphere.apexoverlay.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ApexDarkColorScheme = darkColorScheme(
    primary = ApexAccent,
    onPrimary = ApexBackground,
    secondary = ApexAccentMuted,
    onSecondary = ApexBackground,
    background = ApexBackground,
    onBackground = ApexTextPrimary,
    surface = ApexSurface,
    onSurface = ApexTextPrimary,
    surfaceVariant = ApexSurfaceElevated,
    onSurfaceVariant = ApexTextSecondary,
    outline = ApexBorder
)

@Composable
fun ApexOverlayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ApexDarkColorScheme,
        typography = ApexTypography,
        content = content
    )
}
