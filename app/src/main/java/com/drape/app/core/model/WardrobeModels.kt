package com.drape.app.core.model

import androidx.compose.runtime.Immutable
import com.drape.app.core.designsystem.theme.GarmentColorSwatch
import com.drape.app.core.designsystem.theme.GarmentSwatchPalette

/**
 * Core garment categories supported across Wardrobe, Outfit Studio, and AI Stylist.
 */
@Immutable
enum class GarmentCategory(val displayName: String) {
    TOPS("Tops"),
    BOTTOMS("Bottoms"),
    OUTERWEAR("Outerwear"),
    ONE_PIECE("One-Piece"),
    FOOTWEAR("Footwear"),
    ACCESSORIES("Accessories")
}

/**
 * Seasonal suitability tags for garments and daily weather matching.
 */
@Immutable
enum class SeasonTag(val displayName: String) {
    ALL_SEASONS("All Seasons"),
    SPRING_SUMMER("Spring / Summer"),
    AUTUMN_WINTER("Autumn / Winter"),
    RESORT_HUMID("Warm & Humid")
}

/**
 * Styling vibe / occasion presets used in Today and Style screens.
 */
@Immutable
enum class OccasionVibe(val displayName: String, val description: String) {
    EVERYDAY_MINIMAL("Everyday Minimal", "Clean silhouettes with effortless neutral layering"),
    OFFICE_TAILORED("Office Tailored", "Structured outerwear paired with crisp shirting and trousers"),
    WEEKEND_RELAXED("Weekend Relaxed", "Breathable knits and relaxed cuts for off-duty comfort"),
    EVENING_EDITORIAL("Evening Editorial", "High-contrast obsidian and champagne statement pieces"),
    TRAVEL_LAYERING("Travel Layering", "Versatile wrinkle-resistant pieces across climates")
}

/**
 * Personal style persona archetypes used to tailor AI outfit curation and rotation suggestions.
 */
@Immutable
enum class StylePersonaOption(val displayName: String, val subtitle: String) {
    ARCHITECTURAL_MINIMAL("Architectural Minimal", "Structured shoulders and clean tailoring"),
    MONOCHROME_LAYERING("Monochrome Layering", "Tonal depth across obsidian, chalk, and slate"),
    NATURAL_FIBERS("Natural Fibers", "Virgin wool, organic poplin, linen, and silk"),
    CAPSULE_ROTATION("Capsule Rotation", "High-versatility staples maximizing cost-per-wear"),
    HERITAGE_CRAFT("Heritage Craft", "Welted leather footwear, raw denim, and trench coats"),
    RELAXED_EDITORIAL("Relaxed Editorial", "Wide-leg drape paired with effortless knitwear")
}

/**
 * Persistent personal style preferences stored locally via DataStore Preferences.
 */
@Immutable
data class UserStylePreferences(
    val selectedPersonas: Set<StylePersonaOption> = setOf(
        StylePersonaOption.ARCHITECTURAL_MINIMAL,
        StylePersonaOption.CAPSULE_ROTATION
    ),
    val preferredSwatchIds: Set<String> = setOf("black", "white", "camel"),
    val defaultOccasion: OccasionVibe = OccasionVibe.EVERYDAY_MINIMAL,
    val defaultWeatherId: String = "crisp",
    val prioritizeUnwornInAi: Boolean = true,
    val avoidRepeatWithinDays: Int = 3
)

/**
 * Sorting options for the Digital Wardrobe grid.
 */
@Immutable
enum class WardrobeSortOption(val label: String) {
    NEWEST("Newest"),
    MOST_WORN("Most Worn"),
    LEAST_WORN("Least Worn"),
    ALPHABETICAL("A–Z"),
    LOWEST_CPW("Best Value")
}

/**
 * Multi-facet filter and sort criteria for querying the Digital Wardrobe.
 */
