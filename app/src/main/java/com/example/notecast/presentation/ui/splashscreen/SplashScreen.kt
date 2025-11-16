package com.example.notecast.presentation.ui.splashscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.notecast.R
import com.example.notecast.domain.repository.PreferencesRepository
import com.example.notecast.presentation.navigation.Screen
import com.example.notecast.presentation.theme.Background
import com.example.notecast.presentation.theme.LogoBrush
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    navController: NavController,
    preferences: PreferencesRepository
) {
    // Giữ màn hình trong 2 giây
    LaunchedEffect(Unit) {
        delay(2000)

        val hasSeenOnboarding = preferences.hasSeenOnboarding.first()
        // Kiểm tra xem đã xem onboarding chưa
        val route = if (hasSeenOnboarding) {
            Screen.Main.route //
        } else {
            Screen.Onboarding.route
        }

        navController.navigate(route) {
            popUpTo(Screen.Splash.route) { inclusive = true } // xóa splash khỏi back stack
        }
    }

    // UI của Splash
    SplashScreenUI()
}

@Preview(showBackground = true)
@Composable
fun PreviewSplashScreen(){
    SplashScreenUI()
}


@Composable
fun SplashScreenUI() {
    Box(
        modifier = Modifier
            .fillMaxSize()
//            .padding(32.dp)
            .background(brush = Background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo ),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "NOTECAST",
                fontSize = 26.sp,
                style = TextStyle(
                    brush = LogoBrush,
                ),

                fontWeight = FontWeight.Bold
            )
        }
    }
}