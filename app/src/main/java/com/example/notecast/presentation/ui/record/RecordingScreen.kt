package com.example.notecast.presentation.ui.record

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.notecast.domain.model.AsrResult
import com.example.notecast.domain.model.Folder
import com.example.notecast.presentation.theme.Background
import com.example.notecast.presentation.theme.PrimaryAccent
import com.example.notecast.presentation.theme.Red
import com.example.notecast.presentation.ui.dialog.ProcessingDialog
import com.example.notecast.presentation.viewmodel.ASRState
import com.example.notecast.presentation.viewmodel.AudioViewModel
import com.example.notecast.presentation.viewmodel.RecorderState
import com.example.notecast.presentation.viewmodel.ASRViewModel
import com.example.notecast.presentation.viewmodel.FolderViewModel
import com.example.notecast.utils.formatElapsed
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import com.example.notecast.presentation.navigation.Screen

private const val TAG_RECORDING = "RecordingScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    navController: NavController,
    audioViewModel: AudioViewModel = hiltViewModel(),
    asrViewModel: ASRViewModel = hiltViewModel(),
    folderViewModel: FolderViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val recorderState by audioViewModel.recorderState.collectAsState()
    val durationMillis by audioViewModel.recordingDurationMillis.collectAsState()
    val waveform by audioViewModel.waveform.collectAsState()
    val vadState by audioViewModel.vadState.collectAsState()
    val amplitude by audioViewModel.amplitude.collectAsState()

    val asrState: ASRState by asrViewModel.state.collectAsState()

    // Reuse global folder state that already loads all folders
    val folderState by folderViewModel.state.collectAsState()
    val folders = folderState.folders

    var showProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(asrState) {
        when (val state = asrState) {
            ASRState.Idle -> { showProcessing = false }
            ASRState.Processing -> { showProcessing = true }
            is ASRState.Final -> {
                showProcessing = false

                val result: AsrResult = state.result
                val content = result.text
                // Backend duration (Double, giây)
                val backendDurationMs = (result.durationSec * 1000).toLong()

                val chunksJson: String? = if (result.chunks.isNotEmpty()) {
                    try {
                        Json.encodeToString(result.chunks).also {
                            Log.d(TAG_RECORDING, "Encoding chunks for nav: count=${result.chunks.size}, jsonLength=${it.length}")
                        }
                    } catch (t: Throwable) {
                        Log.e(TAG_RECORDING, "Failed to encode chunksJson: ${t.message}", t)
                        null
                    }
                } else null

                val audioPath = audioViewModel.currentRecordingFilePath
                if (audioPath != null) {
                    Log.d(
                        TAG_RECORDING,
                        "Calling saveVoiceNoteAndReturnId: path=$audioPath, durationMs=$backendDurationMs, textLength=${content.length}"
                    )
                    try {
                        val newId = asrViewModel.saveVoiceNoteAndReturnId(
                            title = "Ghi chú ghi âm",
                            transcript = content,
                            chunksJson = chunksJson,
                            audioFilePath = audioPath,
                            durationMs = backendDurationMs,
                            folderId = null,
                        )

                        Log.d(
                            TAG_RECORDING,
                            "Navigate to NoteDetailText: textLength=${content.length}, hasChunks=${chunksJson != null}, noteId='$newId'"
                        )

                        navController.navigate(
                            Screen.NoteDetailText.createRoute(newId)
                        ) {
                            popUpTo(Screen.Recording.route) { inclusive = true }
                        }
                    } catch (t: Throwable) {
                        Log.e(TAG_RECORDING, "Error saving voice note or navigating: ${t.message}", t)
                        Toast.makeText(
                            context,
                            "Không thể lưu ghi chú giọng nói",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.w(TAG_RECORDING, "saveVoiceNoteAndReturnId skipped because audioPath is null")
                }

                asrViewModel.resetSession()
            }
            is ASRState.Error -> {
                showProcessing = false
                Toast.makeText(context, state.msg, Toast.LENGTH_SHORT).show()
                asrViewModel.resetSession()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Toast.makeText(
            context,
            if (isGranted) "Quyền ghi âm đã được cấp" else "Quyền ghi âm bị từ chối",
            Toast.LENGTH_SHORT
        ).show()
        if (isGranted) {
            audioViewModel.startRecording()
        }
    }

    fun startWithPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            audioViewModel.startRecording()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun stopAndTranscribe() {
        Log.d(TAG_RECORDING, "stopAndTranscribe: invoked, vmState=$recorderState, durationMs=$durationMillis")
        coroutineScope.launch {
            // suspend until stopRecordingUseCase & repository update are done
            audioViewModel.stopRecording()
            val path = audioViewModel.currentRecordingFilePath
            Log.d(TAG_RECORDING, "stopAndTranscribe: path after stopRecording=${path}")
            if (path == null) {
                Log.e(TAG_RECORDING, "stopAndTranscribe: currentRecordingFilePath is null, show toast")
                Toast.makeText(context, "Không tìm thấy file ghi âm", Toast.LENGTH_SHORT).show()
                return@launch
            }
            asrViewModel.transcribeRecordingFile(path)
        }
    }

    var showMenu by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        if (recorderState == RecorderState.Recording || recorderState == RecorderState.Paused) {
                            showExitConfirm = true
                        } else {
                            onClose()
                        }
                    }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.AddBox,
                                contentDescription = "Thêm",
                                tint = PrimaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            offset = DpOffset(x = (-8).dp, y = 8.dp)
                        ) {
                            folders.forEach { folder: Folder ->
                                DropdownMenuItem(
                                    text = { Text(folder.name) },
                                    onClick = {
                                        // TODO: gắn logic chọn folder cho bản ghi hiện tại (nếu cần)
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatElapsed(durationMillis),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                when (recorderState) {
                    RecorderState.Recording -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Đang ghi âm...",
                                color = Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Red)
                            )
                        }
                    }
                    RecorderState.Paused -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Tạm dừng",
                                color = Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Red.copy(alpha = 0.6f))
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = "Nhấn nút ghi âm để bắt đầu thu âm",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryAccent.copy(alpha = 0.08f))
            ) {
                WaveformVisualizer(
                    waveform = waveform,
                    vad = vadState,
                    amplitude = amplitude,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val idleOuterSize = 80.dp
                val idleInnerSize = 56.dp
                val actionOuterSize = 64.dp
                val actionIconSize = 22.dp
                val actionGap = 12.dp

                when (recorderState) {
                    RecorderState.Idle -> {
                        val infiniteTransition = rememberInfiniteTransition(label = "idle-pulse")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.06f,
                            animationSpec = infiniteRepeatable(
                                animation = keyframes {
                                    this.durationMillis = 900
                                    1.06f at 450 using FastOutSlowInEasing
                                },
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "idle-scale"
                        )

                        FloatingActionButton(
                            onClick = { startWithPermission() },
                            modifier = Modifier
                                .size(idleOuterSize)
                                .scale(scale)
                                .shadow(12.dp, CircleShape),
                            containerColor = Color(0xFFF04C4C),
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(idleInnerSize)
                                    .background(Color.White, CircleShape)
                            )
                        }
                    }
                    RecorderState.Recording -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 56.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FloatingActionButton(
                                onClick = { audioViewModel.pauseRecording() },
                                modifier = Modifier
                                    .size(actionOuterSize)
                                    .shadow(8.dp, CircleShape),
                                containerColor = Red,
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = "Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(actionIconSize)
                                )
                            }
                            Spacer(modifier = Modifier.width(actionGap))
                            FloatingActionButton(
                                onClick = {
                                    Log.d(TAG_RECORDING, "Stop & process clicked, durationMs=$durationMillis")
                                    stopAndTranscribe()
                                },
                                modifier = Modifier
                                    .size(actionOuterSize)
                                    .shadow(8.dp, CircleShape),
                                containerColor = Color.DarkGray,
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = Color.White,
                                    modifier = Modifier.size(actionIconSize)
                                )
                            }
                        }
                    }
                    RecorderState.Paused -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 56.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FloatingActionButton(
                                onClick = { audioViewModel.resumeRecording() },
                                modifier = Modifier
                                    .size(actionOuterSize)
                                    .shadow(8.dp, CircleShape),
                                containerColor = Red,
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Resume",
                                    tint = Color.White,
                                    modifier = Modifier.size(actionIconSize)
                                )
                            }
                            Spacer(modifier = Modifier.width(actionGap))
                            FloatingActionButton(
                                onClick = {
                                    Log.d(TAG_RECORDING, "Stop & process clicked from Paused, durationMs=$durationMillis")
                                    stopAndTranscribe()
                                },
                                modifier = Modifier
                                    .size(actionOuterSize)
                                    .shadow(8.dp, CircleShape),
                                containerColor = PrimaryAccent,
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save",
                                    tint = Color.White,
                                    modifier = Modifier.size(actionIconSize)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (durationMillis > 0L) {
                        "Bấm biểu tượng thêm (góc phải) để chọn thư mục lưu (UI stub)"
                    } else {
                        "Nhấn nút ghi âm để bắt đầu thu âm"
                    },
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showProcessing) {
            ProcessingDialog(onDismissRequest = { }, percent = 0, step = 1)
        }

        ExitConfirm(
            visible = showExitConfirm,
            title = "Dừng ghi âm?",
            message = "Bạn đang ghi âm, thoát ra sẽ mất đoạn ghi hiện tại.",
            confirmText = "Thoát",
            dismissText = "Hủy",
            onConfirm = {
                coroutineScope.launch {
                    audioViewModel.stopRecording()
                    showExitConfirm = false
                    onClose()
                }
            },
            onDismiss = { showExitConfirm = false }
        )
    }
}