package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val NeonBastionColorScheme =
  darkColorScheme(
    primary = NeonCyan,
    onPrimary = SpaceObsidian,
    primaryContainer = DeepCobalt,
    onPrimaryContainer = IceBlue,
    secondary = ElectricBlue,
    onSecondary = TextPrimary,
    secondaryContainer = SpaceCardElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = CrystalViolet,
    onTertiary = TextPrimary,
    error = EnemyCrimson,
    onError = TextPrimary,
    background = SpaceObsidian,
    onBackground = TextPrimary,
    surface = SpaceSlate,
    onSurface = TextPrimary,
    surfaceVariant = SpaceCard,
    onSurfaceVariant = TextMuted,
    outline = OutlineNeon,
  )

private val NeonBastionShapes =
  Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(18.dp),
  )

@Composable
fun NeonBastionTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = NeonBastionColorScheme,
    typography = AppTypography,
    shapes = NeonBastionShapes,
    content = content,
  )
}
