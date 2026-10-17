package com.drape.app.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.drape.app.core.model.OccasionVibe
import com.drape.app.core.model.StylePersonaOption
import com.drape.app.core.model.UserStylePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.drapePreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "drape_style_preferences"
)

/**
 * Local DataStore persistence for the user's personal style personas, preferred color swatches,
 * default occasion/weather, and AI unworn rotation priority.
 */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val SELECTED_PERSONAS = stringSetPreferencesKey("selected_personas")
        val PREFERRED_SWATCH_IDS = stringSetPreferencesKey("preferred_swatch_ids")
        val DEFAULT_OCCASION = stringPreferencesKey("default_occasion")
        val DEFAULT_WEATHER_ID = stringPreferencesKey("default_weather_id")
        val PRIORITIZE_UNWORN_IN_AI = booleanPreferencesKey("prioritize_unworn_in_ai")
        val AVOID_REPEAT_WITHIN_DAYS = intPreferencesKey("avoid_repeat_within_days")
    }

    val userPreferencesFlow: Flow<UserStylePreferences> = context.drapePreferencesDataStore.data
        .map { prefs ->
            val rawPersonas = prefs[Keys.SELECTED_PERSONAS]
            val personas = if (rawPersonas == null) {
                setOf(
                    StylePersonaOption.ARCHITECTURAL_MINIMAL,
                    StylePersonaOption.CAPSULE_ROTATION
                )
            } else {
                rawPersonas.mapNotNull { name ->
                    StylePersonaOption.entries.firstOrNull { it.name == name }
                }.toSet()
            }

            val swatches = prefs[Keys.PREFERRED_SWATCH_IDS] ?: setOf("black", "white", "camel")
            val occasion = prefs[Keys.DEFAULT_OCCASION]?.let { name ->
                OccasionVibe.entries.firstOrNull { it.name == name }
            } ?: OccasionVibe.EVERYDAY_MINIMAL

            UserStylePreferences(
                selectedPersonas = personas,
                preferredSwatchIds = swatches,
                defaultOccasion = occasion,
                defaultWeatherId = prefs[Keys.DEFAULT_WEATHER_ID] ?: "crisp",
                prioritizeUnwornInAi = prefs[Keys.PRIORITIZE_UNWORN_IN_AI] ?: true,
                avoidRepeatWithinDays = prefs[Keys.AVOID_REPEAT_WITHIN_DAYS] ?: 3
            )
        }

    suspend fun savePreferences(preferences: UserStylePreferences) {
        context.drapePreferencesDataStore.edit { prefs ->
            prefs[Keys.SELECTED_PERSONAS] = preferences.selectedPersonas.map { it.name }.toSet()
            prefs[Keys.PREFERRED_SWATCH_IDS] = preferences.preferredSwatchIds
            prefs[Keys.DEFAULT_OCCASION] = preferences.defaultOccasion.name
            prefs[Keys.DEFAULT_WEATHER_ID] = preferences.defaultWeatherId
            prefs[Keys.PRIORITIZE_UNWORN_IN_AI] = preferences.prioritizeUnwornInAi
            prefs[Keys.AVOID_REPEAT_WITHIN_DAYS] = preferences.avoidRepeatWithinDays
        }
    }
}
