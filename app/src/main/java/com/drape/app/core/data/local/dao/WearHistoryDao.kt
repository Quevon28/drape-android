package com.drape.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.drape.app.core.data.local.entity.WearHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WearHistoryDao {
    @Query("SELECT * FROM wear_history ORDER BY wornAtEpochMs DESC")
    fun observeWearHistory(): Flow<List<WearHistoryEntity>>

    @Upsert
    suspend fun upsertWearHistoryEntry(entry: WearHistoryEntity)

    @Upsert
    suspend fun upsertWearHistoryEntries(entries: List<WearHistoryEntity>)

    @Query("DELETE FROM wear_history WHERE id = :entryId")
    suspend fun deleteWearHistoryById(entryId: String)

    @Query("DELETE FROM wear_history")
    suspend fun deleteAllWearHistory()
}
