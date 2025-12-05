package com.example.notecast.presentation.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * App routes
 *
 * Add new routes here as objects with .route strings.
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Main : Screen("main")
    object Home : Screen("home")
    object Folders : Screen("folders")
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")
    // New: recording route used when user selects "Ghi âm giọng nói"
    object Recording : Screen("recording")
    // New: Tokenizer debug screen route
    object TokenizerDebug : Screen("tokenizer_debug")
    object NoteEdit : Screen("note_edit/{noteId}"){
        fun createRoute(noteId: String) = "note_edit/$noteId"

        // Định nghĩa tham số
        const val arg = "noteId"
        val routeWithArgs = "note_edit/{$arg}"
        val arguments = listOf(
            navArgument(arg) {
                type = NavType.StringType
                defaultValue = "0" // Mặc định là 0 (Tạo mới)
            }
        )
    }
}