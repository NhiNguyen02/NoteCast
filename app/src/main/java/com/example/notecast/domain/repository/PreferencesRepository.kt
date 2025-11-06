package com.example.notecast.domain.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * Class đơn giản để quản lý SharedPreferences.
 * (Trong một dự án Hilt, bạn nên inject cái này thay vì tạo thủ công)
 */
class PreferencesRepository(context: Context) {

    private val prefsName = "notecast_prefs"
    private val keySeenOnboarding = "seen_onboarding"
    private val preferences: SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    /**
     * Kiểm tra xem người dùng đã xem Onboarding chưa.
     * Mặc định là false (chưa xem).
     */
    fun hasSeenOnboarding(): Boolean {
        return preferences.getBoolean(keySeenOnboarding, false)
    }

    /**
     * Đánh dấu là người dùng đã xem Onboarding.
     */
    fun setSeenOnboarding() {
        preferences.edit().putBoolean(keySeenOnboarding, true).apply()
    }
}