@Immutable
data class WardrobeFilterCriteria(
    val searchQuery: String = "",
    val selectedCategory: GarmentCategory? = null,
    val selectedSeason: SeasonTag? = null,
    val selectedSwatchId: String? = null,
    val favoritesOnly: Boolean = false,
    val withPhotoOnly: Boolean = false,
    val sortOption: WardrobeSortOption = WardrobeSortOption.NEWEST
) {
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() ||
            selectedCategory != null ||
            selectedSeason != null ||
            selectedSwatchId != null ||
            favoritesOnly ||
            withPhotoOnly
}

/**
 * Weather / climate context pill model for Today's outfit recommendations.
 */
@Immutable
data class WeatherConditionPreset(
    val id: String,
    val label: String,
    val tempSummary: String,
    val recommendedSeason: SeasonTag
)

val DefaultWeatherPresets: List<WeatherConditionPreset> = listOf(
    WeatherConditionPreset("crisp", "Crisp Breeze", "18°C · Clear", SeasonTag.AUTUMN_WINTER),
    WeatherConditionPreset("warm", "Warm Sun", "27°C · Humid", SeasonTag.RESORT_HUMID),
    WeatherConditionPreset("mild", "Mild Studio", "22°C · Partly Cloudy", SeasonTag.ALL_SEASONS),
    WeatherConditionPreset("rain", "Light Rain", "16°C · Overcast", SeasonTag.AUTUMN_WINTER)
)

/**
 * Immutable domain model representing a real wardrobe garment with full metadata and local photo URI.
 */
@Immutable
data class GarmentItem(
    val id: String,
    val title: String,
    val brand: String,
    val category: GarmentCategory,
    val season: SeasonTag,
    val swatch: GarmentColorSwatch,
    val wearCount: Int = 0,
    val isFavorite: Boolean = false,
    val localImageUri: String? = null,
    val notes: String = "",
    val size: String = "M",
    val fabric: String = "Natural Blend",
    val purchasePrice: Double? = null,
    val tags: List<String> = emptyList(),
    val createdAtEpochMs: Long = 1_729_000_000_000L,
    val lastWornEpochMs: Long? = null
) {
    val costPerWear: Double?
        get() = purchasePrice?.let { price ->
            if (wearCount > 0) price / wearCount.toDouble() else price
        }
}

/**
 * Pure, unit-testable multi-facet filtering and sorting engine for wardrobe garments.
 */
fun filterAndSortGarments(
    garments: List<GarmentItem>,
    criteria: WardrobeFilterCriteria
): List<GarmentItem> {
    val query = criteria.searchQuery.trim()
    val filtered = garments.filter { item ->
        val matchesQuery = query.isEmpty() ||
            item.title.contains(query, ignoreCase = true) ||
            item.brand.contains(query, ignoreCase = true) ||
            item.category.displayName.contains(query, ignoreCase = true) ||
            item.season.displayName.contains(query, ignoreCase = true) ||
            item.swatch.label.contains(query, ignoreCase = true) ||
            item.fabric.contains(query, ignoreCase = true) ||
            item.size.contains(query, ignoreCase = true) ||
            item.notes.contains(query, ignoreCase = true) ||
            item.tags.any { it.contains(query, ignoreCase = true) }

        val matchesCategory = criteria.selectedCategory == null || item.category == criteria.selectedCategory
        val matchesSeason = criteria.selectedSeason == null || item.season == criteria.selectedSeason
        val matchesSwatch = criteria.selectedSwatchId == null || item.swatch.id == criteria.selectedSwatchId
        val matchesFavorite = !criteria.favoritesOnly || item.isFavorite
        val matchesPhoto = !criteria.withPhotoOnly || !item.localImageUri.isNullOrBlank()

        matchesQuery && matchesCategory && matchesSeason && matchesSwatch && matchesFavorite && matchesPhoto
    }

    return when (criteria.sortOption) {
        WardrobeSortOption.NEWEST -> filtered.sortedByDescending { it.createdAtEpochMs }
        WardrobeSortOption.MOST_WORN -> filtered.sortedByDescending { it.wearCount }
        WardrobeSortOption.LEAST_WORN -> filtered.sortedBy { it.wearCount }
        WardrobeSortOption.ALPHABETICAL -> filtered.sortedBy { it.title.lowercase() }
        WardrobeSortOption.LOWEST_CPW -> filtered.sortedBy { it.costPerWear ?: Double.MAX_VALUE }
    }
}

