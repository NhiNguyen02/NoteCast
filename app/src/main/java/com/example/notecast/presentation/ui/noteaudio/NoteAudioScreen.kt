package com.example.notecast.presentation.ui.noteaudio

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.compose.ui.platform.LocalContext
import com.example.notecast.presentation.theme.*
import com.example.notecast.presentation.viewmodel.NoteAudioViewModel
import com.example.notecast.presentation.ui.dialog.SavedNotificationDialog
import com.example.notecast.presentation.ui.mindmap.MindMapDialog
import com.example.notecast.presentation.ui.common_components.NoteInfoAndActions
import com.example.notecast.utils.formatNoteDate
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteAudioScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteAudioViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    val folderId = state.folderId
    val availableFolders = state.availableFolders

    // --- Audio info từ NoteDomain.audio ---
    val audio = state.audio
    val durationSec = audio?.durationSec ?: 0.0
    val totalSeconds = remember(durationSec) { durationSec.toInt().coerceAtLeast(0) }

    // Ưu tiên localFilePath, fallback sang cloudUrl
    val audioUri: String? = remember(audio) {
        val local = audio?.localFilePath
        val cloud = audio?.cloudUrl
        when {
            !local.isNullOrBlank() -> {
                Log.d("NoteAudioScreen", "Using localFilePath: $local")
                local
            }
            !cloud.isNullOrBlank() -> {
                Log.d("NoteAudioScreen", "Using cloudUrl: $cloud")
                cloud
            }
            else -> {
                Log.d("NoteAudioScreen", "No audio URI available")
                null
            }
        }
    }

    val hasValidAudio = !audioUri.isNullOrBlank() && totalSeconds > 0

    val context = LocalContext.current
    val player: ExoPlayer? = remember(audioUri) {
        if (audioUri.isNullOrBlank()) {
            Log.d("NoteAudioScreen", "No audioUri, player = null")
            null
        } else {
            Log.d("NoteAudioScreen", "Creating ExoPlayer for: $audioUri")
            try {
                ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(audioUri))
                    prepare()
                    Log.d("NoteAudioScreen", "ExoPlayer prepared successfully")
                }
            } catch (e: Exception) {
                Log.e("NoteAudioScreen", "Error creating ExoPlayer", e)
                null
            }
        }
    }

    DisposableEffect(player) {
        onDispose {
            Log.d("NoteAudioScreen", "Releasing player")
            player?.release()
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) } // 0f..1f

    LaunchedEffect(state.noteId, audioUri) {
        Log.d("NoteAudioScreen", "Note/audioUri changed, resetting playback state")
        isPlaying = false
        progress = 0f
    }

    LaunchedEffect(player, durationSec) {
        if (player == null) {
            Log.d("NoteAudioScreen", "No player in progress tracker")
            return@LaunchedEffect
        }
        while (true) {
            delay(200L)
            val dMs = player.duration.takeIf { it > 0 } ?: (durationSec * 1000).toLong()
            if (dMs > 0) {
                val ratio = (player.currentPosition.toFloat() / dMs.toFloat()).coerceIn(0f, 1f)
                progress = ratio
            }
            isPlaying = player.isPlaying
            if (!player.isPlaying && player.playbackState == ExoPlayer.STATE_ENDED) {
                progress = 1f
            }
        }
    }

    LaunchedEffect(audioUri, durationSec, hasValidAudio) {
        Log.d(
            "NoteAudioScreen",
            "Audio state: uri=$audioUri, duration=$durationSec sec, hasValid=$hasValidAudio, chunks=${audio?.chunks?.size ?: 0}"
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Văn bản", "Âm thanh", "Tóm tắt", "Từ khóa", "Mindmap")
    val tabScrollState = rememberScrollState()
    var showMindmapDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                NoteDetailHeader(
                    folderId = folderId,
                    availableFolders = availableFolders,
                    onFolderSelected = { folder -> viewModel.onFolderSelected(folder) },
                    onSaveClick = { viewModel.onSaveNote() },
                    onBack = onBack,
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
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

                NoteInfoAndActions(
                    isProcessing = state.isProcessing,
                    hasMindMap = state.mindMap != null,
                    currentStep = state.currentStep,
                    onRegenerateAll = { viewModel.onRegenerateAllClicked() },
                )

                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    BasicTextField(
                        value = state.title,
                        onValueChange = { newTitle -> viewModel.onTitleChanged(newTitle) },
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Transparent
                        ),
                        cursorBrush = TitleBrush,
                        decorationBox = { innerTextField ->
                            Box {
                                if (state.title.isEmpty()) {
                                    Text(
                                        text = "Tiêu đề...",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray.copy(alpha = 0.5f)
                                    )
                                } else {
                                    Text(
                                        text = state.title,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        style = TextStyle(brush = TitleBrush)
                                    )
                                }
                                innerTextField()
                            }
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

                HorizontalDivider(
                    color = Color(0xffE5E7EB),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))

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
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (selectedTab == index) SubTitleColor else MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.8f
                                    )
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (selectedTab == index) Box(
                                modifier = Modifier
                                    .height(3.dp)
                                    .width(56.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SubTitleColor)
                            )
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
                                val textToShow = state.normalizedText ?: state.rawText ?: ""

                                if (textToShow.isEmpty()) {
                                    Text(
                                        "Đang xử lý transcript...",
                                        color = Color.Gray
                                    )
                                } else {
                                    Text(
                                        text = textToShow,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        textAlign = TextAlign.Justify
                                    )
                                }
                            }
                        }

                        // ===== TAB ÂM THANH =====
                        1 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(12.dp)
                            ) {
                                AudioDisplay(
                                    isPlaying = isPlaying && hasValidAudio,
                                    onTogglePlay = {
                                        Log.d(
                                            "NoteAudioScreen",
                                            "Toggle play clicked. hasValidAudio=$hasValidAudio, player=$player"
                                        )
                                        if (hasValidAudio && player != null) {
                                            if (player.isPlaying) {
                                                Log.d("NoteAudioScreen", "Pausing player")
                                                player.pause()
                                            } else {
                                                Log.d("NoteAudioScreen", "Starting playback")
                                                player.playWhenReady = true
                                                player.play()
                                            }
                                        } else {
                                            Log.w(
                                                "NoteAudioScreen",
                                                "Cannot play: hasValidAudio=$hasValidAudio, player=$player"
                                            )
                                        }
                                    },
                                    progress = if (hasValidAudio) progress else 0f,
                                    totalSeconds = if (hasValidAudio) totalSeconds else 0,
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                TranscriptionDisplay(
                                    chunks = audio?.chunks ?: emptyList(),
                                    currentTimeSec = if (hasValidAudio && player != null)
                                        player.currentPosition / 1000.0 else null,
                                    onChunkClick = { chunk ->
                                        if (hasValidAudio && player != null) {
                                            player.seekTo((chunk.start * 1000).toLong())
                                            player.playWhenReady = true
                                            player.play()
                                        }
                                    }
                                )
                            }
                        }

                        // ===== TAB TÓM TẮT =====
                        2 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(0.5f))
                                    .verticalScroll(rememberScrollState())
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = state.summary ?: "Chưa có tóm tắt.",
                                    color = Color.Black
                                )
                            }
                        }

                        // ===== TAB TỪ KHÓA =====
                        3 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                            ) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    state.keywords.forEach {
                                        AssistChip(onClick = {}, label = { Text(it) })
                                    }
                                }
                            }
                        }

                        // ===== TAB MINDMAP =====
                        4 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    "Nhấn để xem mindmap chi tiết",
                                    color = SubTitleColor,
                                    modifier = Modifier.clickable { showMindmapDialog = true }
                                )
                            }
                        }
                    }
                }
            }

            // ===== DIALOGS - Hiển thị overlay toàn màn hình =====
            if (state.showSavedDialog) {
                SavedNotificationDialog(onDismissRequest = { viewModel.clearSavedDialog() })
            }

            if (selectedTab == 4 && state.mindMap != null && showMindmapDialog) {
                MindMapDialog(
                    rootNode = state.mindMap!!,
                    onDismiss = { showMindmapDialog = false }
                )
            }
        }
    }
}
