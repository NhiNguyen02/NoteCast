package com.example.notecast.navigation

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
}