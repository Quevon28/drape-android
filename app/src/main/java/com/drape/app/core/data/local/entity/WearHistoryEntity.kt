package com.drape.app.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.drape.app.core.model.OccasionVibe
import com.drape.app.core.model.WearHistoryEntry

@Entity(
    tableName = "wear_history",
    indices = [
        Index(value = ["wornAtEpochMs"]),
        Index(value = ["outfitId"])
    ]
)
data class WearHistoryEntity(
    @PrimaryKey
    val id: String,
    val outfitId: String?,
    val title: String,
    val occasion: String,
    val serializedGarmentIds: String,
    val weatherSummary: String,
    val wornAtEpochMs: Long,
    val dateLabel: String,
    val notes: String
)

fun WearHistoryEntity.toDomainModel(): WearHistoryEntry {
    val resolvedOccasion = OccasionVibe.entries.firstOrNull { it.name == occasion }
        ?: OccasionVibe.EVERYDAY_MINIMAL
    val parsedIds = if (serializedGarmentIds.isBlank()) {
        emptyList()
    } else {
        serializedGarmentIds.split("|").filter { it.isNotBlank() }
    }
    return WearHistoryEntry(
        id = id,
        outfitId = outfitId,
        title = title,
        occasion = resolvedOccasion,
        garmentIds = parsedIds,
        weatherSummary = weatherSummary,
        wornAtEpochMs = wornAtEpochMs,
        dateLabel = dateLabel,
        notes = notes
    )
}

fun WearHistoryEntry.toEntity(): WearHistoryEntity = WearHistoryEntity(
    id = id,
    outfitId = outfitId,
    title = title,
    occasion = occasion.name,
    serializedGarmentIds = garmentIds.joinToString("|"),
    weatherSummary = weatherSummary,
    wornAtEpochMs = wornAtEpochMs,
    dateLabel = dateLabel,
    notes = notes
)
