package com.drape.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.drape.app.core.data.local.DrapeDatabase
import com.drape.app.core.data.local.entity.toDomainModel
import com.drape.app.core.data.local.entity.toEntity
import com.drape.app.core.model.StarterCapsuleCatalog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DrapeInstrumentedDatabaseAndSecurityTest {

    private lateinit var database: DrapeDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            DrapeDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun roomDatabase_persistsAndQueriesGarmentsOutfitsAndWearHistory() = runBlocking {
        val garmentDao = database.garmentDao()
        val outfitDao = database.outfitDao()
        val wearHistoryDao = database.wearHistoryDao()

        garmentDao.upsertGarments(StarterCapsuleCatalog.InitialGarments.map { it.toEntity() })
        outfitDao.upsertOutfits(StarterCapsuleCatalog.InitialOutfits.map { it.toEntity() })
        wearHistoryDao.upsertWearHistoryEntries(StarterCapsuleCatalog.InitialWearHistory.map { it.toEntity() })

        val loadedGarments = garmentDao.observeAllGarments().first().map { it.toDomainModel() }
        val loadedOutfits = outfitDao.observeAllOutfits().first().map { it.toDomainModel() }
        val loadedHistory = wearHistoryDao.observeWearHistory().first().map { it.toDomainModel() }

        assertEquals(StarterCapsuleCatalog.InitialGarments.size, loadedGarments.size)
        assertEquals(StarterCapsuleCatalog.InitialOutfits.size, loadedOutfits.size)
        assertEquals(StarterCapsuleCatalog.InitialWearHistory.size, loadedHistory.size)
        assertTrue(loadedOutfits.any { it.isScheduledForToday })
    }
}
