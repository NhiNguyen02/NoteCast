package com.example.notecast.presentation.screen.settingsscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import com.example.notecast.presentation.theme.backgroundTertiary
import com.example.notecast.presentation.theme.lightPurple





// --- DATA MODEL (Thay thế bằng ViewModel State) ---

data class SettingsState(
    val summaryModel: SummaryModel = SummaryModel.OFFLINE,
    val summaryLengthPercent: Float = 0.5f, // 50% độ dài gốc
    val summarySentenceCount: Int = 3,
    val autoNormalize: Boolean = true,
    val autoSummarize: Boolean = false,
    val usedStorageGB: Float = 2.1f,
    val totalStorageGB: Float = 5.0f,
    val cacheSizeMB: Int = 128
)

enum class SummaryModel(val label: String, val chipLabel: String) {
    OFFLINE("Mô hình Offline", "Nhanh"),
    ONLINE("Mô hình Online", "Chính xác")
}

// --- COMPOSE CORE ---

@Composable
fun SettingsScreen(
    // Trong ứng dụng thực tế, truyền ViewModel vào đây
    onBackClick: () -> Unit = {}
) {
    // State Mock-up (thay thế bằng ViewModel state.collectAsState())
    var state by remember { mutableStateOf(SettingsState()) }

    Scaffold(
        topBar = { SettingsTopAppBar(onBackClick) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column (
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xffE5E7EB))
            ){}
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()

            ) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // 1. Cấu hình Tóm tắt
                        SettingsSectionHeader(title = "Cấu hình Tóm tắt", subtitle = "Tùy chỉnh cách hệ thống tạo tóm tắt cho ghi chú")
                        SummaryConfiguration(
                            currentModel = state.summaryModel,
                            onModelChange = { state = state.copy(summaryModel = it) }
                        )

                        // 2. Độ dài Tóm tắt Mặc định
                        Spacer(modifier = Modifier.height(24.dp))
                        DefaultSummaryLength(
                            currentLengthPercent = state.summaryLengthPercent,
                            onLengthPercentChange = { state = state.copy(summaryLengthPercent = it) },
                            onSentenceCountChange = { state = state.copy(summarySentenceCount = it) }
                        )

                        // 3. Tùy chọn Tự động
                        Spacer(modifier = Modifier.height(24.dp))
                        SettingsSectionHeader(title = "Tùy chọn Tự động", icon = Icons.Default.FlashOn,
                            subtitle = "Các tính năng tự động chạy sau khi chép lời hoàn tất")
                        AutoOptions(
                            autoNormalize = state.autoNormalize,
                            onAutoNormalizeToggle = { state = state.copy(autoNormalize = it) },
                            autoSummarize = state.autoSummarize,
                            onAutoSummarizeToggle = { state = state.copy(autoSummarize = it) }
                        )

                        // 4. Lưu trữ
                        Spacer(modifier = Modifier.height(24.dp))
                        StorageUsage(
                            usedGB = state.usedStorageGB,
                            totalGB = state.totalStorageGB,
                            cacheSizeMB = state.cacheSizeMB,
                            onClearCache = { /* Xử lý xóa cache */ }
                        )
                    }
                }
            }
        }

    }
}

// --- COMPOSE COMPONENTS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopAppBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text("Cài đặt",
                style = TextStyle(
                    brush = backgroundTertiary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = lightPurple
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(Color.Transparent)
    )
}

