package com.drape.app

import com.drape.app.core.data.remote.DrapeAiStylistEngine
import com.drape.app.core.data.remote.StylistRefinementOption
import com.drape.app.core.model.DefaultWeatherPresets
import com.drape.app.core.model.DrapeWardrobeStateHolder
import com.drape.app.core.model.GarmentCategory
import com.drape.app.core.model.OccasionVibe
import com.drape.app.core.model.SeasonTag
import com.drape.app.core.model.StylePersonaOption
import com.drape.app.core.model.WardrobeFilterCriteria
import com.drape.app.core.model.filterAndSortGarments
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EndToEndCoreFlowsTest {

    @Test
    fun fullUserJourney_fromEmptyClosetToPhotographedGarmentsAiOutfitWearHistoryAndPreferences() = runTest {
        val state = DrapeWardrobeStateHolder(
            initialGarments = emptyList(),
            initialOutfits = emptyList(),
            initialWearHistory = emptyList()
        )
        val stylistEngine = DrapeAiStylistEngine(isOnlineProvider = { false })

        // 1. Verify Empty States
        assertTrue(state.isWardrobeEmpty)
        assertTrue(state.isLookbookEmpty)
        assertNull(state.scheduledTodayOutfit)

        // 2. Photograph / Import 4 actual garments across categories
        val coat = state.addGarment(
            title = "Double-Breasted Wool Overcoat",
            brand = "Lemaire",
            category = GarmentCategory.OUTERWEAR,
            season = SeasonTag.AUTUMN_WINTER,
            swatchId = "espresso",
            id = "e2e_coat",
            localImageUri = "file:///data/user/0/com.drape.app/files/garments/overcoat.jpg",
            purchasePrice = 540.0,
            fabric = "100% Virgin Wool",
            tags = listOf("tailored", "winter")
        )
        val shirt = state.addGarment(
            title = "Crisp Poplin Button-Down",
            brand = "Auralee",
            category = GarmentCategory.TOPS,
            season = SeasonTag.ALL_SEASONS,
            swatchId = "white",
            id = "e2e_shirt",
            localImageUri = "file:///data/user/0/com.drape.app/files/garments/shirt.jpg",
            purchasePrice = 180.0,
            fabric = "Organic Cotton Poplin"
        )
        val trouser = state.addGarment(
            title = "Wide-Leg Wool Trouser",
            brand = "Studio Nicholson",
            category = GarmentCategory.BOTTOMS,
            season = SeasonTag.ALL_SEASONS,
            swatchId = "black",
            id = "e2e_trouser",
            localImageUri = "file:///data/user/0/com.drape.app/files/garments/trouser.jpg",
            purchasePrice = 280.0,
            fabric = "Wool Gabardine"
        )
        val boots = state.addGarment(
            title = "Chelsea Leather Boots",
            brand = "Paraboot",
            category = GarmentCategory.FOOTWEAR,
            season = SeasonTag.AUTUMN_WINTER,
            swatchId = "black",
            id = "e2e_boots",
            localImageUri = "file:///data/user/0/com.drape.app/files/garments/boots.jpg",
            purchasePrice = 360.0,
            fabric = "Calfskin Leather"
        )

        assertEquals(4, state.garments.size)

        // 3. Search & Filter verification
        val woolFiltered = filterAndSortGarments(
            state.garments,
            WardrobeFilterCriteria(searchQuery = "Wool")
        )
        assertEquals(2, woolFiltered.size)

        // 4. AI Outfit Stylist (Offline Mode) with Locked Coat + Refinement
        val aiResponse = stylistEngine.generateOrRefineOutfit(
            wardrobe = state.garments,
            occasion = OccasionVibe.OFFICE_TAILORED,
            weather = DefaultWeatherPresets.first(),
            lockedGarmentIds = setOf(coat.id),
            refinementOption = StylistRefinementOption.MORE_TAILORED,
            stylePreferences = state.stylePreferences
        )

        assertTrue(coat.id in aiResponse.selectedGarmentIds)
        assertTrue(shirt.id in aiResponse.selectedGarmentIds)
        assertTrue(trouser.id in aiResponse.selectedGarmentIds)
        assertTrue(boots.id in aiResponse.selectedGarmentIds)

        // 5. Save AI Outfit & Schedule for Today
        val savedOutfit = state.createOutfit(
            title = aiResponse.suggestedTitle,
            occasion = OccasionVibe.OFFICE_TAILORED,
            garmentIds = aiResponse.selectedGarmentIds,
            rationale = aiResponse.rationale,
            scheduleToday = true,
            id = "e2e_outfit_1",
            harmonyScore = aiResponse.harmony.overallMatchScore
        )
        assertNotNull(savedOutfit)
        assertEquals("e2e_outfit_1", state.scheduledTodayOutfit?.id)

        // 6. Favorite Outfit & Garment, Log Wear, and Check Cost-Per-Wear + Wear History
        state.toggleFavorite(coat.id)
        state.toggleOutfitFavorite("e2e_outfit_1")
        assertEquals(1, state.favoriteGarmentsCount)
        assertEquals(1, state.favoriteOutfitsCount)

        val wearEntry = state.logOutfitWear(
            outfitId = "e2e_outfit_1",
            notes = "Wore to architecture exhibition",
            dateLabel = "Today"
        )
        assertNotNull(wearEntry)
        assertEquals(1, state.wearHistory.size)
        assertEquals(1, state.getGarmentById(coat.id)?.wearCount)
        assertEquals(540.0, state.getGarmentById(coat.id)?.costPerWear ?: 0.0, 0.01)

        // Log a second wear -> coat cost-per-wear drops to $270.00
        state.logOutfitWear(outfitId = "e2e_outfit_1", dateLabel = "Day 2")
        assertEquals(2, state.getGarmentById(coat.id)?.wearCount)
        assertEquals(270.0, state.getGarmentById(coat.id)?.costPerWear ?: 0.0, 0.01)

        // 7. Personal Style Preferences update
        state.toggleStylePersona(StylePersonaOption.NATURAL_FIBERS)
        assertTrue(StylePersonaOption.NATURAL_FIBERS in state.stylePreferences.selectedPersonas)
    }
}