/**
 * Immutable domain model representing a complete outfit built from actual wardrobe garments.
 */
@Immutable
data class OutfitCombination(
    val id: String,
    val title: String,
    val occasion: OccasionVibe,
    val garmentIds: List<String>,
    val aiRationale: String? = null,
    val timesWorn: Int = 0,
    val isScheduledForToday: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAtEpochMs: Long = 1_729_000_000_000L,
    val lastWornEpochMs: Long? = null,
    val harmonyScore: Int? = 92,
    val notes: String = ""
)

/**
 * Chronological wear history record linking a worn outfit (or garment selection) to a date and weather context.
 */
@Immutable
data class WearHistoryEntry(
    val id: String,
    val outfitId: String?,
    val title: String,
    val occasion: OccasionVibe,
    val garmentIds: List<String>,
    val weatherSummary: String,
    val wornAtEpochMs: Long,
    val dateLabel: String,
    val notes: String = ""
)

/**
 * Starter capsule wardrobe items, saved outfits, and wear history records.
 */
object StarterCapsuleCatalog {
    val InitialGarments: List<GarmentItem> = listOf(
        GarmentItem(
            id = "garment_trench",
            title = "Structured Wool Trench",
            brand = "Atelier Line",
            category = GarmentCategory.OUTERWEAR,
            season = SeasonTag.AUTUMN_WINTER,
            swatch = GarmentSwatchPalette.CamelTan,
            wearCount = 14,
            isFavorite = true,
            size = "M",
            fabric = "100% Virgin Wool",
            purchasePrice = 420.0,
            tags = listOf("tailored", "rain-ready", "outerwear"),
            notes = "Double-breasted storm flap; pairs well with monochrome knitwear.",
            createdAtEpochMs = 1_729_000_600_000L
        ),
        GarmentItem(
            id = "garment_poplin_shirt",
            title = "Relaxed Poplin Shirt",
            brand = "Studio Mercer",
            category = GarmentCategory.TOPS,
            season = SeasonTag.ALL_SEASONS,
            swatch = GarmentSwatchPalette.ChalkWhite,
            wearCount = 22,
            isFavorite = false,
            size = "M",
            fabric = "Organic Cotton Poplin",
            purchasePrice = 135.0,
            tags = listOf("crisp", "office", "layering"),
            notes = "Mother-of-pearl buttons, relaxed dropped shoulder.",
            createdAtEpochMs = 1_729_000_500_000L
        ),
        GarmentItem(
            id = "garment_pleated_trouser",
            title = "Pleated Wide-Leg Trouser",
            brand = "Kurobe Tailors",
            category = GarmentCategory.BOTTOMS,
            season = SeasonTag.ALL_SEASONS,
            swatch = GarmentSwatchPalette.ObsidianBlack,
            wearCount = 19,
            isFavorite = true,
            size = "32",
            fabric = "Tropical Wool Gabardine",
            purchasePrice = 260.0,
            tags = listOf("pleated", "wide-leg", "minimal"),
            notes = "High-waisted double pleat with full drape.",
            createdAtEpochMs = 1_729_000_400_000L
        ),
        GarmentItem(
            id = "garment_merino_crew",
            title = "Merino Ribbed Crewneck",
            brand = "Nordic Knit",
            category = GarmentCategory.TOPS,
            season = SeasonTag.AUTUMN_WINTER,
            swatch = GarmentSwatchPalette.SageOlive,
            wearCount = 9,
            isFavorite = false,
            size = "L",
            fabric = "Extra-Fine Merino Wool",
            purchasePrice = 165.0,
            tags = listOf("knitwear", "textured", "winter"),
            notes = "Saddle shoulder construction, breathable mid-weight gauge.",
            createdAtEpochMs = 1_729_000_300_000L
        ),
        GarmentItem(
            id = "garment_leather_derby",
            title = "Minimal Leather Derby",
            brand = "Solovair Craft",
            category = GarmentCategory.FOOTWEAR,
            season = SeasonTag.ALL_SEASONS,
            swatch = GarmentSwatchPalette.ObsidianBlack,
            wearCount = 27,
            isFavorite = true,
            size = "42",
            fabric = "Full-Grain Calfskin",
            purchasePrice = 290.0,
            tags = listOf("footwear", "welted", "formal-casual"),
            notes = "Goodyear welted lug sole, conditioned monthly.",
            createdAtEpochMs = 1_729_000_200_000L
        ),
        GarmentItem(
            id = "garment_raw_denim",
            title = "Selvedge Straight Denim",
            brand = "Okayama Mill",
            category = GarmentCategory.BOTTOMS,
            season = SeasonTag.ALL_SEASONS,
            swatch = GarmentSwatchPalette.NavyInk,
            wearCount = 16,
            isFavorite = false,
            size = "32",
            fabric = "13.5oz Japanese Selvedge Cotton",
            purchasePrice = 210.0,
            tags = listOf("denim", "casual", "indigo"),
            notes = "Rope-dyed indigo, straight leg cut.",
            createdAtEpochMs = 1_729_000_100_000L
        )
    )

