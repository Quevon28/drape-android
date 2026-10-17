package com.drape.app

import com.drape.app.core.designsystem.theme.DarkLiquidGlassColors
import com.drape.app.core.designsystem.theme.DrapeMotion
import com.drape.app.core.designsystem.theme.LightLiquidGlassColors
import com.drape.app.navigation.AiStylistDestination
import com.drape.app.navigation.OutfitStudioDestination
import com.drape.app.navigation.WardrobeDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FoundationConfigTest {

    @Test
    fun liquidGlassPalettes_haveExpectedDarkAndLightFlags() {
        assertTrue(DarkLiquidGlassColors.isDark)
        assertFalse(LightLiquidGlassColors.isDark)
    }

    @Test
    fun drapeMotion_pressedScaleFactorIsSubtleAndResponsive() {
        val motion = DrapeMotion()
        assertTrue(motion.pressedScaleFactor in 0.90f..0.99f)
    }

    @Test
    fun topLevelDestinations_exposeDistinctLabels() {
        val labels = setOf(
            WardrobeDestination.label,
            OutfitStudioDestination.label,
            AiStylistDestination.label
        )
        assertEquals(3, labels.size)
    }
}
