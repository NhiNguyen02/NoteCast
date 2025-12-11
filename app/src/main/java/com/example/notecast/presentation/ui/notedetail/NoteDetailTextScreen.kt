package com.example.notecast.presentation.ui.notedetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notecast.presentation.theme.*
import com.example.notecast.presentation.viewmodel.NoteDetailViewModel
import com.example.notecast.presentation.ui.dialog.ProcessingDialog
import com.example.notecast.presentation.ui.mindmap.MindMapDialog
import com.example.notecast.presentation.ui.common_components.FolderSelectionButton
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailTextScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    val title = state.title
    val content = state.content
    val chunks = state.chunks
    val folderName = state.folderName
    val availableFolders = state.availableFolders

    val horizontalPadding = 16.dp

    // colors/gradients
    val gradientTop = Color(0xFFB96CFF)
    val gradientMiddle = Color(0xFF8A4BFF)
    val gradientBottom = Color(0xFF6A2CFF)
//    val blueStart = Color(0xFF2EC7FF)
//    val blueEnd = Color(0xFF3AA8FF)

    val headerDividerColor = PrimaryAccent.copy(alpha = 0.22f)
    val tagBg = Color(0xFFFFF5DF)
    val tagBorder = Color(0xFFFFC84D)
    val tagText = Color(0xFF7A3E00)

    var selectedTab by remember { mutableIntStateOf(0) }

    // AUDIO demo state (for preview/demo; replace with real player state)
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0.42f) } // 0..1
    val totalSeconds = 347

    // demo progress increment while playing; safe because it only runs if isPlaying true
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                delay(250L)
                progress = (progress + 0.004f).coerceAtMost(1f)
                if (progress >= 1f) {
                    isPlaying = false
                    break
                }
            }
        }
    }

    // heights (kept as you requested)
    val contentCardHeight = 520.dp
    val transcriptCardHeight = 460.dp // changed to 460 as requested

    // result card state (shown after pressing Tóm tắt)
    var showResultCard by remember { mutableStateOf(false) }

    // keep preview-provided values in sync (hiện tại không dùng resultTitle/resultContent nữa)
    LaunchedEffect(false) {
        showResultCard = false
    }

    // LazyColumn state + coroutine scope for auto-scroll to result (nếu sau này dùng lại)
    val listState = rememberLazyListState()
//    val coroutineScope = rememberCoroutineScope()

    // Scaffold keeps bottomBar fixed; show it only on Văn bản tab (selectedTab == 0)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            NoteDetailHeader(
                title = title,
                onTitleChange = { viewModel.onTitleChanged(it) },
                tagBg = tagBg,
                tagBorder = tagBorder,
                tagText = tagText,
                gradientMiddle = gradientMiddle,
                onBack = onBack,
            )
        },
        bottomBar = {
            if (selectedTab == 0) {
                NoteDetailBottomActions(
                    onNormalize = { viewModel.onNormalizeClicked() },
                    onSaveNote = { viewModel.onSaveNote() },
                    onSummarize = { viewModel.onSummarizeClicked() },
                    onGenerateMindMap = { viewModel.onGenerateMindMapClicked() },
                )
            } else {
                Spacer(modifier = Modifier.height(0.dp))
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            Divider(thickness = 1.dp, color = Color(0xffE5E7EB))
            // Folder selection row (giống NoteEditScreen -> NoteInfoAndActions)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 5.dp)
            ) {
                FolderSelectionButton(
                    currentFolderName = folderName,
                    availableFolders = availableFolders,
                    onFolderSelected = { folder -> viewModel.onFolderSelected(folder) }
                )
            }
            // Divider + Tabs (fixed)
            HorizontalDivider(color = headerDividerColor, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                Column(modifier = Modifier.padding(end = 24.dp).clickable { selectedTab = 0 }) {
                    Text("Văn bản", style = TextStyle(fontSize = 14.sp, fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Medium, color = if (selectedTab == 0) gradientBottom else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)))
                    Spacer(modifier = Modifier.height(6.dp))
                    if (selectedTab == 0) Box(modifier = Modifier.height(3.dp).width(56.dp).clip(RoundedCornerShape(4.dp)).background(gradientBottom))
                }
                Column(modifier = Modifier.padding(end = 24.dp).clickable { selectedTab = 1 }) {
                    Text("Âm thanh", style = TextStyle(fontSize = 14.sp, fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Medium, color = if (selectedTab == 1) gradientBottom else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)))
                    Spacer(modifier = Modifier.height(6.dp))
                    if (selectedTab == 1) Box(modifier = Modifier.height(3.dp).width(56.dp).clip(RoundedCornerShape(4.dp)).background(gradientBottom))
                }
            }

            // CONTENT (scrollable area only). Pass scaffold paddingValues as contentPadding so LazyColumn
            // respects bottomBar height (prevents content from extending under bottom bar).
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                item {
                    if (selectedTab == 0) {
                        // FIX: use fixed-height Box with inner verticalScroll
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(contentCardHeight)
                                .padding(horizontal = horizontalPadding)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(0.5f))
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    "Nội dung",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF5D1AAE)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                BasicTextField(
                                    value = content,
                                    onValueChange = { viewModel.onContentChanged(it) },
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        color = Color(0xFF222222),
                                        lineHeight = 20.sp,
                                        textAlign = TextAlign.Justify
                                    ),
                                    decorationBox = { innerTextField ->
                                        if (content.isEmpty()) {
                                            Text(
                                                "Chỉnh sửa nội dung ghi chú...",
                                                fontSize = 14.sp,
                                                color = Color.Gray.copy(alpha = 0.5f)
                                            )
                                        }
                                        innerTextField()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding)
                        ) {
                            AudioDisplay(
                                isPlaying = isPlaying,
                                onTogglePlay = { isPlaying = !isPlaying },
                                progress = progress,
                                totalSeconds = totalSeconds,
                                gradientTop = gradientTop,
                                gradientMiddle = gradientMiddle,
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            TranscriptionDisplay(
                                chunks = chunks,
                                transcriptCardHeight = transcriptCardHeight,
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }

    // Quan sát isSaved để tự động quay lại (giống NoteEditScreen)
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onBack()
        }
    }

    // Mind map dialog giống NoteEditScreen
    val mindMap = state.mindMap
    if (state.showMindMapDialog && mindMap != null) {
        MindMapDialog(
            rootNode = mindMap,
            onDismiss = { viewModel.onCloseMindMapDialog() }
        )
    }

    // Processing dialog cho mind map
    if (state.isGeneratingMindMap) {
        ProcessingDialog(
            percent = state.processingPercent,
            step = 1,
            onDismissRequest = { /* không cho hủy hoặc xử lý theo ý bạn */ }
        )
    }
}
