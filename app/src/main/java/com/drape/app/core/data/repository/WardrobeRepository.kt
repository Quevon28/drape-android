package com.drape.app.core.data.repository

import com.drape.app.core.data.local.dao.GarmentDao
import com.drape.app.core.data.local.dao.OutfitDao
import com.drape.app.core.data.local.dao.WearHistoryDao
import com.drape.app.core.data.local.entity.toDomainModel
import com.drape.app.core.data.local.entity.toEntity
import com.drape.app.core.data.preferences.UserPreferencesDataSource
import com.drape.app.core.data.storage.GarmentImageStorageManager
import com.drape.app.core.model.GarmentItem
import com.drape.app.core.model.OutfitCombination
import com.drape.app.core.model.StarterCapsuleCatalog
import com.drape.app.core.model.UserStylePreferences
import com.drape.app.core.model.WearHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first repository coordinating Room local persistence (garments, saved outfits,
 * wear history) and Jetpack DataStore personal style preferences.
 */
@Singleton
class WardrobeRepository @Inject constructor(
    private val garmentDao: GarmentDao,
    private val outfitDao: OutfitDao,
    private val wearHistoryDao: WearHistoryDao,
    private val preferencesDataSource: UserPreferencesDataSource,
    private val imageStorageManager: GarmentImageStorageManager
) {
    val garmentsFlow: Flow<List<GarmentItem>> = garmentDao.observeAllGarments()
        .map { list -> list.map { it.toDomainModel() } }

    val outfitsFlow: Flow<List<OutfitCombination>> = outfitDao.observeAllOutfits()
        .map { list -> list.map { it.toDomainModel() } }

    val wearHistoryFlow: Flow<List<WearHistoryEntry>> = wearHistoryDao.observeWearHistory()
        .map { list -> list.map { it.toDomainModel() } }

    val userPreferencesFlow: Flow<UserStylePreferences> = preferencesDataSource.userPreferencesFlow

    suspend fun upsertGarment(garment: GarmentItem) {
        garmentDao.upsertGarment(garment.toEntity())
    }

    suspend fun deleteGarment(garment: GarmentItem) {
        garmentDao.deleteGarmentById(garment.id)
        imageStorageManager.deleteLocalImageIfOwned(garment.localImageUri)
    }

    suspend fun upsertOutfit(outfit: OutfitCombination) {
        outfitDao.upsertOutfit(outfit.toEntity())
    }

    suspend fun deleteOutfit(outfitId: String) {
        outfitDao.deleteOutfitById(outfitId)
    }

    suspend fun upsertWearHistoryEntry(entry: WearHistoryEntry) {
        wearHistoryDao.upsertWearHistoryEntry(entry.toEntity())
    }

    suspend fun deleteWearHistoryEntry(entryId: String) {
        wearHistoryDao.deleteWearHistoryById(entryId)
    }

    suspend fun saveUserPreferences(preferences: UserStylePreferences) {
        preferencesDataSource.savePreferences(preferences)
    }

    suspend fun seedStarterCapsuleIfEmpty(currentCount: Int) {
        if (currentCount == 0) {
            garmentDao.upsertGarments(StarterCapsuleCatalog.InitialGarments.map { it.toEntity() })
            outfitDao.upsertOutfits(StarterCapsuleCatalog.InitialOutfits.map { it.toEntity() })
            wearHistoryDao.upsertWearHistoryEntries(StarterCapsuleCatalog.InitialWearHistory.map { it.toEntity() })
        }
    }

    suspend fun clearAll() {
        garmentDao.deleteAllGarments()
        outfitDao.deleteAllOutfits()
        wearHistoryDao.deleteAllWearHistory()
    }
}
