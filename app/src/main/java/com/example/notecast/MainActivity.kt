package com.example.notecast

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.rememberNavController
import com.example.notecast.domain.repository.PreferencesRepository
import com.example.notecast.presentation.navigation.RootNavGraph
import com.example.notecast.presentation.theme.NoteCastTheme
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
            NoteCastTheme{
                RootNavGraph(navController = navController, preferences = preferencesRepository)
            }
        }
    }
}