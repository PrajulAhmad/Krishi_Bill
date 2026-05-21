package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    secondary = HarvestYellow,
    onSecondary = OnSurface,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    background = AppBackground,
    onBackground = OnSurface,
    surface = AppSurface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = BorderMuted,
    error = ErrorRed,
    errorContainer = SurfaceError,
    onErrorContainer = OnErrorContainer
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    secondary = HarvestYellow,
    onSecondary = OnSurface,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    background = AppBackground,
    onBackground = OnSurface,
    surface = AppSurface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = BorderMuted,
    error = ErrorRed,
    errorContainer = SurfaceError,
    onErrorContainer = OnErrorContainer
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Enforce clean light corporate mode default as shown in mockups
  dynamicColor: Boolean = false, // Use our strict Agri-Utility Pro colors
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
