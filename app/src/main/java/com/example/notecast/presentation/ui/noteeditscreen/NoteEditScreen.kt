package com.example.notecast.presentation.ui.noteeditscreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notecast.R
import com.example.notecast.domain.model.Folder
import com.example.notecast.presentation.theme.PopUpBackgroundBrush
import com.example.notecast.presentation.theme.PrimaryAccent
import com.example.notecast.presentation.theme.Purple
import com.example.notecast.presentation.ui.common_components.FolderSelectionButton
import com.example.notecast.presentation.viewmodel.NoteEditViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Màn hình Sửa/Tạo Ghi chú (Đã kết nối ViewModel thật)
 */
@Composable
fun NoteEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: NoteEditViewModel = hiltViewModel()
) {
    // 1. Lấy State từ ViewModel
    val state by viewModel.state.collectAsState()
    var showFolderDialog by remember { mutableStateOf(false) }

    // 2. Logic tự động quay lại khi lưu xong
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            NoteEditTopBar(
                onBackClick = onNavigateBack,
                // Nút Lưu gọi sự kiện OnSaveNote
                onSaveClick = { viewModel.onEvent(NoteEditEvent.OnSaveNote) }
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Nội dung chính
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Divider(thickness = 1.dp, color = Color(0xffE5E7EB))

                // Các nút chức năng (Tóm tắt, Chuẩn hóa...)
                NoteInfoAndActions(
                    folderName = state.folderName, // "Chưa phân loại" hoặc Tên folder
                    isProcessing = state.isSummarizing,
                    availableFolders = state.availableFolders,
                    onFolderSelected = { folder ->
                        viewModel.onEvent(NoteEditEvent.OnFolderSelected(folder))
                    },
                    onSummarize = { viewModel.onEvent(NoteEditEvent.OnSummarize) },
                    onNormalize = { viewModel.onEvent(NoteEditEvent.OnNormalize) },
                    onMindMap = { viewModel.onEvent(NoteEditEvent.OnGenerateMindMap) }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    // --- Ô nhập Tiêu đề ---
                    BasicTextField(
                        value = state.title,
                        onValueChange = { newTitle ->
                            viewModel.onEvent(NoteEditEvent.OnTitleChanged(newTitle))
                        },
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        ),
                        decorationBox = { innerTextField ->
                            if (state.title.isEmpty()) {
                                Text("Tiêu đề...",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp)
                    )

                    // Ngày giờ (Tạm thời lấy giờ hiện tại hoặc từ state nếu có)
                    Text(
                        text = formatNoteDate(System.currentTimeMillis()),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // --- Ô nhập Nội dung ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(0.5f))
                            .padding(16.dp)
                    ) {
                        BasicTextField(
                            value = state.content,
                            onValueChange = { newContent ->
                                viewModel.onEvent(NoteEditEvent.OnContentChanged(newContent))
                            },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                color = Color.Black
                            ),
                            decorationBox = { innerTextField ->
                                if (state.content.isEmpty()) {
                                    Text("Bắt đầu soạn...",
                                        fontSize = 16.sp,
                                        color = Color.Gray.copy(alpha = 0.5f)
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }

            }
        }
    }

}

// --- Composable: Top AppBar tùy chỉnh ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditTopBar(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "Text Notes App",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Transparent // Ẩn title để giống design của bạn
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBackIos, contentDescription = "Quay lại", tint = Purple)
            }
        },
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Nút Lưu
                IconButton(onClick = onSaveClick) {
                    Icon(
                        painter = painterResource(R.drawable.save), // Đảm bảo bạn có icon này
                        contentDescription = "Lưu",
                        tint = Purple,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        )
    )
}


@Composable
fun NoteInfoAndActions(
    folderName: String,
    isProcessing: Boolean,
    onFolderSelected: (Folder?) -> Unit,
    availableFolders: List<Folder>,
    onSummarize: () -> Unit,
    onNormalize: () -> Unit,
    onMindMap: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
    ){
        FolderSelectionButton(
            currentFolderName = folderName,
            availableFolders = availableFolders,
            onFolderSelected = onFolderSelected
        )
    }
    Divider(thickness = 1.dp, color = Color(0xffE5E7EB))

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        item {
            ActionChip(
                leadingIcon = painterResource(R.drawable.file_text), // Đảm bảo có icon
                label = "Tóm tắt",
                onClick = onSummarize,
                isLoading = isProcessing,
                backgroundBrush = PopUpBackgroundBrush,
                labelColor = Color.White,
            )
        }
        item {
            ActionChip(
                label = "Chuẩn hóa",
                leadingIcon = rememberVectorPainter(Icons.Outlined.AutoFixHigh),
                onClick = onNormalize,
                backgroundBrush = Brush.verticalGradient(
                    0.0f to Color(0xff00D2FF),
                    0.59f to Color(0xff307FE3),
                    1.0f to Color(0xff7532FB),
                ),
                labelColor = Color.White
            )
        }
        item {
            ActionChip(
                label = "Mind map",
                leadingIcon = painterResource(R.drawable.icon_park_mindmap_map), // Đảm bảo có icon
                onClick = onMindMap,
                backgroundBrush = Brush.verticalGradient(
                    0.0f to Color(0xffC2D1EC),
                    1.0f to Color(0xff6A92C8)
                ),
                labelColor = Color.White
            )
        }
    }
    Divider(thickness = 1.dp, color = Color(0xffE5E7EB))
}

// Composable phụ cho Chips hành động (GIỮ NGUYÊN)
@Composable
fun ActionChip(
    label: String,
    leadingIcon: Painter,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    backgroundBrush: Brush,
    labelColor: Color
) {
    val chipShape = RoundedCornerShape(8.dp)
    Surface(
        shape = chipShape,
        shadowElevation = 2.dp,
        modifier = Modifier
            .height(32.dp)
            .clickable(enabled = !isLoading, onClick = onClick)
    ) {
        Box(
            modifier = Modifier.background(brush = backgroundBrush),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {

                Icon(
                    painter = leadingIcon,
                    contentDescription = label,
                    tint = labelColor,
                    modifier = Modifier.size(20.dp)
                )
                if (isLoading) {
                    Text("Đang xử lý...", fontSize = 14.sp, color = labelColor)
                    Spacer(Modifier.width(8.dp))
                    CircularProgressIndicator(
                        Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = labelColor
                    )
                } else {
                    Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = labelColor)
                }
            }
        }
    }
}

// Hàm tiện ích định dạng ngày tháng
fun formatNoteDate(timestamp: Long): String {
    val date = Date(timestamp)
    val todayFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val currentDay = todayFormatter.format(Date())
    val noteDay = todayFormatter.format(date)

    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    return if (currentDay == noteDay) {
        "Hôm nay, ${timeFormatter.format(date)}"
    } else {
        "${noteDay}, ${timeFormatter.format(date)}"
    }
}


// Preview: Bạn cần tạo một State giả để preview, không cần MockViewModel phức tạp
@Preview(showBackground = true)
@Composable
fun PreviewNoteEditScreen() {
    // Lưu ý: Preview sẽ khó hoạt động với HiltViewModel trừ khi bạn tách NoteEditScreenContent ra riêng (như HomeScreen)
    // Để đơn giản, ta bỏ qua preview tích hợp ViewModel ở đây
}