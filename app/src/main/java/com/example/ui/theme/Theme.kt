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

private val DarkColorScheme =
    darkColorScheme(
        primary = DarkRosePrimary,
        onPrimary = DarkRoseOnPrimary,
        primaryContainer = DarkRosePrimaryContainer,
        onPrimaryContainer = DarkRoseOnPrimaryContainer,
        background = DarkRoseBackground,
        onBackground = DarkRoseOnBackground,
        surface = DarkRoseSurface,
        onSurface = DarkRoseOnSurface,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = RosePrimary,
        onPrimary = RoseOnPrimary,
        primaryContainer = RosePrimaryContainer,
        onPrimaryContainer = RoseOnPrimaryContainer,
        secondary = RoseSecondary,
        onSecondary = RoseOnSecondary,
        secondaryContainer = RoseSecondaryContainer,
        onSecondaryContainer = RoseOnSecondaryContainer,
        tertiary = RoseTertiary,
        onTertiary = RoseOnTertiary,
        tertiaryContainer = RoseTertiaryContainer,
        onTertiaryContainer = RoseOnTertiaryContainer,
        background = RoseBackground,
        onBackground = RoseOnBackground,
        surface = RoseSurface,
        onSurface = RoseOnSurface,
        surfaceVariant = RoseSurfaceVariant,
        onSurfaceVariant = RoseOnSurfaceVariant,
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Keep consistent soothing rose branding
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
