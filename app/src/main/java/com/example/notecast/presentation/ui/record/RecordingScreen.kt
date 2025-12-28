package com.example.notecast.presentation.ui.record

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.notecast.presentation.navigation.Screen
import com.example.notecast.presentation.viewmodel.AudioViewModel
import com.example.notecast.presentation.viewmodel.RecorderState
import com.example.notecast.presentation.theme.Background
import com.example.notecast.presentation.theme.PrimaryAccent
import com.example.notecast.presentation.theme.Red
import com.example.notecast.presentation.ui.dialog.ProcessingDialog
import com.example.notecast.presentation.viewmodel.RecordingViewModel
import com.example.notecast.utils.formatElapsed
import kotlinx.coroutines.launch

private const val TAG_RECORDING = "RecordingScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    navController: NavController,
    audioViewModel: AudioViewModel = hiltViewModel(),
    recordingViewModel: RecordingViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val recorderState by audioViewModel.recorderState.collectAsState()
    val durationMillis by audioViewModel.recordingDurationMillis.collectAsState()
    val waveform by audioViewModel.waveform.collectAsState()
    val vadState by audioViewModel.vadState.collectAsState()
    val amplitude by audioViewModel.amplitude.collectAsState()

    var showProcessing by remember { mutableStateOf(false) }

    // Khi dừng ghi và xử lý xong (ở đây chỉ stop local), bạn có thể điều hướng hoặc đơn giản quay lại

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

    fun stopRecordingAndFinish() {
        Log.d(TAG_RECORDING, "stopRecordingAndFinish: invoked, state=$recorderState, durationMs=$durationMillis")
        coroutineScope.launch {
            audioViewModel.stopRecording()
            val path = audioViewModel.currentRecordingFilePath
            Log.d(TAG_RECORDING, "stopRecordingAndFinish: path=$path")
            if (path == null) {
                Toast.makeText(context, "Không tìm thấy file ghi âm", Toast.LENGTH_SHORT).show()
                return@launch
            }
            showProcessing = true
            try {
                val noteId = recordingViewModel.transcribeRecording(path, userId = null)
                // Điều hướng tới màn chi tiết voice note theo route của Screen.NoteAudio
                navController.navigate(Screen.NoteAudio.createRoute(noteId))
            } catch (t: Throwable) {
                Log.e(TAG_RECORDING, "Error transcribing recording: ${t.message}", t)
                Toast.makeText(context, "Lỗi gửi ghi âm: ${t.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showProcessing = false
            }
        }
    }

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
                            modifier = Modifier.size(24.dp)
                        )
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
                            color = Color.Gray.copy(alpha = 0.7f),
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
                    .padding(horizontal = 16.dp)
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
                                    Log.d(TAG_RECORDING, "Stop clicked, durationMs=$durationMillis")
                                    stopRecordingAndFinish()
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
                                    Log.d(TAG_RECORDING, "Stop clicked from Paused, durationMs=$durationMillis")
                                    stopRecordingAndFinish()
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
                        "Đã ghi âm xong, bấm nút dừng để lưu bản ghi."
                    } else {
                        "Nhấn nút ghi âm để bắt đầu thu âm."
                    },
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showProcessing) {
            ProcessingDialog(
                title = "Đang gửi ghi âm và yêu cầu chuyển đổi thành giọng nói...",
                onDismissRequest = { },
            )
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