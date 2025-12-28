package com.example.notecast.presentation.ui.settingsscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pageview
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Pageview
import androidx.compose.material.icons.outlined.TextSnippet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.notecast.presentation.ui.common_components.PermissionGridItem
import com.example.notecast.presentation.ui.common_components.PermissionSwitchItem
import com.example.notecast.presentation.ui.common_components.SectionWrapper
import com.example.notecast.presentation.ui.common_components.SecurityInfoCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit = {}) {
    var state by remember { mutableStateOf(PermissionState()) }

    Scaffold(
        topBar = {
            // TopBar màu tím nhạt đồng bộ với background
            TopAppBar(
                title = {
                    Column() {
                        Text("Cài đặt", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp))
                        Text("Quản lý quyền truy cập ứng dụng", style = TextStyle(fontSize = 12.sp, color = Color.Gray))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBackIos, contentDescription = null, tint = Color(0xFF8E44AD))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Divider(color = Color(0xffE5E7EB), thickness = 1.dp, modifier = Modifier.fillMaxWidth().zIndex(10f))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // --- QUYỀN CƠ BẢN ---
                item {
                    SectionWrapper(title = "Quyền tổng quan", icon = Icons.Default.Lock){
                        PermissionSwitchItem(
                            "Microphone",
                            "Ghi âm giọng nói để chuyển văn bản",
                            state.isMicrophoneEnabled,
                            Icons.Default.Mic
                        ) { state = state.copy(isMicrophoneEnabled = it) }
                        PermissionSwitchItem(
                            "Giao diện",
                            "Sáng/Tối",
                            state.isThemeEnabled,
                            if (!state.isThemeEnabled) Icons.Default.LightMode else Icons.Default.Nightlight
                        ) { state = state.copy(isThemeEnabled = it) }
                        PermissionSwitchItem(
                            "Tự động tóm tắt",
                            "Tự động tạo bản tóm tắt nội dung cho ghi chú mới",
                            state.isSummaryEnabled,
                            Icons.Default.TextSnippet
                        ) { state = state.copy(isSummaryEnabled = it) }
                        PermissionSwitchItem(
                            "Tự động tạo Mind Map",
                            "Tự động tạo sơ đồ MindMap từ nội dung ghi chú mới",
                            state.isMindMapEnabled,
                            Icons.Default.Pageview
                        ) { state = state.copy(isMindMapEnabled = it) }
                        PermissionSwitchItem(
                            "Tự động đồng bộ",
                            "Tải ghi chú lên cloud tự động",
                            state.isAutoSyncEnabled,
                            Icons.Default.CloudUpload
                        ) { state = state.copy(isAutoSyncEnabled = it) }
                    }
                }



                // --- THÔNG TIN BẢO MẬT ---
                item {
                    SectionWrapper(title = "Thông tin bảo mật", icon = Icons.Default.Shield) {
                        SecurityInfoCard("Dữ liệu được mã hóa", "Tất cả ghi chú được mã hóa end-to-end", Icons.Default.Lock)
                        SecurityInfoCard("Lưu trữ cục bộ", "Dữ liệu được lưu an toàn trên thiết bị", Icons.Default.SdCard)
                    }
                }

                item {
                    Button(
                        onClick = { /* Mở link */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Đọc Chính sách Bảo mật")
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

    }
}