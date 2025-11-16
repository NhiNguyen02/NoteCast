package com.example.notecast.navigation

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
    // New: recording route used when user selects "Ghi âm giọng nói"
    object Recording : Screen("recording")
    object NoteEdit : Screen("note_edit/{noteId}"){
        fun createRoute(noteId: Int) = "note_edit/$noteId"

        // Định nghĩa tham số
        const val arg = "noteId"
        val routeWithArgs = "note_edit/{$arg}"
        val arguments = listOf(
            navArgument(arg) {
                type = NavType.IntType
                defaultValue = 0 // Mặc định là 0 (Tạo mới)
            }
        )
    }
}