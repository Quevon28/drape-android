package com.drape.app.core.di

import android.content.Context
import androidx.room.Room
import com.drape.app.core.data.local.DrapeDatabase
import com.drape.app.core.data.local.dao.GarmentDao
import com.drape.app.core.data.local.dao.OutfitDao
import com.drape.app.core.data.local.dao.WearHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDrapeDatabase(
        @ApplicationContext context: Context
    ): DrapeDatabase = Room.databaseBuilder(
        context,
        DrapeDatabase::class.java,
        DrapeDatabase.DATABASE_NAME
    ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideGarmentDao(database: DrapeDatabase): GarmentDao = database.garmentDao()

    @Provides
    fun provideOutfitDao(database: DrapeDatabase): OutfitDao = database.outfitDao()

    @Provides
    fun provideWearHistoryDao(database: DrapeDatabase): WearHistoryDao = database.wearHistoryDao()
}
