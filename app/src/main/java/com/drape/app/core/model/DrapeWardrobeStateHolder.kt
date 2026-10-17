package com.drape.app.core.model

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.drape.app.core.designsystem.theme.GarmentSwatchPalette

/**
 * State holder managing wardrobe garments, saved outfits, outfit favorites, chronological
 * wear history, personal style preferences, weather context, and local persistence callbacks.
 */
@Stable
class DrapeWardrobeStateHolder(
    initialGarments: List<GarmentItem> = StarterCapsuleCatalog.InitialGarments,
    initialOutfits: List<OutfitCombination> = StarterCapsuleCatalog.InitialOutfits,
    initialWearHistory: List<WearHistoryEntry> = StarterCapsuleCatalog.InitialWearHistory,
    initialPreferences: UserStylePreferences = UserStylePreferences(),
    initialWeather: WeatherConditionPreset = DefaultWeatherPresets.first(),
    private val onGarmentPersisted: ((GarmentItem) -> Unit)? = null,
    private val onGarmentDeleted: ((GarmentItem) -> Unit)? = null,
    private val onOutfitPersisted: ((OutfitCombination) -> Unit)? = null,
    private val onOutfitDeleted: ((String) -> Unit)? = null,
    private val onWearHistoryPersisted: ((WearHistoryEntry) -> Unit)? = null,
    private val onWearHistoryDeleted: ((String) -> Unit)? = null,
    private val onPreferencesPersisted: ((UserStylePreferences) -> Unit)? = null
) {
    private val _garments = mutableStateListOf<GarmentItem>().apply {
        addAll(initialGarments)
    }
    val garments: List<GarmentItem> get() = _garments

    private val _outfits = mutableStateListOf<OutfitCombination>().apply {
        addAll(initialOutfits)
    }
    val outfits: List<OutfitCombination> get() = _outfits

    private val _wearHistory = mutableStateListOf<WearHistoryEntry>().apply {
        addAll(initialWearHistory)
    }
    val wearHistory: List<WearHistoryEntry> get() = _wearHistory

    var stylePreferences by mutableStateOf(initialPreferences)
        private set

    var selectedWeather by mutableStateOf(initialWeather)
        private set

    val isWardrobeEmpty: Boolean
        get() = _garments.isEmpty()

    val isLookbookEmpty: Boolean
        get() = _outfits.isEmpty()

    val scheduledTodayOutfit: OutfitCombination?
        get() = _outfits.firstOrNull { it.isScheduledForToday }

    val favoriteGarmentsCount: Int
        get() = _garments.count { it.isFavorite }

    val favoriteOutfitsCount: Int
        get() = _outfits.count { it.isFavorite }

    val totalGarmentWears: Int
        get() = _garments.sumOf { it.wearCount }

    fun selectWeather(preset: WeatherConditionPreset) {
        selectedWeather = preset
        updatePreferences(stylePreferences.copy(defaultWeatherId = preset.id))
    }

    fun getGarmentById(garmentId: String): GarmentItem? =
        _garments.firstOrNull { it.id == garmentId }

    fun addGarment(
        title: String,
        brand: String,
        category: GarmentCategory,
        season: SeasonTag,
        swatchId: String,
        id: String = "garment_${System.currentTimeMillis()}",
        localImageUri: String? = null,
        notes: String = "",
        size: String = "M",
        fabric: String = "Natural Blend",
        purchasePrice: Double? = null,
        tags: List<String> = emptyList()
    ): GarmentItem {
        val swatch = GarmentSwatchPalette.AllSwatches
            .firstOrNull { it.id == swatchId }
            ?: GarmentSwatchPalette.ObsidianBlack
        val newItem = GarmentItem(
            id = id,
            title = title.trim(),
            brand = brand.trim().ifBlank { "Personal Archive" },
            category = category,
            season = season,
            swatch = swatch,
            wearCount = 0,
            isFavorite = false,
            localImageUri = localImageUri,
            notes = notes.trim(),
            size = size.trim().ifBlank { "M" },
            fabric = fabric.trim().ifBlank { "Natural Blend" },
            purchasePrice = purchasePrice,
            tags = tags.map { it.trim() }.filter { it.isNotEmpty() },
            createdAtEpochMs = System.currentTimeMillis()
        )
        _garments.add(0, newItem)
        onGarmentPersisted?.invoke(newItem)
        return newItem
    }

    fun updateGarment(updated: GarmentItem): Boolean {
        val idx = _garments.indexOfFirst { it.id == updated.id }
        if (idx < 0) return false
        val normalized = updated.copy(
            title = updated.title.trim().ifBlank { _garments[idx].title },
            brand = updated.brand.trim().ifBlank { "Personal Archive" },
            size = updated.size.trim().ifBlank { "M" },
            fabric = updated.fabric.trim().ifBlank { "Natural Blend" },
            notes = updated.notes.trim(),
            tags = updated.tags.map { it.trim() }.filter { it.isNotEmpty() }
        )
        _garments[idx] = normalized
        onGarmentPersisted?.invoke(normalized)
        return true
    }

    fun updateGarmentPhoto(garmentId: String, localImageUri: String?): Boolean {
        val existing = getGarmentById(garmentId) ?: return false
        return updateGarment(existing.copy(localImageUri = localImageUri))
    }

    fun removeGarment(garmentId: String) {
        val existing = getGarmentById(garmentId)
        _garments.removeAll { it.id == garmentId }
        if (existing != null) {
            onGarmentDeleted?.invoke(existing)
        }
        for (i in _outfits.indices.reversed()) {
            val updatedIds = _outfits[i].garmentIds.filter { it != garmentId }
            if (updatedIds.isEmpty()) {
                val removedOutfitId = _outfits[i].id
                _outfits.removeAt(i)
                onOutfitDeleted?.invoke(removedOutfitId)
            } else if (updatedIds.size != _outfits[i].garmentIds.size) {
                val modified = _outfits[i].copy(garmentIds = updatedIds)
                _outfits[i] = modified
                onOutfitPersisted?.invoke(modified)
            }
        }
    }

    fun toggleFavorite(garmentId: String) {
        val idx = _garments.indexOfFirst { it.id == garmentId }
        if (idx >= 0) {
            val item = _garments[idx]
            val updated = item.copy(isFavorite = !item.isFavorite)
            _garments[idx] = updated
            onGarmentPersisted?.invoke(updated)
        }
    }

    fun incrementGarmentWear(garmentId: String) {
        val idx = _garments.indexOfFirst { it.id == garmentId }
        if (idx >= 0) {
            val item = _garments[idx]
            val updated = item.copy(
                wearCount = item.wearCount + 1,
                lastWornEpochMs = System.currentTimeMillis()
            )
            _garments[idx] = updated
            onGarmentPersisted?.invoke(updated)
        }
    }

    fun decrementGarmentWear(garmentId: String) {
        val idx = _garments.indexOfFirst { it.id == garmentId }
        if (idx >= 0) {
            val item = _garments[idx]
            val updated = item.copy(wearCount = (item.wearCount - 1).coerceAtLeast(0))
            _garments[idx] = updated
            onGarmentPersisted?.invoke(updated)
        }
    }

    fun createOutfit(
        title: String,
        occasion: OccasionVibe,
        garmentIds: List<String>,
        rationale: String?,
        scheduleToday: Boolean,
        id: String = "outfit_${System.currentTimeMillis()}",
        isFavorite: Boolean = false,
        harmonyScore: Int? = 92,
        notes: String = ""
    ): OutfitCombination? {
        if (garmentIds.isEmpty()) return null
        val now = System.currentTimeMillis()
        if (scheduleToday) {
            for (i in _outfits.indices) {
                if (_outfits[i].isScheduledForToday) {
                    val unscheduled = _outfits[i].copy(isScheduledForToday = false)
                    _outfits[i] = unscheduled
                    onOutfitPersisted?.invoke(unscheduled)
                }
            }
        }
        val newOutfit = OutfitCombination(
            id = id,
            title = title.trim().ifBlank { "${occasion.displayName} Look #${_outfits.size + 1}" },
            occasion = occasion,
            garmentIds = garmentIds,
            aiRationale = rationale,
            timesWorn = if (scheduleToday) 1 else 0,
            isScheduledForToday = scheduleToday,
            isFavorite = isFavorite,
            createdAtEpochMs = now,
            lastWornEpochMs = if (scheduleToday) now else null,
            harmonyScore = harmonyScore,
            notes = notes.trim()
        )
        _outfits.add(0, newOutfit)
        onOutfitPersisted?.invoke(newOutfit)
        return newOutfit
    }

    fun toggleOutfitFavorite(outfitId: String) {
        val idx = _outfits.indexOfFirst { it.id == outfitId }
        if (idx >= 0) {
            val target = _outfits[idx]
            val updated = target.copy(isFavorite = !target.isFavorite)
            _outfits[idx] = updated
            onOutfitPersisted?.invoke(updated)
        }
    }

    fun updateOutfit(updated: OutfitCombination): Boolean {
        val idx = _outfits.indexOfFirst { it.id == updated.id }
        if (idx < 0) return false
        _outfits[idx] = updated
        onOutfitPersisted?.invoke(updated)
        return true
    }

    fun scheduleOutfitForToday(outfitId: String) {
        for (i in _outfits.indices) {
            val shouldSchedule = _outfits[i].id == outfitId
            if (_outfits[i].isScheduledForToday != shouldSchedule) {
                val updated = _outfits[i].copy(isScheduledForToday = shouldSchedule)
                _outfits[i] = updated
                onOutfitPersisted?.invoke(updated)
            }
        }
    }

    fun clearTodayOutfit() {
        for (i in _outfits.indices) {
            if (_outfits[i].isScheduledForToday) {
                val updated = _outfits[i].copy(isScheduledForToday = false)
                _outfits[i] = updated
                onOutfitPersisted?.invoke(updated)
            }
        }
    }

    fun logOutfitWear(
        outfitId: String,
        notes: String = "",
        dateLabel: String = "Today"
    ): WearHistoryEntry? {
        val idx = _outfits.indexOfFirst { it.id == outfitId }
        if (idx < 0) return null
        val now = System.currentTimeMillis()
        val target = _outfits[idx]
        val updatedOutfit = target.copy(
            timesWorn = target.timesWorn + 1,
            lastWornEpochMs = now
        )
        _outfits[idx] = updatedOutfit
        onOutfitPersisted?.invoke(updatedOutfit)

        target.garmentIds.forEach { gid ->
            incrementGarmentWear(gid)
        }

        val entry = WearHistoryEntry(
            id = "wear_${now}_${_wearHistory.size + 1}",
            outfitId = target.id,
            title = target.title,
            occasion = target.occasion,
            garmentIds = target.garmentIds,
            weatherSummary = "${selectedWeather.label} · ${selectedWeather.tempSummary}",
            wornAtEpochMs = now,
            dateLabel = dateLabel,
            notes = notes.trim()
        )
        _wearHistory.add(0, entry)
        onWearHistoryPersisted?.invoke(entry)
        return entry
    }

    fun deleteWearHistoryEntry(entryId: String) {
        _wearHistory.removeAll { it.id == entryId }
        onWearHistoryDeleted?.invoke(entryId)
    }

    fun deleteOutfit(outfitId: String) {
        _outfits.removeAll { it.id == outfitId }
        onOutfitDeleted?.invoke(outfitId)
    }

    // Personal Style Preferences mutations
    fun updatePreferences(newPrefs: UserStylePreferences) {
        stylePreferences = newPrefs
        onPreferencesPersisted?.invoke(newPrefs)
    }

    fun toggleStylePersona(persona: StylePersonaOption) {
        val current = stylePreferences.selectedPersonas.toMutableSet()
        if (persona in current) {
            if (current.size > 1) current.remove(persona)
        } else {
            current.add(persona)
        }
        updatePreferences(stylePreferences.copy(selectedPersonas = current))
    }

    fun togglePreferredSwatch(swatchId: String) {
        val current = stylePreferences.preferredSwatchIds.toMutableSet()
        if (swatchId in current) {
            current.remove(swatchId)
        } else {
            current.add(swatchId)
        }
        updatePreferences(stylePreferences.copy(preferredSwatchIds = current))
    }

    fun setDefaultOccasion(occasion: OccasionVibe) {
        updatePreferences(stylePreferences.copy(defaultOccasion = occasion))
    }

    fun setPrioritizeUnwornInAi(enabled: Boolean) {
        updatePreferences(stylePreferences.copy(prioritizeUnwornInAi = enabled))
    }

    fun loadStarterCapsule() {
        _garments.clear()
        _garments.addAll(StarterCapsuleCatalog.InitialGarments)
        _outfits.clear()
        _outfits.addAll(StarterCapsuleCatalog.InitialOutfits)
        _wearHistory.clear()
        _wearHistory.addAll(StarterCapsuleCatalog.InitialWearHistory)
    }

    fun clearAll() {
        _garments.clear()
        _outfits.clear()
        _wearHistory.clear()
    }
}
