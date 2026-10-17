package com.drape.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * DRAPE palette inspired by Apple Liquid Glass:
 * Deep obsidian and warm alabaster bases paired with multi-stop translucent glass layers,
 * inner refraction highlights, and crisp specular rim borders.
 */
object DrapePalette {
    // Dark theme atmospheric base
    val ObsidianBase = Color(0xFF0A0C10)
    val ObsidianElevated = Color(0xFF12161F)
    val ObsidianCard = Color(0xFF181D29)

    // Light theme atmospheric base
    val AlabasterBase = Color(0xFFF5F6F9)
    val AlabasterElevated = Color(0xFFFFFFFF)
    val AlabasterCard = Color(0xFFEEF1F6)

    // Typography & Iconography
    val InkPrimaryDark = Color(0xFFF5F7FA)
    val InkSecondaryDark = Color(0xFFA8B0C2)
    val InkMutedDark = Color(0xFF6C7486)

    val InkPrimaryLight = Color(0xFF111318)
    val InkSecondaryLight = Color(0xFF525866)
    val InkMutedLight = Color(0xFF8A919E)

    // Editorial Accents & Semantic Status
    val ChampagneAccent = Color(0xFFDCC7AA)
    val ChampagneAccentDeep = Color(0xFFB39774)
    val IcyBlueAccent = Color(0xFF8AB4F8)
    val IcyBlueAccentDeep = Color(0xFF3B78E7)
    val EmeraldStatus = Color(0xFF4ADE80)
    val AmberStatus = Color(0xFFFBBF24)
    val CoralDanger = Color(0xFFF87171)
}

/**
 * Canonical wardrobe garment color swatches for consistent tagging, filtering, and AI color harmony.
 */
@Immutable
data class GarmentColorSwatch(
    val id: String,
    val label: String,
    val color: Color,
    val hexCode: String
)

object GarmentSwatchPalette {
    val ObsidianBlack = GarmentColorSwatch("black", "Obsidian", Color(0xFF141619), "#141619")
    val ChalkWhite = GarmentColorSwatch("white", "Chalk", Color(0xFFF7F7F5), "#F7F7F5")
    val HeatherGrey = GarmentColorSwatch("grey", "Heather", Color(0xFF8E95A2), "#8E95A2")
    val CamelTan = GarmentColorSwatch("camel", "Camel", Color(0xFFC8A986), "#C8A986")
    val EspressoBrown = GarmentColorSwatch("espresso", "Espresso", Color(0xFF4B382A), "#4B382A")
    val NavyInk = GarmentColorSwatch("navy", "Navy", Color(0xFF1E2D4A), "#1E2D4A")
    val SlateBlue = GarmentColorSwatch("slate", "Slate Blue", Color(0xFF6482A6), "#6482A6")
    val SageOlive = GarmentColorSwatch("sage", "Sage Olive", Color(0xFF6E7D68), "#6E7D68")
    val BurgundyWine = GarmentColorSwatch("burgundy", "Burgundy", Color(0xFF6D2836), "#6D2836")
    val ChampagneGold = GarmentColorSwatch("champagne", "Champagne", Color(0xFFDCC7AA), "#DCC7AA")

    val AllSwatches: List<GarmentColorSwatch> = listOf(
        ObsidianBlack,
        ChalkWhite,
        HeatherGrey,
        CamelTan,
        EspressoBrown,
        NavyInk,
        SlateBlue,
        SageOlive,
        BurgundyWine,
        ChampagneGold
    )
}

/**
 * Immutable glass token set to prevent unnecessary recompositions during 60 FPS animations.
 */
