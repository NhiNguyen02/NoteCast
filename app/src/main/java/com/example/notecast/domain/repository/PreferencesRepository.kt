package com.example.notecast.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    // Ví dụ: Lưu trữ trạng thái Onboarding
    val isOnboardingCompleted: Flow<Boolean>
    suspend fun setOnboardingCompleted(isCompleted: Boolean)

    // Ví dụ: Lấy tham số cấu hình tóm tắt
    val summaryModel: Flow<String>
    suspend fun setSummaryModel(model: String) // Thêm hàm set cho summary model

    // Thêm các hàm Onboarding mới (has/set)
    val hasSeenOnboarding: Flow<Boolean>
    suspend fun setSeenOnboarding(seen: Boolean)
}