package com.arin.tube18.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val Charcoal = Color(0xFF14141A)
val Orange = Color(0xFFFF6A00)

private val DarkColors = darkColorScheme(
    primary = Orange,
    onPrimary = Color(0xFF1A0B00),
    primaryContainer = Color(0xFF4A2200),
    onPrimaryContainer = Color(0xFFFFDBC8),
    secondary = Color(0xFFFFB68C),
    onSecondary = Color(0xFF2B1300),
    secondaryContainer = Color(0xFF3A2416),
    onSecondaryContainer = Color(0xFFFFB68C),
    tertiary = Color(0xFFFFB68C),
    background = Charcoal,
    onBackground = Color(0xFFECECF1),
    surface = Charcoal,
    onSurface = Color(0xFFECECF1),
    surfaceVariant = Color(0xFF23232C),
    onSurfaceVariant = Color(0xFFA6A6B4),
    surfaceContainerLowest = Color(0xFF101015),
    surfaceContainerLow = Color(0xFF18181F),
    surfaceContainer = Color(0xFF1B1B22),
    surfaceContainerHigh = Color(0xFF24242D),
    surfaceContainerHighest = Color(0xFF2C2C36),
    outline = Color(0xFF4A4A57),
    outlineVariant = Color(0xFF33333D),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF3B1A1A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFFD45500),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBC8),
    onPrimaryContainer = Color(0xFF331200),
    secondary = Color(0xFF8A4F2A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBC8),
    onSecondaryContainer = Color(0xFF331200),
    background = Color(0xFFF7F7FA),
    onBackground = Color(0xFF1A1A20),
    surface = Color(0xFFF7F7FA),
    onSurface = Color(0xFF1A1A20),
    surfaceVariant = Color(0xFFE6E6EC),
    onSurfaceVariant = Color(0xFF4A4A57),
    surfaceContainer = Color(0xFFEDEDF2),
    outline = Color(0xFF7A7A88),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** The approved design is the dark charcoal theme, so dark is the default. */
@Composable
fun TubeTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        typography = Typography(),
        content = content
    )
}
