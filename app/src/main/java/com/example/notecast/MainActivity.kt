package com.example.notecast

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.notecast.domain.repository.PreferencesRepository
import com.example.notecast.navigation.RootNavGraph
import com.example.notecast.presentation.theme.backgroundPrimary
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var preferencesRepository: PreferencesRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

                // Thiết lập NavController và gọi NavGraph ---
                val navController = rememberNavController()
                RootNavGraph(navController = navController, preferences = preferencesRepository)

        }
    }
}