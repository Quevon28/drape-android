package com.drape.app

import com.drape.app.core.designsystem.theme.DrapeWindowWidthClass
import com.drape.app.core.designsystem.theme.calculateResponsiveLayoutMetrics
import com.drape.app.core.model.GarmentInputValidationResult
import com.drape.app.core.model.WardrobeInputValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolishPerformanceAndResponsiveTest {

    @Test
    fun responsiveLayoutMetrics_adaptColumnsForPhoneFoldableAndTablet() {
        val compactPortrait = calculateResponsiveLayoutMetrics(screenWidthDp = 393, screenHeightDp = 852)
        assertEquals(DrapeWindowWidthClass.COMPACT, compactPortrait.widthClass)
        assertEquals(2, compactPortrait.wardrobeGridColumns)
        assertEquals(2, compactPortrait.outfitSlotColumns)

        val foldableUnfolded = calculateResponsiveLayoutMetrics(screenWidthDp = 700, screenHeightDp = 840)
        assertEquals(DrapeWindowWidthClass.MEDIUM, foldableUnfolded.widthClass)
        assertEquals(3, foldableUnfolded.wardrobeGridColumns)
        assertEquals(3, foldableUnfolded.outfitSlotColumns)

        val tabletExpanded = calculateResponsiveLayoutMetrics(screenWidthDp = 1024, screenHeightDp = 768)
        assertEquals(DrapeWindowWidthClass.EXPANDED, tabletExpanded.widthClass)
        assertEquals(4, tabletExpanded.wardrobeGridColumns)
        assertEquals(4, tabletExpanded.outfitSlotColumns)
    }

    @Test
    fun wardrobeInputValidator_rejectsBlankTitleAndInvalidPriceAndAcceptsValidInput() {
        val blankTitle = WardrobeInputValidator.validateGarmentDraft(title = "   ", priceText = "120")
        assertTrue(blankTitle is GarmentInputValidationResult.Invalid)
        assertEquals("title", (blankTitle as GarmentInputValidationResult.Invalid).field)

        val negativePrice = WardrobeInputValidator.validateGarmentDraft(title = "Linen Shirt", priceText = "-45")
        assertTrue(negativePrice is GarmentInputValidationResult.Invalid)
        assertEquals("price", (negativePrice as GarmentInputValidationResult.Invalid).field)

        val nonNumericPrice = WardrobeInputValidator.validateGarmentDraft(title = "Linen Shirt", priceText = "abc")
        assertTrue(nonNumericPrice is GarmentInputValidationResult.Invalid)

        val valid = WardrobeInputValidator.validateGarmentDraft(title = "Linen Shirt", priceText = "165.50")
        assertEquals(GarmentInputValidationResult.Valid, valid)
    }
}
