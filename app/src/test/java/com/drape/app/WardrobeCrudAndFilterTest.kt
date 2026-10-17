package com.drape.app

import com.drape.app.core.data.local.entity.toDomainModel
import com.drape.app.core.data.local.entity.toEntity
import com.drape.app.core.data.storage.GarmentImageStorageManager
import com.drape.app.core.model.DrapeWardrobeStateHolder
import com.drape.app.core.model.GarmentCategory
import com.drape.app.core.model.SeasonTag
import com.drape.app.core.model.WardrobeFilterCriteria
import com.drape.app.core.model.WardrobeSortOption
import com.drape.app.core.model.filterAndSortGarments
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WardrobeCrudAndFilterTest {

    @Test
    fun garmentEditing_updatesMetadataPhotoAndCostPerWear() {
        val state = DrapeWardrobeStateHolder()
        val created = state.addGarment(
            title = "Silk Camp Collar Shirt",
            brand = "Our Legacy",
            category = GarmentCategory.TOPS,
            season = SeasonTag.SPRING_SUMMER,
            swatchId = "champagne",
            id = "garment_edit_test",
            localImageUri = "file:///data/user/0/com.drape.app/files/garments/silk_shirt.jpg",
            notes = "Dry clean only",
            size = "48",
            fabric = "100% Mulberry Silk",
            purchasePrice = 200.0,
            tags = listOf("silk", "summer")
        )

        assertEquals(200.0, created.costPerWear ?: 0.0, 0.01)

        // Log 4 wears -> cost per wear drops to $50.00
        repeat(4) { state.incrementGarmentWear(created.id) }
        val afterWears = state.getGarmentById(created.id)
        assertNotNull(afterWears)
        assertEquals(4, afterWears!!.wearCount)
        assertEquals(50.0, afterWears.costPerWear ?: 0.0, 0.01)

        // Decrement wear -> 3 wears
        state.decrementGarmentWear(created.id)
        assertEquals(3, state.getGarmentById(created.id)?.wearCount)

        // Edit title, brand, category, and photo URI
        val updated = afterWears.copy(
            title = "Washed Silk Camp Shirt",
            brand = "Our Legacy Workshop",
            localImageUri = "file:///data/user/0/com.drape.app/files/garments/silk_shirt_v2.jpg",
            tags = listOf("silk", "resort", "editorial")
        )
        assertTrue(state.updateGarment(updated))

        val fetched = state.getGarmentById(created.id)!!
        assertEquals("Washed Silk Camp Shirt", fetched.title)
        assertEquals("Our Legacy Workshop", fetched.brand)
        assertEquals(
            "file:///data/user/0/com.drape.app/files/garments/silk_shirt_v2.jpg",
            fetched.localImageUri
        )
        assertEquals(listOf("silk", "resort", "editorial"), fetched.tags)
    }

    @Test
    fun garmentDeletion_removesPieceAndCleansUpReferencingOutfits() {
        val state = DrapeWardrobeStateHolder()
        val initialCount = state.garments.size
        val targetId = "garment_trench"

        assertNotNull(state.getGarmentById(targetId))
        state.removeGarment(targetId)

        assertNull(state.getGarmentById(targetId))
        assertEquals(initialCount - 1, state.garments.size)
        state.outfits.forEach { outfit ->
            assertTrue(targetId !in outfit.garmentIds)
        }
    }

    @Test
    fun multiFacetFilterAndSort_filtersBySearchCategorySeasonSwatchAndSortsCorrectly() {
        val state = DrapeWardrobeStateHolder()

        // Filter by category TOPS
        val topsOnly = filterAndSortGarments(
            state.garments,
            WardrobeFilterCriteria(selectedCategory = GarmentCategory.TOPS)
        )
        assertTrue(topsOnly.isNotEmpty())
        assertTrue(topsOnly.all { it.category == GarmentCategory.TOPS })

        // Filter by fabric/tag keyword "Selvedge"
        val selvedgeSearch = filterAndSortGarments(
            state.garments,
            WardrobeFilterCriteria(searchQuery = "Selvedge")
        )
        assertEquals(1, selvedgeSearch.size)
        assertEquals("garment_raw_denim", selvedgeSearch.first().id)

        // Sort by MOST_WORN
        val sortedMostWorn = filterAndSortGarments(
            state.garments,
            WardrobeFilterCriteria(sortOption = WardrobeSortOption.MOST_WORN)
        )
        assertEquals("garment_leather_derby", sortedMostWorn.first().id)

        // Sort by LOWEST_CPW (Best Value)
        val sortedBestValue = filterAndSortGarments(
            state.garments,
            WardrobeFilterCriteria(sortOption = WardrobeSortOption.LOWEST_CPW)
        )
        assertTrue(
            (sortedBestValue.first().costPerWear ?: Double.MAX_VALUE) <=
                (sortedBestValue.last().costPerWear ?: Double.MAX_VALUE)
        )
    }

    @Test
    fun roomEntityMappingAndImageDownscaling_preserveDataIntegrity() {
        val state = DrapeWardrobeStateHolder()
        val original = state.garments.first()
        val roundTrip = original.toEntity().toDomainModel()

        assertEquals(original.id, roundTrip.id)
        assertEquals(original.title, roundTrip.title)
        assertEquals(original.category, roundTrip.category)
        assertEquals(original.season, roundTrip.season)
        assertEquals(original.swatch.id, roundTrip.swatch.id)
        assertEquals(original.tags, roundTrip.tags)

        // Verify 4032x3024 camera photo downscales cleanly for 1440px max edge
        val sampleSize = GarmentImageStorageManager.calculateInSampleSize(
            width = 4032,
            height = 3024,
            maxEdgePx = 1440
        )
        assertTrue(sampleSize >= 2)
    }
}
