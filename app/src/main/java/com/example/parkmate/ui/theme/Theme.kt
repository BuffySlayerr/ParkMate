package com.example.parkmate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = Color.White,

    secondary = LightPrimary,
    onSecondary = LightOnPrimary,
    secondaryContainer = LightSecondaryContainerSoft,
    onSecondaryContainer = LightOnSecondaryContainerSoft,

    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,

    background = LightSurfaceBase,
    onBackground = LightOnSurface,

    surface = LightSurfaceBase,
    onSurface = LightOnSurface,

    surfaceVariant = LightSurfaceContainerHigh,
    onSurfaceVariant = LightOnSurfaceVariant,

    outlineVariant = LightOutlineVariantSoft
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkPrimary,

    secondary = DarkPrimary,
    onSecondary = DarkOnPrimary,
    secondaryContainer = DarkSecondaryContainerSoft,
    onSecondaryContainer = DarkOnSecondaryContainerSoft,

    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,

    background = DarkSurfaceBase,
    onBackground = DarkOnSurface,

    surface = DarkSurfaceBase,
    onSurface = DarkOnSurface,

    surfaceVariant = DarkSurfaceContainerHigh,
    onSurfaceVariant = DarkOnSurfaceVariant,

    outlineVariant = DarkOutlineVariantSoft
)

@Immutable
data class ParkMateExtraColors(
    val surfaceBase: Color,
    val surfaceContainerLow: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outlineVariantSoft: Color,
    val secondaryContainerSoft: Color,
    val onSecondaryContainerSoft: Color,
    val successSoft: Color,
    val livePill: Color,
    val gradientStart: Color,
    val gradientEnd: Color
)

private val LightExtraColors = ParkMateExtraColors(
    surfaceBase = LightSurfaceBase,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outlineVariantSoft = LightOutlineVariantSoft,
    secondaryContainerSoft = LightSecondaryContainerSoft,
    onSecondaryContainerSoft = LightOnSecondaryContainerSoft,
    successSoft = LightSuccessSoft,
    livePill = LightLivePill,
    gradientStart = LightGradientStart,
    gradientEnd = LightGradientEnd
)

private val DarkExtraColors = ParkMateExtraColors(
    surfaceBase = DarkSurfaceBase,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outlineVariantSoft = DarkOutlineVariantSoft,
    secondaryContainerSoft = DarkSecondaryContainerSoft,
    onSecondaryContainerSoft = DarkOnSecondaryContainerSoft,
    successSoft = DarkSuccessSoft,
    livePill = DarkLivePill,
    gradientStart = DarkGradientStart,
    gradientEnd = DarkGradientEnd
)

private val LocalParkMateExtraColors = staticCompositionLocalOf {
    LightExtraColors
}

val MaterialTheme.parkMateColors: ParkMateExtraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalParkMateExtraColors.current

@Composable
fun ParkMateTheme(
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val extraColors = if (darkTheme) DarkExtraColors else LightExtraColors

    CompositionLocalProvider(
        LocalParkMateExtraColors provides extraColors
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = ParkMateTypography,
            shapes = ParkMateShapes,
            content = content
        )
    }
}