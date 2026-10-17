package com.drape.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.drape.app.core.data.local.entity.OutfitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutfitDao {
    @Query("SELECT * FROM outfits")
    fun observeAllOutfits(): Flow<List<OutfitEntity>>

    @Upsert
    suspend fun upsertOutfit(outfit: OutfitEntity)

    @Upsert
    suspend fun upsertOutfits(outfits: List<OutfitEntity>)

    @Query("DELETE FROM outfits WHERE id = :outfitId")
    suspend fun deleteOutfitById(outfitId: String)

    @Query("DELETE FROM outfits")
    suspend fun deleteAllOutfits()
}
