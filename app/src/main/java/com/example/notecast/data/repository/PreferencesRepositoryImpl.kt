package com.example.notecast.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.notecast.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Định nghĩa DataStore (Tương tự như SharedPreferences nhưng dùng Flow/Coroutines)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    private val dataStore = context.dataStore

    // Keys
    private object PreferencesKeys {
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val SUMMARY_MODEL = stringPreferencesKey("summary_model")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding") // Key mới
    }

    // --- Logic Onboarding (Mới) ---
    override val hasSeenOnboarding: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_ONBOARDING] ?: false
        }

    override suspend fun setSeenOnboarding(seen: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_ONBOARDING] = seen
        }
    }

    // --- Logic Summary Model (Đã có) ---
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