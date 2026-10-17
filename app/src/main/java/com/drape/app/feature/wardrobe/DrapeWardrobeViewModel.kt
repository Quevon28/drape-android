package com.drape.app.feature.wardrobe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drape.app.core.data.repository.WardrobeRepository
import com.drape.app.core.model.DrapeWardrobeStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application-scoped Hilt ViewModel bridging Room + DataStore local persistence
 * (`WardrobeRepository`) with `DrapeWardrobeStateHolder`.
 */
@HiltViewModel
class DrapeWardrobeViewModel @Inject constructor(
    private val repository: WardrobeRepository
) : ViewModel() {

    val stateHolder = DrapeWardrobeStateHolder(
        onGarmentPersisted = { garment ->
            viewModelScope.launch { repository.upsertGarment(garment) }
        },
        onGarmentDeleted = { deletedGarment ->
            viewModelScope.launch { repository.deleteGarment(deletedGarment) }
        },
        onOutfitPersisted = { outfit ->
            viewModelScope.launch { repository.upsertOutfit(outfit) }
        },
        onOutfitDeleted = { outfitId ->
            viewModelScope.launch { repository.deleteOutfit(outfitId) }
        },
        onWearHistoryPersisted = { entry ->
            viewModelScope.launch { repository.upsertWearHistoryEntry(entry) }
        },
        onWearHistoryDeleted = { entryId ->
            viewModelScope.launch { repository.deleteWearHistoryEntry(entryId) }
        },
        onPreferencesPersisted = { prefs ->
            viewModelScope.launch { repository.saveUserPreferences(prefs) }
        }
    )

    init {
        viewModelScope.launch {
            repository.garmentsFlow.collectLatest { dbGarments ->
                if (dbGarments.isEmpty() && stateHolder.garments.isNotEmpty()) {
                    repository.seedStarterCapsuleIfEmpty(currentCount = 0)
                }
            }
        }
        viewModelScope.launch {
            repository.userPreferencesFlow.collectLatest { savedPrefs ->
                stateHolder.updatePreferences(savedPrefs)
            }
        }
    }
}
