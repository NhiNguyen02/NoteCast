package com.example.notecast.presentation.ui.noteeditscreen

import androidx.compose.foundation.background
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
import com.example.notecast.presentation.ui.dialog.ProcessingDialog
import com.example.notecast.presentation.ui.dialog.ProcessingType
import com.example.notecast.presentation.ui.mindmap.MindMapDialog
import com.example.notecast.presentation.viewmodel.NoteEditViewModel
import com.example.notecast.utils.formatNoteDate

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
    var showSummaryDialog by remember { mutableStateOf(false) }

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
            ) {
                Divider(thickness = 1.dp, color = Color(0xffE5E7EB))

                // Các nút chức năng (Tóm tắt, Chuẩn hóa...)
                NoteInfoAndActions(
                    isProcessing = state.isSummarizing,
                    isNormalizing = state.isNormalizing,
                    hasMindMap = state.mindMapData != null,
                    onNormalize = { viewModel.onEvent(NoteEditEvent.OnNormalize) },
                    // open the in-file summary dialog, which will call the ViewModel
                    onSummarize = { showSummaryDialog = true },
                    onMindMap = { viewModel.onEvent(NoteEditEvent.OnGenerateMindMap) },
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
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
                            .padding(bottom = 5.dp)
                    )

                    // Ngày giờ (Tạm thời lấy giờ hiện tại hoặc từ state nếu có)
                    Text(
                        text = formatNoteDate(state.updatedAt),
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
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        BasicTextField(
                            value = state.content,
                            onValueChange = { newContent ->
                                viewModel.onEvent(NoteEditEvent.OnContentChanged(newContent))
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
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }

            }
        }
    }

    if (state.showMindMapDialog && state.mindMapData != null) {
        MindMapDialog(
            rootNode = state.mindMapData!!,
            onDismiss = { viewModel.onEvent(NoteEditEvent.OnCloseMindMap) }
        )

    }
    if (state.isGeneratingMindMap) {
        ProcessingDialog(
            percent = state.processingPercent,
            step = 1,
            type = ProcessingType.MindMap,
            onDismissRequest = { }
        )
    }
    if (state.isNormalizing){
        ProcessingDialog(
            percent = state.processingPercent,
            step = 1,
            type = ProcessingType.Normalize,
            onDismissRequest = {}
        )
    }
    if (state.isSummarizing) {
        ProcessingDialog(
            percent = state.processingPercent,
            step = 1,
            type = ProcessingType.Summarize,
            onDismissRequest = { }
        )
    }

}
