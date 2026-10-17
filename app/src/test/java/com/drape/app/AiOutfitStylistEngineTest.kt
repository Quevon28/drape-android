package com.drape.app

import com.drape.app.core.data.remote.DrapeAiStylistEngine
import com.drape.app.core.data.remote.GarmentPhotoVisionAnalyzer
import com.drape.app.core.data.remote.StylistRefinementOption
import com.drape.app.core.model.DefaultWeatherPresets
import com.drape.app.core.model.GarmentCategory
import com.drape.app.core.model.OccasionVibe
import com.drape.app.core.model.StarterCapsuleCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiOutfitStylistEngineTest {

    private val engine = DrapeAiStylistEngine()
    private val wardrobe = StarterCapsuleCatalog.InitialGarments

    @Test
    fun composeWithOnDeviceEngine_createsCompleteOutfitFromActualWardrobeItems() {
        val response = engine.composeWithOnDeviceEngine(
            wardrobe = wardrobe,
            occasion = OccasionVibe.OFFICE_TAILORED,
            weather = DefaultWeatherPresets.first()
        )

        val validIds = wardrobe.map { it.id }.toSet()
        assertTrue(response.selectedGarmentIds.isNotEmpty())
        assertTrue(response.selectedGarmentIds.all { it in validIds })

        val categories = response.selectedGarmentIds.mapNotNull { id ->
            wardrobe.firstOrNull { it.id == id }?.category
        }.toSet()

        // Complete outfit includes Top, Bottom, and Footwear
        assertTrue(GarmentCategory.TOPS in categories)
        assertTrue(GarmentCategory.BOTTOMS in categories)
        assertTrue(GarmentCategory.FOOTWEAR in categories)
        assertTrue(response.harmony.overallMatchScore in 60..100)
    }

    @Test
    fun shuffleWithExcludedSignatures_producesDistinctCombinationWhilePreservingLockedItems() {
        val lockedId = "garment_trench" // Structured Wool Trench (Outerwear)
        val first = engine.composeWithOnDeviceEngine(
            wardrobe = wardrobe,
            occasion = OccasionVibe.EVERYDAY_MINIMAL,
            weather = DefaultWeatherPresets.first(),
            lockedGarmentIds = setOf(lockedId)
        )

        assertTrue(lockedId in first.selectedGarmentIds)

        val second = engine.composeWithOnDeviceEngine(
            wardrobe = wardrobe,
            occasion = OccasionVibe.EVERYDAY_MINIMAL,
            weather = DefaultWeatherPresets.first(),
            lockedGarmentIds = setOf(lockedId),
            excludedSignatures = setOf(first.combinationSignature)
        )

        // Locked item MUST still be present, and overall combination must be different
        assertTrue(lockedId in second.selectedGarmentIds)
        assertNotEquals(first.combinationSignature, second.combinationSignature)
    }

    @Test
    fun refinements_applyWarmerAndLighterAndCustomPromptAdjustmentsRespectingLocks() {
        val lockedShoe = "garment_leather_derby"

        val lighterResponse = engine.composeWithOnDeviceEngine(
            wardrobe = wardrobe,
            occasion = OccasionVibe.WEEKEND_RELAXED,
            weather = DefaultWeatherPresets[1], // Warm Sun
            lockedGarmentIds = setOf(lockedShoe),
            refinementOption = StylistRefinementOption.LIGHTER_BREATHABLE
        )
        assertTrue(lockedShoe in lighterResponse.selectedGarmentIds)
        val lighterCategories = lighterResponse.selectedGarmentIds.mapNotNull { id ->
            wardrobe.firstOrNull { it.id == id }?.category
        }
        assertFalse(GarmentCategory.OUTERWEAR in lighterCategories)

        // Refine with custom prompt requesting "denim" and "merino"
        val customRefined = engine.composeWithOnDeviceEngine(
            wardrobe = wardrobe,
            occasion = OccasionVibe.WEEKEND_RELAXED,
            weather = DefaultWeatherPresets.first(),
            lockedGarmentIds = setOf(lockedShoe),
            currentOutfitGarmentIds = lighterResponse.selectedGarmentIds,
            customRefinementPrompt = "pair with selvedge denim and merino crewneck"
        )
        assertTrue(lockedShoe in customRefined.selectedGarmentIds)
        assertTrue("garment_raw_denim" in customRefined.selectedGarmentIds)
        assertTrue("garment_merino_crew" in customRefined.selectedGarmentIds)
        assertNotNull(customRefined.refinementSummary)
    }

    @Test
    fun visionAnalyzer_computesAccurateLuminanceAndWarmthFromSwatchAndPhotoMetadata() {
        val chalkWhite = wardrobe.first { it.id == "garment_poplin_shirt" }
        val obsidianBlack = wardrobe.first { it.id == "garment_pleated_trouser" }

        val whiteSig = GarmentPhotoVisionAnalyzer.extractSignatureSync(chalkWhite)
        val blackSig = GarmentPhotoVisionAnalyzer.extractSignatureSync(obsidianBlack)

        assertTrue(whiteSig.relativeLuminance > 0.85f)
        assertTrue(blackSig.relativeLuminance < 0.15f)
        assertEquals(chalkWhite.id, whiteSig.garmentId)
    }
}
