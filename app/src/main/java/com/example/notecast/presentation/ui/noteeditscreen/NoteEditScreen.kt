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
import androidx.compose.material.icons.outlined.Pageview
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notecast.presentation.ui.common_components.NoteInfoAndActions
import com.example.notecast.presentation.ui.dialog.ProcessingDialog
import com.example.notecast.presentation.ui.mindmap.MindMapDialog
import com.example.notecast.presentation.viewmodel.NoteEditViewModel
import com.example.notecast.utils.formatNoteDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

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
                    onSummarize = { showSummaryDialog = true },
//                    onNormalize = { viewModel.onEvent(NoteEditEvent.OnNormalize) },
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

    // Summary dialog (in-file) - opens when user taps Tóm tắt chip
    if (showSummaryDialog) {
        SummaryDialog(
            noteContent = state.content,
            isProcessing = state.isSummarizing,
            error = state.error,
            onStart = {
                // trigger ViewModel summarization
                viewModel.onEvent(NoteEditEvent.OnSummarize)
            },
            onDismiss = {
                showSummaryDialog = false
            },
            contentAfter = state.content
        )
    }

    if (state.showMindMapDialog && state.mindMapData != null) {
        MindMapDialog(
            rootNode = state.mindMapData!!,
            onDismiss = { viewModel.onEvent(NoteEditEvent.OnCloseMindMap) }
        )

    }
    if (state.isGeneratingMindMap) {
        // Sử dụng ProcessingDialog bạn đã có
        ProcessingDialog(
            percent = state.processingPercent,
            step = 1, // Hoặc số bước tùy logic của dialog bạn
            onDismissRequest = {
                // Tùy chọn: Có cho phép hủy khi đang tạo không?
                // Nếu không, để trống hoặc không làm gì
            }
        )
    }
    if (state.isNormalizing){
        ProcessingDialog(
            percent = state.processingPercent,
            step = 1,
            onDismissRequest = {}
        )
    }

}

// Summary dialog composable (in-file). Displays initial preview, runs summarization via onStart,
// shows loading state, and shows extracted summary from contentAfter when available.
@Composable
fun SummaryDialog(
    noteContent: String,
    isProcessing: Boolean,
    error: String?,
    onStart: () -> Unit,
    onDismiss: () -> Unit,
    contentAfter: String // observe ViewModel content to detect appended summary
) {
    val clipboard = LocalClipboardManager.current
    var showResult by remember { mutableStateOf(false) }
    val extracted by remember(contentAfter) {
        mutableStateOf(extractSummaryFromContent(contentAfter))
    }

    LaunchedEffect(isProcessing, extracted) {
        // when processing finished and extracted text is present, show result
        if (!isProcessing && !extracted.isNullOrBlank()) {
            showResult = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (showResult) {
                TextButton(onClick = {
                    // copy and close
                    extracted?.let { clipboard.setText(AnnotatedString(it)) }
                    onDismiss()
                }) {
                    Text("Copy & Close")
                }
            } else {
                TextButton(onClick = onStart) {
                    Text("Tạo tóm tắt")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        },
        title = {
            Text("Tóm tắt ghi chú")
        },
        text = {
            Column {
                if (isProcessing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Đang tạo tóm tắt...")
                    }
                } else if (!showResult) {
                    if (!error.isNullOrBlank()) {
                        Text("Lỗi: $error", color = Color.Red)
                        Spacer(Modifier.height(8.dp))
                    }
                    Text("Nội dung (xem trước):", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(previewText(noteContent), style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("Kết quả tóm tắt:", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(extracted ?: "Không tìm thấy tóm tắt", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    )
}