package com.example.notecast.presentation.ui.common_components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.R // Đảm bảo import R
import com.example.notecast.presentation.navigation.Screen
import com.example.notecast.presentation.theme.Background
import com.example.notecast.presentation.theme.MenuBackgroundBrush
import com.example.notecast.presentation.theme.Purple
import kotlinx.coroutines.launch


@Composable
fun AppDrawerContent(
    drawerState: DrawerState,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    // Biến để đóng/mở menu
    val scope = rememberCoroutineScope()

    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxWidth(0.7f) // Chiếm 70% chiều rộng
            .background(brush = MenuBackgroundBrush),
        drawerContainerColor = Color.Transparent
    ) {
        Column {
            // 1. Hàng chứa nút Đóng (X)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {
                    scope.launch { drawerState.close() }
                }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_close_24),
                        contentDescription = "Đóng menu",
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 2. Các mục menu (Style tùy chỉnh)

            // Mục 1: Trang chủ
            DrawerMenuItem(
                text = "Trang chủ",
                iconPainter = painterResource(R.drawable.home_outline),
                isSelected = currentRoute == Screen.Home.route,
                onClick = {
                    onNavigate(Screen.Home.route)
                    scope.launch { drawerState.close() } // Đóng menu
                }
            )

            Spacer(Modifier.height(8.dp))

            // Mục 2: Thư mục
            DrawerMenuItem(
                text = "Thư mục",
                iconPainter = painterResource(R.drawable.folder_outline),
                isSelected = currentRoute == Screen.Folders.route,
                onClick = {
                    onNavigate(Screen.Folders.route)
                    Background
                    scope.launch { drawerState.close() } // Đóng menu
                }
            )

            Spacer(Modifier.height(8.dp))

            // Mục 3: Thông báo
            DrawerMenuItem(
                text = "Thông báo",
                iconPainter = painterResource(R.drawable.bell_outline),
                isSelected = currentRoute == Screen.Notifications.route,
                onClick = {
                    onNavigate(Screen.Notifications.route) //
                    scope.launch { drawerState.close() } // Đóng menu
                }
            )

            Spacer(Modifier.height(8.dp))

            // Mục 4: Cài đặt
            DrawerMenuItem(
                text = "Cài đặt",
                iconPainter = painterResource(R.drawable.setting_outline),
                isSelected = currentRoute == Screen.Settings.route,
                onClick = {
                    onNavigate(Screen.Settings.route)
                    scope.launch { drawerState.close() } // Đóng menu
                }
            )
        }
    }
}

/**
 * Một Composable tùy chỉnh cho từng mục menu để khớp với thiết kế
 */
@Composable
private fun DrawerMenuItem(
    text: String,
    iconPainter: Painter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Màu sắc dựa trên trạng thái (đang chọn hay không)
    val backgroundColor = if (isSelected) Color.White.copy(0.3f) else Color.Transparent
    val contentColor = if (isSelected) Purple else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = text,
            tint = contentColor
        )
        Text(
            text = text,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}