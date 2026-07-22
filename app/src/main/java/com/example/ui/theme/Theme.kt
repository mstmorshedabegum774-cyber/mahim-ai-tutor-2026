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
    primary = Primary80,
    onPrimary = Color(0xFF1C360E),
    primaryContainer = Color(0xFF2D3E21),
    onPrimaryContainer = Color(0xFFE8F3D6),
    secondary = Secondary80,
    onSecondary = Color(0xFF5D1D0C),
    secondaryContainer = Color(0xFF5D4037),
    onSecondaryContainer = Color(0xFFFFF0EC),
    tertiary = Tertiary80,
    onTertiary = Color(0xFF422C00),
    tertiaryContainer = Color(0xFF5F4B32),
    onTertiaryContainer = Color(0xFFFCF5E5),
    background = Color(0xFF121411),
    onBackground = Color(0xFFE2E3DC),
    surface = Color(0xFF1B1E19),
    onSurface = Color(0xFFE2E3DC),
    surfaceVariant = Color(0xFF282C25),
    onSurfaceVariant = Color(0xFFC3C8BD)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Primary40,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = Secondary40,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = Tertiary40,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
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
