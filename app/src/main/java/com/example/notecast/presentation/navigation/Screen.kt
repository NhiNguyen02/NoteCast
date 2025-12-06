package com.example.notecast.presentation.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Định nghĩa các tuyến đường (route) cho NavGraph gốc.
 */
//sealed class Screen(val route: String) {
//    data object Splash : Screen("splash")
//    data object Onboarding : Screen("onboarding")
//
//    data object Main : Screen("main")
//
//    // Các route cho Drawer (bên trong Main)
//    data object Home : Screen("home") // HomeScreen (có ghi chú)
//    data object Folders : Screen("folders")
//    data object Notifications : Screen("notifications")
//    data object Settings : Screen("settings")
//}


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
    object TokenizerDebug : Screen("tokenizer_debug")

    object NoteEdit : Screen("note_edit/{noteId}?initialContent={initialContent}&audioPath={audioPath}&durationMs={durationMs}&sampleRate={sampleRate}&channels={channels}") {
        fun createRoute(noteId: String) = "note_edit/$noteId"

        fun createRouteWithTranscript(
            title: String,
            initialContent: String,
            audioPath: String?,
            durationMs: Long,
            sampleRate: Int,
            channels: Int,
        ): String {
            val encodedContent = java.net.URLEncoder.encode(initialContent, "UTF-8")
            val encodedPath = java.net.URLEncoder.encode(audioPath ?: "", "UTF-8")
            // use fixed pseudo id "new_voice" that actually matches the route pattern
            return "note_edit/new_voice?title=${title}&initialContent=${encodedContent}&audioPath=${encodedPath}&durationMs=${durationMs}&sampleRate=${sampleRate}&channels=${channels}"
        }

        const val arg = "noteId"
        const val title = "title"
        const val initialContentArg = "initialContent"
        const val audioPathArg = "audioPath"
        const val durationMsArg = "durationMs"
        const val sampleRateArg = "sampleRate"
        const val channelsArg = "channels"

        val routeWithArgs = "note_edit/{$arg}?$title={$title}&$initialContentArg={$initialContentArg}&$audioPathArg={$audioPathArg}&$durationMsArg={$durationMsArg}&$sampleRateArg={$sampleRateArg}&$channelsArg={$channelsArg}"

        val arguments = listOf(
            navArgument(arg) {
                type = NavType.StringType
                defaultValue = "new" // create mode when missing
            },
            navArgument(title) {
                type = NavType.StringType
                defaultValue = "Ghi chú ghi âm"
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