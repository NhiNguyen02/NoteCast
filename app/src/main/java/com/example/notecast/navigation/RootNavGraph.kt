package com.example.notecast.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.notecast.domain.repository.PreferencesRepository
import com.example.notecast.presentation.screen.MainAppScreen
import com.example.notecast.presentation.screen.onboarding.OnboardingScreen
import com.example.notecast.presentation.screen.splashscreen.SplashScreen

@Composable
fun RootNavGraph(navController: NavHostController, preferences: PreferencesRepository) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController, preferences = preferences)
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingFinished = {
                    preferences.setSeenOnboarding()
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainAppScreen()
        }
    }
}