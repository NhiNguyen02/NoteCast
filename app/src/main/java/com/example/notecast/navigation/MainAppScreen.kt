package com.example.notecast.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.notecast.navigation.AppDrawerContent
import com.example.notecast.navigation.Screen
import com.example.notecast.presentation.components.sampleNotes
import com.example.notecast.presentation.screen.sort.SortScreen
import com.example.notecast.presentation.screen.filter.FilterScreen
import com.example.notecast.presentation.screen.dialog.CreateNoteDialog
import com.example.notecast.presentation.screen.homescreen.HomeScreen
import com.example.notecast.presentation.screen.record.RecordingScreen
import com.example.notecast.presentation.theme.backgroundPrimary
import kotlinx.coroutines.launch

@Composable
fun MainAppScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val appNavController = rememberNavController()

    val navBackStackEntry by appNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route
    var visualSelectedRoute by remember(currentRoute) { mutableStateOf(currentRoute) }

    var searchQuery by remember { mutableStateOf("") }
    val allNotes = remember { sampleNotes }

    // Overlay / dialog states
    var showCreateDialog by remember { mutableStateOf(false) }
    var showFilterScreen by remember { mutableStateOf(false) }
    var showSortScreen by remember { mutableStateOf(false) }

    // Ensure drawer closes automatically when navigating away (existing behavior)
    LaunchedEffect(currentRoute) {
        if (currentRoute != Screen.Home.route && drawerState.isOpen) {
            scope.launch { drawerState.close() }
        }
    }

    // If Filter/Sort/Dialogs are visible, close drawer and disable gestures so user can't open it underneath
    LaunchedEffect(showFilterScreen, showSortScreen, showCreateDialog) {
        if (showFilterScreen || showSortScreen || showCreateDialog) {
            if (drawerState.isOpen) {
                scope.launch { drawerState.close() }
            }
        }
    }

    // IMPORTANT: set gesturesEnabled = !showFilterScreen (and other overlays) so edge swipe/tap won't open drawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                drawerState = drawerState,
                currentRoute = visualSelectedRoute,
                onNavigate = { route ->
                    visualSelectedRoute = route
                    appNavController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        gesturesEnabled = !showFilterScreen && !showSortScreen && !showCreateDialog // disable gestures while overlays shown
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundPrimary),
            color = Color.Transparent
        ) {
            NavHost(
                navController = appNavController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        drawerState = drawerState,
                        notes = allNotes,
                        searchQuery = searchQuery,
                        onSearch = { newQuery -> searchQuery = newQuery },
                        onFilterClick = { showFilterScreen = true },
                        onSortClick = { showSortScreen = true },
                        onOpenCreateDialog = { showCreateDialog = true },
                        onAddNoteClick = {},
                        onToggleFavorite = {},
                        onTogglePin = {}
                    )
                }

                composable(Screen.Folders.route) { PlaceholderScreen(text = "Thư mục") }
                composable(Screen.Notifications.route) { PlaceholderScreen(text = "Thông báo") }
                composable(Screen.Settings.route) { PlaceholderScreen(text = "Cài đặt") }

                composable(Screen.Recording.route) {
                    RecordingScreen(
                        onClose = { appNavController.navigateUp() },
                        availableFolders = listOf("Công việc", "Cá nhân", "Ý tưởng"),
                        onSaveFile = { folderName, recordedMs ->
                            appNavController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        }
                    )
                }
            }

            // Show Filter overlay full-screen
            if (showFilterScreen) {
                FilterScreen(onClose = { showFilterScreen = false })
            }

            // Show Sort overlay full-screen
            if (showSortScreen) {
                SortScreen(onClose = { showSortScreen = false })
            }

            // Create note dialog: when user chooses a type, handle accordingly
            if (showCreateDialog) {
                CreateNoteDialog(
                    onDismiss = { showCreateDialog = false },
                    onCreate = { type, autoSummary ->
                        showCreateDialog = false
                        when (type) {
                            "record" -> appNavController.navigate(Screen.Recording.route)
                            "text" -> { /* handle text note */ }
                        }
                    },
                    startAutoSummary = true
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text)
    }
}