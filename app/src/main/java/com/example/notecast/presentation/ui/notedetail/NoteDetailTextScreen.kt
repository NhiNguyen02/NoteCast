package com.example.notecast.presentation.ui.notedetail

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.compose.ui.platform.LocalContext
import com.example.notecast.presentation.ui.common_components.NoteInfoAndActions
import com.example.notecast.presentation.ui.noteeditscreen.NoteEditEvent
import com.example.notecast.utils.formatNoteDate

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
    val folderId = state.folderId
    val availableFolders = state.availableFolders

    // --- Audio info from DB ---
    val filePath = state.filePath
    val durationMs = state.durationMs ?: 0L
    val totalSeconds = remember(durationMs) { ((durationMs.coerceAtLeast(0L)) / 1000L).toInt() }
    val hasValidAudio = !filePath.isNullOrBlank() && totalSeconds > 0

    // --- ExoPlayer instance tied to this screen & filePath ---
    val context = LocalContext.current
    val player: ExoPlayer? = remember(filePath) {
        if (filePath.isNullOrBlank()) null
        else ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(filePath))
            prepare()
        }
    }

    DisposableEffect(player) {
        onDispose { player?.release() }
    }

    // Player-backed UI state
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) } // 0f..1f

    // Reset when note or file changes
    LaunchedEffect(state.noteId, filePath) {
        isPlaying = false
        progress = 0f
    }

    // Sync isPlaying & progress with ExoPlayer
    LaunchedEffect(player, durationMs) {
        if (player == null) return@LaunchedEffect
        while (true) {
            delay(200L)
            val d = player.duration.takeIf { it > 0 } ?: durationMs
            if (d > 0) {
                val ratio = (player.currentPosition.toFloat() / d.toFloat()).coerceIn(0f, 1f)
                progress = ratio
            }
            isPlaying = player.isPlaying

            // Nếu player đã đến cuối nhưng isPlaying vẫn false, đảm bảo progress = 1f
            if (!player.isPlaying && player.playbackState == ExoPlayer.STATE_ENDED) {
                progress = 1f
            }
        }
    }

    LaunchedEffect(filePath, durationMs) {
        Log.d("NoteDetailTextScreen", "Loaded audio: filePath=$filePath, durationMs=$durationMs")
    }

    val horizontalPadding = 16.dp

    // colors/gradients
    val gradientTop = Color(0xFFB96CFF)
    val gradientMiddle = Color(0xFF8A4BFF)
    val gradientBottom = Color(0xFF6A2CFF)

    val headerDividerColor = PrimaryAccent.copy(alpha = 0.22f)
    val tagBg = Color(0xFFFFF5DF)
    val tagBorder = Color(0xFFFFC84D)
    val tagText = Color(0xFF7A3E00)

    var selectedTab by remember { mutableIntStateOf(0) }

    // heights
    val contentCardHeight = 520.dp
    val transcriptCardHeight = 460.dp

    // result card state (shown after pressing Tóm tắt)
    var showResultCard by remember { mutableStateOf(false) }

    // keep preview-provided values in sync (hiện tại không dùng resultTitle/resultContent nữa)
    LaunchedEffect(false) {
        showResultCard = false
    }

    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
//            NoteDetailHeader(
//                title = title,
//                onTitleChange = { viewModel.onTitleChanged(it) },
//                tagBg = tagBg,
//                tagBorder = tagBorder,
//                tagText = tagText,
//                gradientMiddle = gradientMiddle,
//                onBack = onBack,
//            )
            NoteDetailHeader(
                folderId = folderId,
                availableFolders = availableFolders,
                onFolderSelected = { folder -> viewModel.onFolderSelected(folder) },
                onSaveClick = {viewModel.onSaveNote()},
                onBack = onBack,
            )
        },
