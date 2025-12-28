package com.example.notecast.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.notecast.domain.repository.PreferencesRepository
import com.example.notecast.presentation.ui.MainAppScreen
import com.example.notecast.presentation.ui.onboarding.OnboardingScreen
import com.example.notecast.presentation.ui.splashscreen.SplashScreen
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RootNavGraph(navController: NavHostController, preferences: PreferencesRepository) {
    val scope = rememberCoroutineScope()
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
                    scope.launch {
                        preferences.setSeenOnboarding(seen = true)
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainAppScreen()
        }
    }
}