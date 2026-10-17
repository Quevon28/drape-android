package com.drape.app

import com.drape.app.core.data.local.entity.toDomainModel
import com.drape.app.core.data.local.entity.toEntity
import com.drape.app.core.model.DrapeWardrobeStateHolder
import com.drape.app.core.model.OccasionVibe
import com.drape.app.core.model.StylePersonaOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedOutfitsHistoryAndPreferencesTest {

    @Test
    fun outfitSavingAndFavorites_toggleAndPersistCleanly() {
        val state = DrapeWardrobeStateHolder()
        val outfit = state.createOutfit(
            title = "Evening Monochrome Column",
            occasion = OccasionVibe.EVENING_EDITORIAL,
            garmentIds = listOf("garment_poplin_shirt", "garment_pleated_trouser", "garment_leather_derby"),
            rationale = "High contrast shirting and pleated wool.",
            scheduleToday = false,
            id = "outfit_fav_test",
            isFavorite = false
        )
        assertNotNull(outfit)
        assertFalse(state.outfits.first { it.id == "outfit_fav_test" }.isFavorite)

        state.toggleOutfitFavorite("outfit_fav_test")
        val toggled = state.outfits.first { it.id == "outfit_fav_test" }
        assertTrue(toggled.isFavorite)

        // Verify Room entity round-trip preserves isFavorite, harmonyScore, and notes
        val roundTrip = toggled.toEntity().toDomainModel()
        assertEquals(toggled.id, roundTrip.id)
        assertEquals(toggled.isFavorite, roundTrip.isFavorite)
        assertEquals(toggled.harmonyScore, roundTrip.harmonyScore)
    }

    @Test
    fun loggingOutfitWear_createsChronologicalWearHistoryEntryAndIncrementsGarments() {
        val state = DrapeWardrobeStateHolder()
        val initialHistorySize = state.wearHistory.size
        val targetOutfitId = "outfit_editorial_commute"
        val initialTrenchWears = state.getGarmentById("garment_trench")!!.wearCount

        val historyEntry = state.logOutfitWear(
            outfitId = targetOutfitId,
            notes = "Wore to client dinner",
            dateLabel = "Today"
        )
        assertNotNull(historyEntry)
        assertEquals(initialHistorySize + 1, state.wearHistory.size)
        assertEquals("Wore to client dinner", state.wearHistory.first().notes)
        assertEquals(initialTrenchWears + 1, state.getGarmentById("garment_trench")!!.wearCount)

        // Verify WearHistoryEntity Room round-trip
        val entityRoundTrip = historyEntry!!.toEntity().toDomainModel()
        assertEquals(historyEntry.id, entityRoundTrip.id)
        assertEquals(historyEntry.outfitId, entityRoundTrip.outfitId)
        assertEquals(historyEntry.garmentIds, entityRoundTrip.garmentIds)
        assertEquals(historyEntry.notes, entityRoundTrip.notes)

        // Delete wear history entry
        state.deleteWearHistoryEntry(historyEntry.id)
        assertEquals(initialHistorySize, state.wearHistory.size)
    }

    @Test
    fun personalStylePreferences_updatePersonasSwatchesAndRotationRules() {
        val state = DrapeWardrobeStateHolder()
        assertFalse(StylePersonaOption.HERITAGE_CRAFT in state.stylePreferences.selectedPersonas)

        state.toggleStylePersona(StylePersonaOption.HERITAGE_CRAFT)
        assertTrue(StylePersonaOption.HERITAGE_CRAFT in state.stylePreferences.selectedPersonas)

        state.togglePreferredSwatch("sage")
        assertTrue("sage" in state.stylePreferences.preferredSwatchIds)

        state.setDefaultOccasion(OccasionVibe.TRAVEL_LAYERING)
        assertEquals(OccasionVibe.TRAVEL_LAYERING, state.stylePreferences.defaultOccasion)

        state.setPrioritizeUnwornInAi(false)
        assertFalse(state.stylePreferences.prioritizeUnwornInAi)
    }
}