//        bottomBar = {
//            if (selectedTab == 0) {
//                NoteDetailBottomActions(
//                    onNormalize = { viewModel.onNormalizeClicked() },
//                    onSaveNote = { viewModel.onSaveNote() },
//                    onSummarize = { viewModel.onSummarizeClicked() },
//                    hasMindMap = state.mindMap != null,
//                    onGenerateOrShowMindMap = { viewModel.onGenerateMindMapClicked() },
//                )
//            } else {
//                Spacer(modifier = Modifier.height(0.dp))
//            }
//        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
//            Divider(thickness = 1.dp, color = Color(0xffE5E7EB))
//            // Folder selection row (giống NoteEditScreen -> NoteInfoAndActions)
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//            ) {
//                FolderSelectionButton(
//                    currentFolderId = folderId,
//                    availableFolders = availableFolders,
//                    onFolderSelected = { folder -> viewModel.onFolderSelected(folder) }
//                )
//            }
            // Divider + Tabs (fixed)
            HorizontalDivider(color = Color(0xffE5E7EB), thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            NoteInfoAndActions(
                isProcessing = state.isSummarizing,
                isNormalizing = state.isNormalizing,
                hasMindMap = state.mindMap != null,
                onSummarize = { viewModel.onSummarizeClicked() },
                onNormalize = { viewModel.onNormalizeClicked() },
                onMindMap = { viewModel.onGenerateMindMapClicked() }
            )
            HorizontalDivider(color = Color(0xffE5E7EB), thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            Column(
                modifier = Modifier
                    .padding(16.dp),
            ) {
                // --- Ô nhập Tiêu đề ---
                BasicTextField(
                    value = state.title,
                    onValueChange = { newTitle ->
                        viewModel.onTitleChanged(newTitle)
                    },
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    decorationBox = { innerTextField ->
                        if (state.title.isEmpty()) {
                            Text(
                                "Tiêu đề...",
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
                Text(
                    text = formatNoteDate(state.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )
            }
            HorizontalDivider(color = Color(0xffE5E7EB), thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.padding(horizontal = 24.dp)) {
                Column(modifier = Modifier.padding(end = 24.dp).clickable { selectedTab = 0 }) {
                    Text("Văn bản", style = TextStyle(fontSize = 14.sp, fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Medium, color = if (selectedTab == 0) gradientBottom else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
                    Spacer(modifier = Modifier.height(6.dp))
                    if (selectedTab == 0) Box(modifier = Modifier.height(3.dp).width(56.dp).clip(RoundedCornerShape(4.dp)).background(gradientBottom))
                }
                Column(modifier = Modifier.padding(end = 24.dp).clickable { selectedTab = 1 }) {
                    Text("Âm thanh", style = TextStyle(fontSize = 14.sp, fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Medium, color = if (selectedTab == 1) gradientBottom else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
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
                contentPadding = PaddingValues(12.dp)
            ) {
                item {
                    if (selectedTab == 0) {
                        // Simple non-scrollable card; LazyColumn handles scrolling
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .weight(1f)
                                .background(Color.White.copy(0.5f))
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            BasicTextField(
                                value = content,
                                onValueChange = { viewModel.onContentChanged(it) },
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.Justify
                                ),
                                decorationBox = { innerTextField ->
                                    if (content.isEmpty()) {
                                        Text(
                                            "Chỉnh sửa nội dung ghi chú...",
                                            fontSize = 16.sp,
                                            color = Color.Gray.copy(alpha = 0.5f)
                                        )
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            AudioDisplay(
                                isPlaying = isPlaying && hasValidAudio,
                                onTogglePlay = {
                                    if (hasValidAudio && player != null) {
                                        if (player.isPlaying) {
                                            player.pause()
                                        } else {
                                            player.playWhenReady = true
                                            player.play()
                                        }
                                    }
                                },
                                progress = if (hasValidAudio) progress else 0f,
                                totalSeconds = if (hasValidAudio) totalSeconds else 0,
                                gradientTop = gradientTop,
                                gradientMiddle = gradientMiddle,
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            TranscriptionDisplay(
                                chunks = chunks,
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
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
