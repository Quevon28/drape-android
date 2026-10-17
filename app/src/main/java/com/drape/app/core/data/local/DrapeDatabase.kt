package com.drape.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.drape.app.core.data.local.dao.GarmentDao
import com.drape.app.core.data.local.dao.OutfitDao
import com.drape.app.core.data.local.dao.WearHistoryDao
import com.drape.app.core.data.local.entity.GarmentEntity
import com.drape.app.core.data.local.entity.OutfitEntity
import com.drape.app.core.data.local.entity.WearHistoryEntity

@Database(
    entities = [
        GarmentEntity::class,
        OutfitEntity::class,
        WearHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DrapeDatabase : RoomDatabase() {
    abstract fun garmentDao(): GarmentDao
    abstract fun outfitDao(): OutfitDao
    abstract fun wearHistoryDao(): WearHistoryDao

    companion object {
        const val DATABASE_NAME = "drape_wardrobe.db"
    }
}
