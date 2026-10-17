package com.drape.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalDrapeGlassColors = staticCompositionLocalOf { DarkLiquidGlassColors }
val LocalDrapeMotion = staticCompositionLocalOf { DrapeMotion() }
val LocalDrapeSpacing = staticCompositionLocalOf { DrapeSpacing() }
val LocalDrapeComponentShapes = staticCompositionLocalOf { DrapeComponentShapes() }
val LocalDrapeEditorialTypography = staticCompositionLocalOf { DrapeEditorialTypography() }

private val DarkMaterialScheme = darkColorScheme(
    primary = DrapePalette.ChampagneAccent,
    secondary = DrapePalette.IcyBlueAccent,
    background = DrapePalette.ObsidianBase,
    surface = DrapePalette.ObsidianElevated,
    error = DrapePalette.CoralDanger,
    onPrimary = DrapePalette.ObsidianBase,
    onBackground = DrapePalette.InkPrimaryDark,
    onSurface = DrapePalette.InkPrimaryDark
)

private val LightMaterialScheme = lightColorScheme(
    primary = DrapePalette.ChampagneAccentDeep,
    secondary = DrapePalette.IcyBlueAccentDeep,
    background = DrapePalette.AlabasterBase,
    surface = DrapePalette.AlabasterElevated,
    error = DrapePalette.CoralDanger,
    onPrimary = DrapePalette.AlabasterElevated,
    onBackground = DrapePalette.InkPrimaryLight,
    onSurface = DrapePalette.InkPrimaryLight
)

@Composable
fun DrapeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val glassColors = if (darkTheme) DarkLiquidGlassColors else LightLiquidGlassColors
    val colorScheme = if (darkTheme) DarkMaterialScheme else LightMaterialScheme
    val motion = DrapeMotion()
    val spacing = DrapeSpacing()
    val componentShapes = DrapeComponentShapes()
    val editorialTypography = DrapeEditorialTypography()

    CompositionLocalProvider(
        LocalDrapeGlassColors provides glassColors,
        LocalDrapeMotion provides motion,
        LocalDrapeSpacing provides spacing,
        LocalDrapeComponentShapes provides componentShapes,
        LocalDrapeEditorialTypography provides editorialTypography
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DrapeTypography,
            shapes = DrapeShapes,
            content = content
        )
    }
}

object DrapeThemeTokens {
    val glassColors: DrapeGlassColors
        @Composable
        @ReadOnlyComposable
        get() = LocalDrapeGlassColors.current

    val motion: DrapeMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalDrapeMotion.current

    val spacing: DrapeSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalDrapeSpacing.current

    val componentShapes: DrapeComponentShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalDrapeComponentShapes.current

    val editorialTypography: DrapeEditorialTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalDrapeEditorialTypography.current
}
