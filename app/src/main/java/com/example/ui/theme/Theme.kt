package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ComicPrimary,
    onPrimary = ComicOnPrimary,
    primaryContainer = ComicPrimaryContainer,
    onPrimaryContainer = ComicOnPrimaryContainer,
    secondary = ComicSecondary,
    onSecondary = ComicOnSecondary,
    secondaryContainer = ComicSecondaryContainer,
    onSecondaryContainer = ComicOnSecondaryContainer,
    tertiary = ComicTertiary,
    onTertiary = ComicOnTertiary,
    background = ComicBackgroundDark,
    surface = ComicSurfaceDark,
    surfaceVariant = ComicSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = ComicPrimary,
    onPrimary = ComicOnPrimary,
    primaryContainer = ComicPrimaryContainer,
    onPrimaryContainer = ComicOnPrimaryContainer,
    secondary = ComicSecondary,
    onSecondary = ComicOnSecondary,
    secondaryContainer = ComicSecondaryContainer,
    onSecondaryContainer = ComicOnSecondaryContainer,
    tertiary = ComicTertiary,
    onTertiary = ComicOnTertiary,
    background = ComicBackgroundLight,
    surface = ComicSurfaceLight,
    surfaceVariant = ComicSurfaceVariantLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
