//package com.example.notecast.presentation.ui.noteeditscreen
//
//import android.annotation.SuppressLint
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBackIos
//import androidx.compose.material.icons.filled.PushPin
//import androidx.compose.material.icons.outlined.AutoFixHigh
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.painter.Painter
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.graphics.vector.rememberVectorPainter
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.hilt.navigation.compose.hiltViewModel
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//import androidx.lifecycle.ViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import com.example.notecast.domain.model.Note
//import com.example.notecast.presentation.viewmodel.NoteEditViewModel
//import com.example.notecast.presentation.viewmodel.NoteEditViewModelContract
//import com.example.notecast.presentation.viewmodel.NoteEditScreenState
//import com.example.notecast.R
//import com.example.notecast.presentation.theme.PopUpBackgroundBrush
//import com.example.notecast.presentation.theme.Purple
//
//@Composable
//fun NoteEditScreen(
//    onBackClick: () -> Unit = {},
//
//    viewModel: NoteEditViewModelContract = hiltViewModel<NoteEditViewModel>()
//) {
//    val state by viewModel.state.collectAsState()
//    val note = state.note
//
//    Scaffold(
//        topBar = {
//            NoteEditTopBar(
//                onBackClick = onBackClick,
//                isPinned = false,
//                isFavorite = note?.isFavorite ?: false,
//                onTogglePin = viewModel::togglePin,
//                onToggleFavorite = viewModel::toggleFavorite,
//            )
//        },
//        containerColor = Color.Transparent
//    ) { paddingValues ->
//
//
//        if (state.isLoading) {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                CircularProgressIndicator()
//            }
//        } else if (note != null) {
//            // Nội dung chính
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//            ) {
//                Divider(thickness = 1.dp, color = Color(0xffE5E7EB))
//
//                NoteInfoAndActions(
//
//                    folderName = "Chưa phân loại", // TODO: Cần logic tải Folder
//                    lastEdited = Date(note.updatedAt),
//                    isProcessing = state.isSummarizing,
//                    onSummarize = viewModel::summarizeNote,
//                    onNormalize = viewModel::normalizeNote,
//                    onMindMap = viewModel::generateMindMap
//                )
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp),
//                ) {
//                    BasicTextField(
//                        value = note.title,
//                        onValueChange = viewModel::onTitleChange,
//                        textStyle = TextStyle(
//                            fontSize = 24.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.Black
//                        ),
//                        decorationBox = { innerTextField ->
//                            if (note.title.isEmpty()) {
//                                Text("Tiêu đề...",
//                                    fontSize = 24.sp,
//                                    fontWeight = FontWeight.Bold,
//                                    color = Color.Gray.copy(alpha = 0.5f)
//                                )
//                            }
//                            innerTextField()
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(bottom = 5.dp)
//
//                    )
//
//                    Text(
//                        text = formatNoteDate(note.updatedAt),
//                        style = MaterialTheme.typography.bodySmall,
//                        color = Color.Gray,
//                        modifier = Modifier.padding(bottom = 20.dp)
//                    )
//
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(1f)
//                            .clip(RoundedCornerShape(12.dp))
//                            .background(Color(0xffE5E7EB))
//                            .padding(16.dp)
//                    ) {
//                        BasicTextField(
//                            value = note.content ?: "", // SỬA: Xử lý content null
//                            onValueChange = viewModel::onContentChange,
//                            textStyle = TextStyle(
//                                fontSize = 16.sp,
//                                color = Color.Black
//                            ),
//                            decorationBox = { innerTextField ->
//                                if (note.content.isNullOrEmpty()) {
//                                    Text("Bắt đầu soạn...",
//                                        fontSize = 16.sp,
//                                        color = Color.Gray.copy(alpha = 0.5f)
//                                    )
//                                }
//                                innerTextField()
//                            },
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .verticalScroll(rememberScrollState())
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//// --- Composable: Top AppBar tùy chỉnh ---
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun NoteEditTopBar(
//    onBackClick: () -> Unit,
//    isPinned: Boolean,
//    isFavorite: Boolean,
//    onTogglePin: () -> Unit,
//    onToggleFavorite: () -> Unit
//) {
//    TopAppBar(
//        title = {
//            Text(
//                "Text Notes App",
//                style = MaterialTheme.typography.titleMedium,
//                color = Color.Transparent
//            )
//        },
//        navigationIcon = {
//            IconButton(onClick = onBackClick) {
//                Icon(Icons.Filled.ArrowBackIos, contentDescription = "Quay lại", tint = Purple)
//            }
//        },
//        actions = {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                // Nút Ghim
////                IconButton(onClick = onTogglePin) {
////                    Icon(
////                        Icons.Default.PushPin,
////                        contentDescription = "Ghim",
////                        tint = if (isPinned) Purple else Color.Gray,
////                        modifier = Modifier.size(20.dp)
////                    )
////                }
//
//                // Nút lưu (Yêu thích?)
//                IconButton(onClick = onToggleFavorite) {
//                    Icon(
//                        painter = painterResource(R.drawable.save),
//                        contentDescription = "Lưu", // Đổi tên mô tả
//                        tint = if (isFavorite) Purple else Color.Gray,
//                        modifier = Modifier.size(20.dp)
//                    )
//                }
//            }
//        },
//        colors = TopAppBarDefaults.topAppBarColors(
//            containerColor = Color.Transparent,
//        )
//    )
//}
//
//// --- Composable: Thông tin và Chips hành động ---
//
//@Composable
//fun NoteInfoAndActions(
//    folderName: String,
//    lastEdited: Date,
//    isProcessing: Boolean,
//    onSummarize: () -> Unit,
//    onNormalize: () -> Unit,
//    onMindMap: () -> Unit
//) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 5.dp)
//    ){
//        AssistChip(
//            leadingIcon = {
//                Icon(
//                    painter = painterResource(R.drawable.folder_outline),
//                    tint = Color.White,
//                    contentDescription = "folder",
//                    modifier = Modifier.size(20.dp)
//                )
//            },
//            onClick = { /* Mở Dialog chọn thư mục */ },
//            label = { Text(folderName, fontSize = 14.sp) },
//            colors = AssistChipDefaults.assistChipColors(
//                containerColor = Color(0xffCCA8FF),
//                labelColor = Color.White
//            ),
//            shape = RoundedCornerShape(8.dp),
//        )
//    }
//    Divider(thickness = 1.dp, color = Color(0xffE5E7EB))
//
//    LazyRow(
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//        modifier = Modifier
//            .padding(vertical = 12.dp, horizontal = 16.dp)
//            .fillMaxWidth()
//    ) {
//        item {
//            ActionChip(
//                leadingIcon = painterResource(R.drawable.file_text),
//                label = "Tóm tắt",
//                onClick = onSummarize,
//                isLoading = isProcessing, // Sử dụng state để hiển thị loading
//                backgroundBrush = PopUpBackgroundBrush,
//                labelColor = Color.White,
//            )
//        }
//        item {
//            ActionChip(
//                label = "Chuẩn hóa",
//                leadingIcon = rememberVectorPainter(Icons.Outlined.AutoFixHigh),
//                onClick = onNormalize,
//                backgroundBrush = Brush.verticalGradient(
//                    0.0f to Color(0xff00D2FF),
//                    0.59f to Color(0xff307FE3),
//                    1.0f to Color(0xff7532FB),
//                ),
//                labelColor = Color.White
//            )
//        }
//        item {
//            ActionChip(
//                label = "Mind map",
//                leadingIcon = painterResource(R.drawable.icon_park_mindmap_map),
//                onClick = onMindMap,
//                backgroundBrush = Brush.verticalGradient(
//                    0.0f to Color(0xffC2D1EC),
//                    1.0f to Color(0xff6A92C8)
//                ),
//                labelColor = Color.White
//            )
//        }
//    }
//    Divider(thickness = 1.dp, color = Color(0xffE5E7EB))
//}
//
//// Composable phụ cho Chips hành động (có thể hiển thị loading)
//@Composable
//fun ActionChip(
//    label: String,
//    leadingIcon: Painter,
//    onClick: () -> Unit,
//    isLoading: Boolean = false,
//    backgroundBrush: Brush,
//    labelColor: Color
//) {
//    val chipShape = RoundedCornerShape(8.dp)
//    Surface(
//        shape = chipShape,
//        shadowElevation = 2.dp,
//        modifier = Modifier
//            .height(32.dp)
//            .clickable(enabled = !isLoading, onClick = onClick)
//    ) {
//        Box(
//            modifier = Modifier.background(brush = backgroundBrush),
//            contentAlignment = Alignment.Center
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(5.dp),
//                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
//            ) {
//
//                Icon(
//                    painter = leadingIcon,
//                    contentDescription = label,
//                    tint = labelColor,
//                    modifier = Modifier.size(20.dp)
//                )
//                if (isLoading) {
//                    Text("Đang xử lý...", fontSize = 14.sp, color = labelColor)
//                    Spacer(Modifier.width(8.dp))
//                    CircularProgressIndicator(
//                        Modifier.size(16.dp),
//                        strokeWidth = 2.dp,
//                        color = labelColor
//                    )
//                } else {
//                    Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = labelColor)
//                }
//            }
//        }
//    }
//
//}
//
//// Hàm tiện ích định dạng ngày tháng
//fun formatNoteDate(timestamp: Long): String {
//    val date = Date(timestamp)
//    val todayFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//    val currentDay = todayFormatter.format(Date())
//    val noteDay = todayFormatter.format(date)
//
//    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
//
//    return if (currentDay == noteDay) {
//        "Hôm nay, ${timeFormatter.format(date)}"
//    } else {
//        "${noteDay}, ${timeFormatter.format(date)}"
//    }
//}
//
//// --- MOCK VIEWMODEL CHO PREVIEW ---
//class NoteEditViewModelMock : ViewModel(), NoteEditViewModelContract {
//    private val mockNote = Note(
//        id = "preview",
//        title = "Tiêu đề ghi chú mẫu",
//        content = "Nội dung mẫu để xem trước giao diện. Đây là một đoạn văn bản dài hơn một chút để kiểm tra khu vực soạn thảo.",
//        updatedAt = System.currentTimeMillis()
//    )
//    private val _state = MutableStateFlow(NoteEditScreenState(note = mockNote, isSummarizing = false))
//    override val state = _state.asStateFlow()
//
//    override fun togglePin() {}
//    override fun toggleFavorite() {}
//    override fun summarizeNote() {}
//    override fun normalizeNote() {}
//    override fun generateMindMap() {}
//    override fun onTitleChange(newTitle: String) {}
//    override fun onContentChange(newContent: String) {}
//    override fun saveNote() {}
//}
//
//
//@SuppressLint("ViewModelConstructorInComposable")
//@Preview()
//@Composable
//fun PreviewNoteEditScreen() {
//    MaterialTheme {
//        // GỌI MOCK VIEWMODEL KHÔNG PHỤ THUỘC VÀO HILT
//        NoteEditScreen(viewModel = NoteEditViewModelMock())
//    }
//}