@Composable
fun SettingsSectionHeader(title: String, subtitle: String? = null, icon: ImageVector? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = lightPurple, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = lightPurple
        )
    }
    if (subtitle != null) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun SummaryConfiguration(currentModel: SummaryModel, onModelChange: (SummaryModel) -> Unit) {
    val models = SummaryModel.entries

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp)

    ) {
        models.forEach { model ->
            Row(
                modifier = Modifier
                    .clickable { onModelChange(model) }


                    .padding(vertical = 8.dp),
                verticalAlignment =Alignment.CenterVertically,
                horizontalArrangement =  Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .weight(0.8f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = currentModel == model,
                        onClick = { onModelChange(model) },
                        colors = RadioButtonDefaults.colors(selectedColor = lightPurple)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Column(
//                        modifier = Modifier.width()
                    ){
                        Text(model.label, style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                        Text(
                            if (model == SummaryModel.OFFLINE) "Nhanh hơn, hoạt động không cần internet"
                            else "Chính xác hơn, yêu cầu kết nối internet",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                }
// Chip Tốc độ/Chính xác
                AssistChip(
                    onClick = { onModelChange(model) },
                    label = { Text(model.chipLabel, fontSize = 10.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (model == SummaryModel.OFFLINE) Color(0xFF3CB371).copy(alpha = 0.9f) else Color(0xFF1E90FF).copy(alpha = 0.9f),
                        labelColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(24.dp)
                )

            }
        }
    }
}

@Composable
fun DefaultSummaryLength(
    currentLengthPercent: Float,
    onLengthPercentChange: (Float) -> Unit,
    onSentenceCountChange: (Int) -> Unit
) {
    val percentage = (currentLengthPercent * 100).toInt()
    val sliderSteps = 10 // 10% steps

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("Độ dài Tóm tắt Mặc định", style = MaterialTheme.typography.bodyLarge, color = Color.Black)

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Ngắn gọn", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("Chi tiết", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        Slider(
            value = currentLengthPercent,
            onValueChange = onLengthPercentChange,
            steps = sliderSteps,
            valueRange = 0.1f..1f, // 10% đến 100%
            colors = SliderDefaults.colors(activeTrackColor = lightPurple)
        )

        Text(
            text = "${percentage}% độ dài gốc",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = lightPurple,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
        )

        // Hộp chọn nhanh
        Text("Hoặc chọn nhanh:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val quickOptions = listOf(0.3f, 0.5f) // 30%, 50%
            val sentenceOptions = listOf(3) // 3 câu

            quickOptions.forEach { percent ->
                QuickLengthChip(
                    label = "${(percent * 100).toInt()}%",
                    isSelected = currentLengthPercent == percent,
                    onClick = { onLengthPercentChange(percent) }
                )
            }
            sentenceOptions.forEach { count ->
                QuickLengthChip(
                    label = "${count} câu",
                    isSelected = false, // Giả sử chỉ chọn % hoặc câu, không cả hai
                    onClick = { onSentenceCountChange(count) }
                )
            }
        }
    }
}

@Composable
fun QuickLengthChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val chipColor = if (isSelected) lightPurple else Color(0xFFE0E0E0)
    val textColor = if (isSelected) Color.White else Color.Black

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(chipColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .height(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = textColor)
    }
}

@Composable
fun AutoOptions(
    autoNormalize: Boolean,
    onAutoNormalizeToggle: (Boolean) -> Unit,
    autoSummarize: Boolean,
    onAutoSummarizeToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Tùy chọn 1: Tự động Chuẩn hóa Dấu câu
        ToggleSettingItem(
            title = "Tự động Chuẩn hóa Dấu câu",
            subtitle = "Tự động thêm dấu câu vào văn bản chép lời",
            checked = autoNormalize,
            onCheckedChange = onAutoNormalizeToggle
        )
        Divider(color = Color(0xffE5E7EB), thickness = 1.dp)

        // Tùy chọn 2: Tự động Tóm tắt
        ToggleSettingItem(
            title = "Tự động Tóm tắt",
            subtitle = "Tự động tạo bản tóm tắt cho ghi chú mới",
            checked = autoSummarize,
            onCheckedChange = onAutoSummarizeToggle
        )
    }
}

@Composable
fun ToggleSettingItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Color.Black)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = lightPurple)
        )
    }
}

@Composable
fun StorageUsage(
    usedGB: Float,
    totalGB: Float,
    cacheSizeMB: Int,
    onClearCache: () -> Unit
) {
    val usagePercent = usedGB / totalGB

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Tiêu đề Lưu trữ
        Text("Lưu trữ", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = lightPurple)
        Text("Quản lý dung lượng lưu trữ", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))

        // Thanh tiến trình
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Đã sử dụng", style = MaterialTheme.typography.bodyMedium, color = Color.Black)
            Text("${usedGB} GB / ${totalGB} GB", style = MaterialTheme.typography.bodyMedium, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = usagePercent,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = lightPurple,
            trackColor = Color(0xffE5E7EB)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Xóa Cache
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClearCache)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Xóa Cache Tạm thời", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                Text("Giải phóng ${cacheSizeMB} MB dung lượng", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Button(
                onClick = onClearCache,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC143C)), // Màu Đỏ cho Xóa
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Xóa", color = Color.White)
            }
        }
    }
}


@Preview()
@Composable
fun PreviewSettingsScreen() {
    MaterialTheme {
        SettingsScreen()
    }
}