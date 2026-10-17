package com.drape.app.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.drape.app.core.designsystem.theme.GarmentSwatchPalette
import com.drape.app.core.model.GarmentCategory
import com.drape.app.core.model.GarmentItem
import com.drape.app.core.model.SeasonTag

/**
 * Room persistence entity for a wardrobe garment.
 * Stores local file URI for CameraX / Photo Picker garment images and full metadata.
 */
@Entity(
    tableName = "garments",
    indices = [
        Index(value = ["category"]),
        Index(value = ["season"]),
        Index(value = ["swatchId"]),
        Index(value = ["isFavorite"])
    ]
)
data class GarmentEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val brand: String,
    val category: String,
    val season: String,
    val swatchId: String,
    val wearCount: Int,
    val isFavorite: Boolean,
    val localImageUri: String?,
    val notes: String,
    val size: String,
    val fabric: String,
    val purchasePrice: Double?,
    val serializedTags: String,
    val createdAtEpochMs: Long,
    val lastWornEpochMs: Long?
)

fun GarmentEntity.toDomainModel(): GarmentItem {
    val resolvedCategory = GarmentCategory.entries.firstOrNull { it.name == category }
        ?: GarmentCategory.TOPS
    val resolvedSeason = SeasonTag.entries.firstOrNull { it.name == season }
        ?: SeasonTag.ALL_SEASONS
    val resolvedSwatch = GarmentSwatchPalette.AllSwatches.firstOrNull { it.id == swatchId }
        ?: GarmentSwatchPalette.ObsidianBlack
    val parsedTags = if (serializedTags.isBlank()) {
        emptyList()
    } else {
        serializedTags.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    }
    return GarmentItem(
        id = id,
        title = title,
        brand = brand,
        category = resolvedCategory,
        season = resolvedSeason,
        swatch = resolvedSwatch,
        wearCount = wearCount,
        isFavorite = isFavorite,
        localImageUri = localImageUri,
        notes = notes,
        size = size,
        fabric = fabric,
        purchasePrice = purchasePrice,
        tags = parsedTags,
        createdAtEpochMs = createdAtEpochMs,
        lastWornEpochMs = lastWornEpochMs
    )
}

fun GarmentItem.toEntity(): GarmentEntity = GarmentEntity(
    id = id,
    title = title,
    brand = brand,
    category = category.name,
    season = season.name,
    swatchId = swatch.id,
    wearCount = wearCount,
    isFavorite = isFavorite,
    localImageUri = localImageUri,
    notes = notes,
    size = size,
    fabric = fabric,
    purchasePrice = purchasePrice,
    serializedTags = tags.joinToString("|"),
    createdAtEpochMs = createdAtEpochMs,
    lastWornEpochMs = lastWornEpochMs
)
