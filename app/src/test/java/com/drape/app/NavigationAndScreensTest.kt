package com.drape.app

import com.drape.app.core.model.DefaultWeatherPresets
import com.drape.app.core.model.DrapeWardrobeStateHolder
import com.drape.app.core.model.GarmentCategory
import com.drape.app.core.model.OccasionVibe
import com.drape.app.core.model.SeasonTag
import com.drape.app.core.model.StarterCapsuleCatalog
import com.drape.app.navigation.DrapeMainTabDestinations
import com.drape.app.navigation.ProfileDestination
import com.drape.app.navigation.StyleDestination
import com.drape.app.navigation.TodayDestination
import com.drape.app.navigation.WardrobeDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationAndScreensTest {

    @Test
    fun mainNavigation_exposesFourOrderedTopLevelDestinations() {
        assertEquals(
            listOf(
                TodayDestination,
                WardrobeDestination,
                StyleDestination,
                ProfileDestination
            ),
            DrapeMainTabDestinations
        )
        assertEquals(
            listOf("Today", "Wardrobe", "Style", "Profile"),
            DrapeMainTabDestinations.map { it.label }
        )
        assertEquals(
            4,
            DrapeMainTabDestinations.map { it.routeKey }.toSet().size
        )
    }

    @Test
    fun starterCapsuleCatalog_containsValidGarmentsAndScheduledTodayOutfit() {
        val garments = StarterCapsuleCatalog.InitialGarments
        val outfits = StarterCapsuleCatalog.InitialOutfits

        assertTrue(garments.size >= 4)
        assertTrue(outfits.isNotEmpty())

        val garmentIds = garments.map { it.id }.toSet()
        outfits.forEach { outfit ->
            assertTrue(outfit.garmentIds.all { it in garmentIds })
        }
        assertTrue(outfits.any { it.isScheduledForToday })
    }

    @Test
    fun wardrobeStateHolder_transitionsBetweenEmptyStatesAndActiveCloset() {
        val state = DrapeWardrobeStateHolder()
        assertFalse(state.isWardrobeEmpty)
        assertNotNull(state.scheduledTodayOutfit)

        // Clear all to trigger Today, Wardrobe, Style, and Profile empty states
        state.clearAll()
        assertTrue(state.isWardrobeEmpty)
        assertTrue(state.isLookbookEmpty)
        assertNull(state.scheduledTodayOutfit)
        assertEquals(0, state.totalGarmentWears)

        // Add a custom garment and create a scheduled outfit
        val added = state.addGarment(
            title = "Linen Overshirt",
            brand = "Auralee",
            category = GarmentCategory.TOPS,
            season = SeasonTag.SPRING_SUMMER,
            swatchId = "champagne",
            id = "custom_1"
        )
        assertFalse(state.isWardrobeEmpty)
        assertEquals("Linen Overshirt", added.title)

        val createdOutfit = state.createOutfit(
            title = "Warm Gallery Look",
            occasion = OccasionVibe.EVERYDAY_MINIMAL,
            garmentIds = listOf(added.id),
            rationale = "Lightweight linen for humid days.",
            scheduleToday = true,
            id = "outfit_custom_1"
        )
        assertNotNull(createdOutfit)
        assertEquals("outfit_custom_1", state.scheduledTodayOutfit?.id)

        // Logging wear increments both outfit timesWorn and constituent garment wearCount
        state.logOutfitWear("outfit_custom_1")
        assertEquals(2, state.scheduledTodayOutfit?.timesWorn)
        assertEquals(1, state.garments.first { it.id == "custom_1" }.wearCount)
    }

    @Test
    fun domainEnumsAndPresets_coverAllRequiredCategoriesAndVibes() {
        assertEquals(6, GarmentCategory.entries.size)
        assertEquals(5, OccasionVibe.entries.size)
        assertTrue(DefaultWeatherPresets.isNotEmpty())
    }
}
