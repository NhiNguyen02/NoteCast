package com.example.notecast.presentation.ui.notetext

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notecast.presentation.theme.TitleBrush
import com.example.notecast.presentation.ui.common_components.NoteInfoAndActions
import com.example.notecast.presentation.ui.dialog.SavedNotificationDialog
import com.example.notecast.presentation.ui.mindmap.MindMapDialog
import com.example.notecast.presentation.viewmodel.NoteTextViewModel
import com.example.notecast.utils.formatNoteDate
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.res.painterResource
import com.example.notecast.R
import com.example.notecast.presentation.theme.*
import com.example.notecast.presentation.viewmodel.NoteAudioViewModel

/**
 * Màn hình Sửa/Tạo Ghi chú (Đã kết nối ViewModel thật)
 */
@Composable
fun NoteTextScreen(
    onNavigateBack: () -> Unit,
    viewModel: NoteTextViewModel = hiltViewModel()
) {
    // 1. Lấy State từ ViewModel
    val state by viewModel.state.collectAsState()

    // 2. Logic tự động quay lại khi lưu xong
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Văn bản", "Tóm tắt", "Từ khóa", "Mindmap")
    val tabScrollState = rememberScrollState()
    var showMindmapDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                NoteEditTopBar(
                    onBackClick = onNavigateBack,
                    // Nút Lưu gọi sự kiện OnSaveNote
                    onSaveClick = { viewModel.onEvent(NoteEditEvent.OnSaveNote) },
                    folderId = state.folderId,
                    availableFolders = state.availableFolders,
                    onFolderSelected = { folder ->
                        viewModel.onEvent(NoteEditEvent.OnFolderSelected(folder))
                    }
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
                        .background(Background)
                ) {
                    HorizontalDivider(
                        color = Color(0xffE5E7EB),
                        thickness = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Các nút chức năng (Tóm tắt, Chuẩn hóa...)
                    NoteInfoAndActions(
                        isProcessing = state.isNormalizing || state.isSummarizing || state.isGeneratingMindMap,
                        hasMindMap = state.mindMapData != null,
                        currentStep = when {
                            state.isGeneratingMindMap -> NoteAudioViewModel.ProcessingStep.MINDMAP
                            state.isSummarizing -> NoteAudioViewModel.ProcessingStep.SUMMARIZING
                            state.isNormalizing -> NoteAudioViewModel.ProcessingStep.NORMALIZING
                            else -> NoteAudioViewModel.ProcessingStep.DONE
                        },
                        onRegenerateAll = {
                            // với text note, gọi normalize + summary + mindmap
                            viewModel.onEvent(NoteEditEvent.OnNormalize)
                            viewModel.onEvent(NoteEditEvent.OnSummarize)
                            viewModel.onEvent(NoteEditEvent.OnGenerateMindMap)
                        },
                    )

                    Column(
                        modifier = Modifier
                    ) {
                        // Title + date
                        BasicTextField(
                            value = state.title,
                            onValueChange = { newTitle ->
                                viewModel.onEvent(NoteEditEvent.OnTitleChanged(newTitle))
                            },
                            textStyle = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Transparent // ẩn màu chữ thật, ta sẽ vẽ bằng brush
                            ),
                            cursorBrush = TitleBrush, // bạn đã dùng ở NoteDetailTextScreen
                            decorationBox = { innerTextField ->
                                Box {
                                    if (state.title.isEmpty()) {
                                        // Placeholder bình thường
                                        Text(
                                            text = "Tiêu đề...",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray.copy(alpha = 0.5f)
                                        )
                                    } else {
                                        // Vẽ text bằng brush gradient
                                        Text(
                                            text = state.title,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            style = TextStyle(
                                                brush = TitleBrush  // hỗ trợ từ compose ui-text 1.6.0+
                                            )
                                        )
                                    }
                                    // Field thật để nhận input + cursor, nhưng chữ của nó là transparent
                                    innerTextField()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 5.dp)
                                .padding(horizontal = 16.dp)
                        )

                        // Ngày giờ (Tạm thời lấy giờ hiện tại hoặc từ state nếu có)
                        Text(
                            text = formatNoteDate(state.updatedAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                        )
                        HorizontalDivider(
                            color = Color(0xffE5E7EB),
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        // Tabs row (horizontally scrollable)
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .horizontalScroll(tabScrollState)
                        ) {
                            tabs.forEachIndexed { index, label ->
                                Column(
                                    modifier = Modifier
                                        .padding(end = 24.dp)
                                        .clickable { selectedTab = index }
                                ) {
                                    Text(
                                        label,
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = if (selectedTab == index)
                                                FontWeight.SemiBold else FontWeight.Medium,
                                            color = if (selectedTab == index)
                                                SubTitleColor
                                            else
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    if (selectedTab == index) {
                                        Box(
                                            modifier = Modifier
                                                .height(3.dp)
                                                .width(56.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(SubTitleColor)
                                        )
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            when (selectedTab) {

                                // ===== TAB VĂN BẢN =====
                                0 -> {
                                    val scrollState = rememberScrollState()
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(0.5f))
                                            .verticalScroll(scrollState)
                                            .padding(20.dp)
                                    ) {
                                        BasicTextField(
                                            value = state.content,
                                            onValueChange = {
                                                viewModel.onEvent(NoteEditEvent.OnContentChanged(it))
                                            },
                                            textStyle = TextStyle(
                                                fontSize = 16.sp,
                                                color = Color.Black,
                                                textAlign = TextAlign.Justify
                                            ),
                                            decorationBox = { innerTextField ->
                                                if (state.content.isEmpty()) {
                                                    Text(
                                                        "Bắt đầu soạn...",
                                                        fontSize = 16.sp,
                                                        color = Color.Gray.copy(alpha = 0.5f)
                                                    )
                                                }
                                                innerTextField()
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                // ===== TAB TÓM TẮT =====
                                1 -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(0.5f))
                                            .verticalScroll(rememberScrollState())
                                            .padding(20.dp),
                                    ) {
                                        Text(
                                            text = state.summary ?: "Chưa có tóm tắt.",
                                            fontSize = 16.sp,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (state.summary != null) Color.Black else Color.Gray
                                        )
                                    }
                                }

                                // ===== TAB TỪ KHÓA =====
                                2 -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (state.keywords.isEmpty()) {
                                            Text(
                                                "Chưa có từ khóa.",
                                                color = Color.Gray
                                            )
                                        } else {
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                state.keywords.forEach {
                                                    AssistChip(
                                                        onClick = {},
                                                        enabled = false,
                                                        label = { Text(it) },
                                                        colors = AssistChipDefaults.assistChipColors(
                                                            labelColor = Color.Black,
                                                            disabledLabelColor = Color.Black,
                                                        ),
                                                        border = BorderStroke(
                                                            width = 1.dp,
                                                            color = Color.Black.copy(alpha = 0.2f)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // ===== TAB MINDMAP =====
                                3 -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val mindMap = state.mindMapData
                                        if (mindMap != null) {
                                            ActionChip(
                                                label = "Xem mindmap chi tiết",
                                                leadingIcon = painterResource(R.drawable.mindmap_map_svgrepo_com),
                                                onClick = { showMindmapDialog = true },
                                                enabled = true,
                                                isLoading = false,
                                                backgroundBrush = MenuBackgroundBrush,
                                                labelColor = Color.White
                                            )
                                        } else {
                                            Text(
                                                "Chưa có mindmap.",
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog mindmap điều khiển bằng state cục bộ, ngoài nội dung tab
    if (showMindmapDialog && state.mindMapData != null) {
        MindMapDialog(
            rootNode = state.mindMapData!!,
            onDismiss = { showMindmapDialog = false }
        )
    }

    if (state.showSavedDialog) {
        SavedNotificationDialog(
            message = "Đã lưu ghi chú thành công",
            onDismissRequest = {
                viewModel.onEvent(NoteEditEvent.OnClearSavedDialog)
            }
        )
    }
}
