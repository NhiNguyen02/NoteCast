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
    object Recording : Screen("recording")
    object NoteText : Screen("note_text/{noteId}"){
        fun createRoute(noteId: String) = "note_text/$noteId"

        const val arg = "noteId"
        val routeWithArgs = "note_text/{$arg}"
        val arguments = listOf(
            navArgument(arg) {
                type = NavType.StringType
                defaultValue = "0"
            }
        )
    }

    // New screen for viewing voice notes with text + audio tabs, loading data from DB by noteId
    object NoteAudio : Screen("note_audio/{noteId}") {
        private const val arg = "noteId"
        val routeWithArgs = "note_audio/{$arg}"
        val arguments = listOf(
            navArgument(arg) {
                type = NavType.StringType
            }
        )

        fun createRoute(noteId: String): String = "note_audio/$noteId"
    }
}