@Immutable
data class DrapeGlassColors(
    val canvasBackground: Color,
    val canvasSecondaryBackground: Color,
    val ambientGlowPrimary: Color,
    val ambientGlowSecondary: Color,
    val glassFillSubtleTop: Color,
    val glassFillSubtleBottom: Color,
    val glassFillTop: Color,
    val glassFillBottom: Color,
    val glassFillFloatingTop: Color,
    val glassFillFloatingBottom: Color,
    val glassFillElevated: Color,
    val specularRimTop: Color,
    val specularRimBottom: Color,
    val specularRimFocus: Color,
    val innerRefractionHighlight: Color,
    val glassScrim: Color,
    val shimmerBase: Color,
    val shimmerHighlight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val statusSuccess: Color,
    val statusWarning: Color,
    val statusDanger: Color,
    val isDark: Boolean
)

val DarkLiquidGlassColors = DrapeGlassColors(
    canvasBackground = DrapePalette.ObsidianBase,
    canvasSecondaryBackground = DrapePalette.ObsidianElevated,
    ambientGlowPrimary = Color(0x28DCC7AA),
    ambientGlowSecondary = Color(0x228AB4F8),
    glassFillSubtleTop = Color(0x1FFFFFFF),
    glassFillSubtleBottom = Color(0x0AFFFFFF),
    glassFillTop = Color(0x2EFFFFFF),
    glassFillBottom = Color(0x12FFFFFF),
    glassFillFloatingTop = Color(0x421E2433),
    glassFillFloatingBottom = Color(0x2812161F),
    glassFillElevated = Color(0x38FFFFFF),
    specularRimTop = Color(0x59FFFFFF),
    specularRimBottom = Color(0x14FFFFFF),
    specularRimFocus = Color(0xB3DCC7AA),
    innerRefractionHighlight = Color(0x33FFFFFF),
    glassScrim = Color(0x9905070A),
    shimmerBase = Color(0x18FFFFFF),
    shimmerHighlight = Color(0x3DFFFFFF),
    textPrimary = DrapePalette.InkPrimaryDark,
    textSecondary = DrapePalette.InkSecondaryDark,
    textMuted = DrapePalette.InkMutedDark,
    accentPrimary = DrapePalette.ChampagneAccent,
    accentSecondary = DrapePalette.IcyBlueAccent,
    statusSuccess = DrapePalette.EmeraldStatus,
    statusWarning = DrapePalette.AmberStatus,
    statusDanger = DrapePalette.CoralDanger,
    isDark = true
)

val LightLiquidGlassColors = DrapeGlassColors(
    canvasBackground = DrapePalette.AlabasterBase,
    canvasSecondaryBackground = DrapePalette.AlabasterElevated,
    ambientGlowPrimary = Color(0x38DCC7AA),
    ambientGlowSecondary = Color(0x268AB4F8),
    glassFillSubtleTop = Color(0x99FFFFFF),
    glassFillSubtleBottom = Color(0x59FFFFFF),
    glassFillTop = Color(0xCCFFFFFF),
    glassFillBottom = Color(0x88FFFFFF),
    glassFillFloatingTop = Color(0xE6FFFFFF),
    glassFillFloatingBottom = Color(0xB8F5F6F9),
    glassFillElevated = Color(0xF2FFFFFF),
    specularRimTop = Color(0xFFFFFFFF),
    specularRimBottom = Color(0x26111318),
    specularRimFocus = Color(0xCCB39774),
    innerRefractionHighlight = Color(0xCCFFFFFF),
    glassScrim = Color(0x66111318),
    shimmerBase = Color(0x66FFFFFF),
    shimmerHighlight = Color(0xE6FFFFFF),
    textPrimary = DrapePalette.InkPrimaryLight,
    textSecondary = DrapePalette.InkSecondaryLight,
    textMuted = DrapePalette.InkMutedLight,
    accentPrimary = DrapePalette.ChampagneAccentDeep,
    accentSecondary = DrapePalette.IcyBlueAccentDeep,
    statusSuccess = Color(0xFF16A34A),
    statusWarning = Color(0xFFD97706),
    statusDanger = Color(0xFFDC2626),
    isDark = false
)