    val InitialOutfits: List<OutfitCombination> = listOf(
        OutfitCombination(
            id = "outfit_editorial_commute",
            title = "Architectural Commute",
            occasion = OccasionVibe.OFFICE_TAILORED,
            garmentIds = listOf(
                "garment_trench",
                "garment_poplin_shirt",
                "garment_pleated_trouser",
                "garment_leather_derby"
            ),
            aiRationale = "Warm camel outerwear balances the high-contrast chalk shirt and obsidian pleated trouser.",
            timesWorn = 4,
            isScheduledForToday = true,
            isFavorite = true,
            createdAtEpochMs = 1_729_000_700_000L,
            lastWornEpochMs = 1_729_000_900_000L,
            harmonyScore = 95,
            notes = "Go-to studio and client presentation uniform."
        ),
        OutfitCombination(
            id = "outfit_weekend_knit",
            title = "Off-Duty Merino & Denim",
            occasion = OccasionVibe.WEEKEND_RELAXED,
            garmentIds = listOf(
                "garment_merino_crew",
                "garment_raw_denim",
                "garment_leather_derby"
            ),
            aiRationale = "Earthy sage olive merino knit softens raw indigo selvedge denim with grounded calfskin footwear.",
            timesWorn = 2,
            isScheduledForToday = false,
            isFavorite = false,
            createdAtEpochMs = 1_729_000_650_000L,
            lastWornEpochMs = 1_729_000_800_000L,
            harmonyScore = 91,
            notes = "Comfortable gallery and coffee walk combination."
        )
    )

    val InitialWearHistory: List<WearHistoryEntry> = listOf(
        WearHistoryEntry(
            id = "wear_log_1",
            outfitId = "outfit_editorial_commute",
            title = "Architectural Commute",
            occasion = OccasionVibe.OFFICE_TAILORED,
            garmentIds = listOf(
                "garment_trench",
                "garment_poplin_shirt",
                "garment_pleated_trouser",
                "garment_leather_derby"
            ),
            weatherSummary = "Crisp Breeze · 18°C",
            wornAtEpochMs = 1_729_000_900_000L,
            dateLabel = "Yesterday",
            notes = "Wore to morning design review."
        ),
        WearHistoryEntry(
            id = "wear_log_2",
            outfitId = "outfit_weekend_knit",
            title = "Off-Duty Merino & Denim",
            occasion = OccasionVibe.WEEKEND_RELAXED,
            garmentIds = listOf(
                "garment_merino_crew",
                "garment_raw_denim",
                "garment_leather_derby"
            ),
            weatherSummary = "Mild Studio · 22°C",
            wornAtEpochMs = 1_729_000_800_000L,
            dateLabel = "3 days ago",
            notes = "Weekend bookstore visit."
        )
    )
}
