package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = ComicOrange,
    onPrimary = Color.White,
    primaryContainer = ComicIndigo,
    onPrimaryContainer = Color.White,
    secondary = ComicCyan,
    onSecondary = Color.Black,
    tertiary = ComicYellow,
    onTertiary = Color.Black,
    background = DarkInkBg,
    onBackground = Color(0xFFF0F1F8),
    surface = DarkInkSurface,
    onSurface = Color(0xFFF0F1F8),
    surfaceVariant = DarkInkCard,
    onSurfaceVariant = Color(0xFFD2D5E6),
    outline = DarkInkBorder
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ComicOrange,
    onPrimary = Color.White,
    primaryContainer = ComicOrangeLight,
    onPrimaryContainer = Color.Black,
    secondary = ComicIndigo,
    onSecondary = Color.White,
    tertiary = ComicCyan,
    onTertiary = Color.Black,
    background = LightParchmentBg,
    onBackground = Color(0xFF141520),
    surface = LightParchmentSurface,
    onSurface = Color(0xFF141520),
    surfaceVariant = LightParchmentCard,
    onSurfaceVariant = Color(0xFF4A4E69),
    outline = Color(0xFFD0D4E4)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
