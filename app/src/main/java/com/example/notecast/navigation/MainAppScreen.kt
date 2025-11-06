package com.example.notecast.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.notecast.navigation.AppDrawerContent
import com.example.notecast.navigation.Screen
import com.example.notecast.presentation.components.sampleNotes
import com.example.notecast.presentation.screen.homescreen.HomeScreen
import com.example.notecast.presentation.theme.backgroundPrimary
import kotlinx.coroutines.launch

/**
 * Composable chính, chứa Drawer và NavHost nội bộ.
 * Đây là màn hình mà RootNavGraph sẽ điều hướng đến sau khi Onboarding.
 */
@Composable
fun MainAppScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // NavController nội bộ
    val appNavController = rememberNavController()


    // Lắng nghe route hiện tại để highlight
    val navBackStackEntry by appNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    var visualSelectedRoute by remember(currentRoute) { mutableStateOf(currentRoute) }

//    Logic tìm kiếm
    var searchQuery by remember { mutableStateOf("") }
    val allNotes = remember { sampleNotes } // Dùng dữ liệu mẫu
    val filteredNotes = remember(searchQuery, allNotes) {
        if (searchQuery.isBlank()) {
            allNotes
        } else {
            allNotes.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.content.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    LaunchedEffect(currentRoute) {
        if (currentRoute != Screen.Home.route && drawerState.isOpen) {
            scope.launch {
                drawerState.close()
            }
        }
    }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                drawerState = drawerState,
                currentRoute = visualSelectedRoute,
                onNavigate = { route ->
                    // Cập nhật trạng thái trực quan NGAY LẬP TỨC
                    visualSelectedRoute = route
                    // Điều hướng trong NavHost nội bộ
                    appNavController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()
                .background(brush = backgroundPrimary),
            color = Color.Transparent
        ) {
            // NavHost nội bộ
            NavHost(
                navController = appNavController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Màn hình Home (file HomeScreen.kt của bạn)
                composable(Screen.Home.route) {
                    HomeScreen(
                        // Truyền drawerState vào HomeScreen để nó có thể mở
                        drawerState = drawerState,
                        searchQuery = searchQuery,
                        onSearch = { newQuery ->
                            searchQuery = newQuery
                        },
                    notes = sampleNotes, // Dùng sample data
//                        notes = emptyList(),
                        onFilterClick = {},
                        onSortClick = {},
                        onAddNoteClick = {},
                        onToggleFavorite = {},
                        onTogglePin = {}
                    )
                }

                // 2. Các màn hình Placeholder khác
                composable(Screen.Folders.route) {
                    PlaceholderScreen(text = "Thư mục")
                }
                composable(Screen.Notifications.route) {
                    PlaceholderScreen(text = "Thông báo")
                }
                composable(Screen.Settings.route) {
                    PlaceholderScreen(text = "Cài đặt")
                }
            }
        }
    }
}

// Composable giữ chỗ
@Composable
fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text)
    }
}