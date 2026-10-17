package com.drape.app

import com.drape.app.core.designsystem.component.GlassElevation
import com.drape.app.core.designsystem.theme.DarkLiquidGlassColors
import com.drape.app.core.designsystem.theme.DrapeComponentShapes
import com.drape.app.core.designsystem.theme.DrapeEditorialTypography
import com.drape.app.core.designsystem.theme.DrapeMotion
import com.drape.app.core.designsystem.theme.DrapeSpacing
import com.drape.app.core.designsystem.theme.GarmentSwatchPalette
import com.drape.app.core.designsystem.theme.LightLiquidGlassColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignSystemTokensTest {

    @Test
    fun garmentSwatchPalette_containsTenDistinctCanonicalColors() {
        val swatches = GarmentSwatchPalette.AllSwatches
        assertEquals(10, swatches.size)
        assertEquals(10, swatches.map { it.id }.toSet().size)
        assertEquals(10, swatches.map { it.hexCode }.toSet().size)
    }

    @Test
    fun darkAndLightLiquidGlassPalettes_provideDistinctBackgroundAndRefractionTokens() {
        assertNotEquals(
            DarkLiquidGlassColors.canvasBackground,
            LightLiquidGlassColors.canvasBackground
        )
        assertTrue(DarkLiquidGlassColors.glassFillTop.alpha in 0.10f..0.40f)
        assertTrue(LightLiquidGlassColors.glassFillTop.alpha in 0.65f..0.95f)
    }

    @Test
    fun editorialTypography_usesTabularNumeralsForMetricsAndBadges() {
        val editorial = DrapeEditorialTypography()
        assertEquals("tnum", editorial.numericMetricLarge.fontFeatureSettings)
        assertEquals("tnum", editorial.numericBadge.fontFeatureSettings)
    }

    @Test
    fun spacingAndMotionTokens_maintainExpectedRanges() {
        val spacing = DrapeSpacing()
        val motion = DrapeMotion()
        val shapes = DrapeComponentShapes()

        assertTrue(spacing.garmentCardAspectRatio in 0.70f..0.85f)
        assertTrue(motion.pressedScaleFactor < 1.0f)
        assertTrue(motion.iconPressedScaleFactor < motion.pressedScaleFactor)
        assertEquals(4, GlassElevation.entries.size)
        assertNotEquals(shapes.garmentCard, shapes.garmentInnerImage)
    }
}
