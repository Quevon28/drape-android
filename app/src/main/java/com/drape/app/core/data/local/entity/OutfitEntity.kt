package com.drape.app.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.drape.app.core.model.OccasionVibe
import com.drape.app.core.model.OutfitCombination

@Entity(tableName = "outfits")
data class OutfitEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val occasion: String,
    val serializedGarmentIds: String,
    val aiRationale: String?,
    val timesWorn: Int,
    val isScheduledForToday: Boolean,
    val isFavorite: Boolean = false,
    val createdAtEpochMs: Long = 1_729_000_000_000L,
    val lastWornEpochMs: Long? = null,
    val harmonyScore: Int? = 92,
    val notes: String = ""
)

fun OutfitEntity.toDomainModel(): OutfitCombination {
    val resolvedOccasion = OccasionVibe.entries.firstOrNull { it.name == occasion }
        ?: OccasionVibe.EVERYDAY_MINIMAL
    val parsedIds = if (serializedGarmentIds.isBlank()) {
        emptyList()
    } else {
        serializedGarmentIds.split("|").filter { it.isNotBlank() }
    }
    return OutfitCombination(
        id = id,
        title = title,
        occasion = resolvedOccasion,
        garmentIds = parsedIds,
        aiRationale = aiRationale,
        timesWorn = timesWorn,
        isScheduledForToday = isScheduledForToday,
        isFavorite = isFavorite,
        createdAtEpochMs = createdAtEpochMs,
        lastWornEpochMs = lastWornEpochMs,
        harmonyScore = harmonyScore,
        notes = notes
    )
}

fun OutfitCombination.toEntity(): OutfitEntity = OutfitEntity(
    id = id,
    title = title,
    occasion = occasion.name,
    serializedGarmentIds = garmentIds.joinToString("|"),
    aiRationale = aiRationale,
    timesWorn = timesWorn,
    isScheduledForToday = isScheduledForToday,
    isFavorite = isFavorite,
    createdAtEpochMs = createdAtEpochMs,
    lastWornEpochMs = lastWornEpochMs,
    harmonyScore = harmonyScore,
    notes = notes
)
