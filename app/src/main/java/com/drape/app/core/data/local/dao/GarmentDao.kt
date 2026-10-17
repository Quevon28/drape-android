package com.drape.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.drape.app.core.data.local.entity.GarmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GarmentDao {
    @Query("SELECT * FROM garments ORDER BY createdAtEpochMs DESC")
    fun observeAllGarments(): Flow<List<GarmentEntity>>

    @Query("SELECT * FROM garments WHERE id = :garmentId LIMIT 1")
    suspend fun getGarmentById(garmentId: String): GarmentEntity?

    @Upsert
    suspend fun upsertGarment(garment: GarmentEntity)

    @Upsert
    suspend fun upsertGarments(garments: List<GarmentEntity>)

    @Query("DELETE FROM garments WHERE id = :garmentId")
    suspend fun deleteGarmentById(garmentId: String)

    @Query("DELETE FROM garments")
    suspend fun deleteAllGarments()
}
