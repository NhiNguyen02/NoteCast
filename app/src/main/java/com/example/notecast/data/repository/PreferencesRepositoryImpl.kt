package com.example.notecast.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.notecast.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    private object PreferencesKeys {
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val SUMMARY_MODEL = stringPreferencesKey("summary_model")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
    }

    override val hasSeenOnboarding: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_ONBOARDING] ?: false
        }

    override suspend fun setSeenOnboarding(seen: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_ONBOARDING] = seen
        }
    }

    override val isOnboardingCompleted: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] ?: false
        }

    override suspend fun setOnboardingCompleted(isCompleted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] = isCompleted
        }
    }

    override val summaryModel: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SUMMARY_MODEL] ?: "OFFLINE"
        }

    override suspend fun setSummaryModel(model: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUMMARY_MODEL] = model
        }
    }
}