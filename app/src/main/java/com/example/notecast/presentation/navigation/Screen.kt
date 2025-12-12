package com.example.notecast.navigation

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
    object NoteEdit : Screen("note_edit/{noteId}"){
        fun createRoute(noteId: String) = "note_edit/$noteId"

        const val arg = "noteId"
        val routeWithArgs = "note_edit/{$arg}"
        val arguments = listOf(
            navArgument(arg) {
                type = NavType.StringType
                defaultValue = "0" // Mặc định là 0 (Tạo mới)
            }
        )
    }

    object NoteDetail : Screen("note_detail") {
        const val noteIdArg = "noteId"
        const val titleArg = "title"
        const val dateArg = "date"
        const val contentArg = "content"
        const val chunksArg = "chunksJson"

        fun createRoute(
            noteId: String?,
            title: String,
            date: String,
            content: String,
            chunksJson: String? = null,
        ): String {
            val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
            val encodedDate = java.net.URLEncoder.encode(date, "UTF-8")
            val encodedContent = java.net.URLEncoder.encode(content, "UTF-8")
            val encodedChunks = java.net.URLEncoder.encode(chunksJson ?: "", "UTF-8")
            val encodedNoteId = java.net.URLEncoder.encode(noteId ?: "", "UTF-8")
            return "note_detail?noteId=${encodedNoteId}&title=${encodedTitle}&date=${encodedDate}&content=${encodedContent}&chunksJson=${encodedChunks}"
        }

        val routeWithArgs =
            "note_detail?${noteIdArg}={${noteIdArg}}&${titleArg}={${titleArg}}&${dateArg}={${dateArg}}&${contentArg}={${contentArg}}&${chunksArg}={${chunksArg}}"

        val arguments = listOf(
            navArgument(noteIdArg) {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument(titleArg) {
                type = NavType.StringType
                defaultValue = "Ghi chú ghi âm"
            },
            navArgument(dateArg) {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument(contentArg) {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument(chunksArg) {
                type = NavType.StringType
                defaultValue = ""
            },
        )
    }
}