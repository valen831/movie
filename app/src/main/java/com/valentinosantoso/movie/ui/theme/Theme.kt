package com.valentinosantoso.movie.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.valentinosantoso.movie.data.local.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryRed,
    onPrimary = Color.White,
    primaryContainer = PrimaryRedVariant,
    onPrimaryContainer = Color.White,
    secondary = StarGold,
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryRed,
    onPrimary = Color.White,
    primaryContainer = PrimaryRedVariant,
    onPrimaryContainer = Color.White,
    secondary = StarGold,
    onSecondary = Color.Black,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightCardBorder
)

@Composable
fun MovieTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = if (themeMode == ThemeMode.DARK) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
