package com.example.notecast.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val isOnboardingCompleted: Flow<Boolean>
    suspend fun setOnboardingCompleted(isCompleted: Boolean)

    val summaryModel: Flow<String>
    suspend fun setSummaryModel(model: String)

    val hasSeenOnboarding: Flow<Boolean>
    suspend fun setSeenOnboarding(seen: Boolean)
}