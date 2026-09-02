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
    primary = ChakhLeRedPrimary,
    onPrimary = Color.White,
    primaryContainer = ChakhLeRedDark,
    onPrimaryContainer = Color.White,
    secondary = ChakhLeAmber,
    onSecondary = Color.Black,
    secondaryContainer = ChakhLeAmberDark,
    onSecondaryContainer = Color.White,
    tertiary = ChakhLeGold,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color(0xFFEDE7E3),
    onSurface = Color(0xFFEDE7E3),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ChakhLeRedPrimary,
    onPrimary = Color.White,
    primaryContainer = ChakhLeRedContainer,
    onPrimaryContainer = ChakhLeRedDark,
    secondary = ChakhLeAmber,
    onSecondary = Color.White,
    secondaryContainer = ChakhLeAmberLight,
    onSecondaryContainer = ChakhLeAmberDark,
    tertiary = ChakhLeGold,
    background = ChakhLeBackground,
    surface = ChakhLeSurface,
    surfaceVariant = ChakhLeSurfaceVariant,
    onTertiary = Color.Black,
    onBackground = ChakhLeTextPrimary,
    onSurface = ChakhLeTextPrimary,
    onSurfaceVariant = ChakhLeTextSecondary,
    outline = ChakhLeBorder,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our signature warm red & amber food delivery styling
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

