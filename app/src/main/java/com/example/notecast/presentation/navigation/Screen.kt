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
    object NoteEdit : Screen("note_edit/{noteId}?initialContent={initialContent}&audioPath={audioPath}&durationMs={durationMs}&sampleRate={sampleRate}&channels={channels}"){
        fun createRoute(noteId: String) = "note_edit/$noteId"
        fun createRouteWithTranscript(
            noteId: String,
            initialContent: String,
            audioPath: String?,
            durationMs: Long,
            sampleRate: Int,
            channels: Int,
        ): String {
            val encodedContent = java.net.URLEncoder.encode(initialContent, "UTF-8")
            val encodedPath = java.net.URLEncoder.encode(audioPath ?: "", "UTF-8")
            return "note_edit/$noteId?initialContent=$encodedContent&audioPath=$encodedPath&durationMs=$durationMs&sampleRate=$sampleRate&channels=$channels"
        }

        // Định nghĩa tham số
        const val arg = "noteId"
        const val initialContentArg = "initialContent"
        const val audioPathArg = "audioPath"
        const val durationMsArg = "durationMs"
        const val sampleRateArg = "sampleRate"
        const val channelsArg = "channels"

        val routeWithArgs = "note_edit/{$arg}?$initialContentArg={$initialContentArg}&$audioPathArg={$audioPathArg}&$durationMsArg={$durationMsArg}&$sampleRateArg={$sampleRateArg}&$channelsArg={$channelsArg}"
        val arguments = listOf(
            navArgument(arg) {
                type = NavType.StringType
                defaultValue = "0" // Mặc định là 0 (Tạo mới)
            },
            navArgument(initialContentArg) {
                type = NavType.StringType
                defaultValue = ""
                nullable = true
            },
            navArgument(audioPathArg) {
                type = NavType.StringType
                defaultValue = ""
                nullable = true
            },
            navArgument(durationMsArg) {
                type = NavType.LongType
                defaultValue = 0L
            },
            navArgument(sampleRateArg) {
                type = NavType.IntType
                defaultValue = 0
            },
            navArgument(channelsArg) {
                type = NavType.IntType
                defaultValue = 0
            },
        )
    }
}