package com.example.notecast.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
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
import com.example.notecast.presentation.navigation.Screen
import com.example.notecast.presentation.theme.Background
import com.example.notecast.presentation.ui.common_components.AppDrawerContent
import com.example.notecast.presentation.ui.dialog.CreateNoteDialog
import com.example.notecast.presentation.ui.folderscreen.FolderScreen
import com.example.notecast.presentation.ui.homescreen.HomeScreen
//import com.example.notecast.presentation.ui.noteeditscreen.NoteEditScreen
import com.example.notecast.presentation.ui.record.RecordingScreen
import com.example.notecast.presentation.ui.settingsscreen.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun MainAppScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val appNavController = rememberNavController()

    val navBackStackEntry by appNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route
    var visualSelectedRoute by remember(currentRoute) { mutableStateOf(currentRoute) }

    // Trạng thái Dialog tạo ghi chú (Vẫn giữ ở đây vì nó điều hướng đi nơi khác)
    var showCreateDialog by remember { mutableStateOf(false) }

    // Tự động đóng Drawer khi chuyển màn hình
    LaunchedEffect(currentRoute) {
        if (currentRoute != Screen.Home.route && drawerState.isOpen) {
            scope.launch { drawerState.close() }
        }
    }

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
        gesturesEnabled = !showCreateDialog
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Background),
            color = Color.Transparent
        ) {
            NavHost(
                navController = appNavController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Màn hình HOME
                composable(Screen.Home.route) {
                    // MainAppScreen không cần biết về Filter/Sort/Search nữa
                    HomeScreen(
                        drawerState = drawerState,
                        onOpenCreateDialog = { showCreateDialog = true },
                        onNoteClick = { noteId ->
                            appNavController.navigate(Screen.NoteEdit.createRoute(noteId))
                        }
                    )
                }

                // 2. Màn hình EDIT (Sửa/Tạo)
                composable(
                    route = Screen.NoteEdit.routeWithArgs,
                    arguments = Screen.NoteEdit.arguments
                ) {
//                    NoteEditScreen(
//                        onNavigateBack = { appNavController.popBackStack() }
//                    )
                }

                // 3. Màn hình FOLDER
                composable(Screen.Folders.route) {
                    FolderScreen(
                        onBackClick = { appNavController.popBackStack() },
                        onNewFolderClick = { /* Xử lý tạo folder */ },
                    )
                }

                // 4. Màn hình SETTINGS
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onBackClick = { appNavController.popBackStack() }
                    )
                }

                // 5. Màn hình GHI ÂM
                composable(Screen.Recording.route) {
                    RecordingScreen(
                        onClose = { appNavController.navigateUp() },
                        availableFolders = listOf("Chưa phân loại"),
                        onSaveFile = { folderName, recordedMs ->
                            appNavController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        }
                    )
                }

                // Placeholder
                composable(Screen.Notifications.route) { PlaceholderScreen("Thông báo") }
            }

            // Dialog Tạo Ghi chú (Global)
            if (showCreateDialog) {
                CreateNoteDialog(
                    onDismiss = { showCreateDialog = false },
                    onCreate = { type, autoSummary ->
                        showCreateDialog = false
                        when (type) {
                            "record" -> appNavController.navigate(Screen.Recording.route)
                            "text" -> appNavController.navigate(Screen.NoteEdit.createRoute("new"))
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