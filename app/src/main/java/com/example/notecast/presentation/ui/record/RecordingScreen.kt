@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.notecast.presentation.ui.record

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notecast.presentation.theme.Background
import com.example.notecast.presentation.theme.PrimaryAccent
import com.example.notecast.presentation.ui.dialog.ProcessingDialog
import com.example.notecast.presentation.viewmodel.ASRState
import com.example.notecast.presentation.viewmodel.ASRViewModel
import com.example.notecast.presentation.viewmodel.AudioViewModel
import com.example.notecast.presentation.viewmodel.RecorderState
import com.example.notecast.utils.formatElapsed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    viewModel: AudioViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onRecordingFinished: (noteId: String) -> Unit = {},
) {
    val context = LocalContext.current

    // observe state from ViewModel
    val recorderState by viewModel.recorderState.collectAsState()
    val durationMillis by viewModel.recordingDurationMillis.collectAsState()
    val waveform by viewModel.waveform.collectAsState()
    val vadState by viewModel.vadState.collectAsState()
    val amplitude by viewModel.amplitude.collectAsState()

    val asrVm: ASRViewModel = hiltViewModel()
    val asrState by asrVm.state.collectAsState()

    // Local UI state để điều khiển hiển thị ProcessingDialog
    val showProcessing = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.attachAsrViewModel(asrVm)
    }

    // Khi ASR hoàn tất (Final), ẩn dialog và điều hướng sang màn Edit
    LaunchedEffect(asrState) {
        if (asrState is ASRState.Final) {
            showProcessing.value = false
            // TODO: thay "new" bằng noteId thực tế nếu sau này bạn tạo Note từ kết quả ASR
            onRecordingFinished("new")
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
        if (isGranted) viewModel.startRecording()
    }

    fun startWithPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasPermission) viewModel.startRecording() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Background)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (recorderState) {
                        RecorderState.Recording -> "Đang ghi âm"
                        RecorderState.Paused -> "Tạm dừng ghi âm"
                        RecorderState.Idle -> "Ghi âm"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Timer hiển thị khi không Idle
                if (recorderState != RecorderState.Idle) {
                    Text(
                        text = formatElapsed(durationMillis),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Nhấn nút micro để bắt đầu ghi âm.",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Waveform visualizer khu vực giữa
            WaveformVisualizer(
                waveform = waveform,
                vad = vadState,
                amplitude = amplitude,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        when (recorderState) {
                            RecorderState.Idle -> startWithPermission()
                            RecorderState.Recording -> viewModel.pauseRecording()
                            RecorderState.Paused -> viewModel.resumeRecording()
                        }
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            when (recorderState) {
                                RecorderState.Recording -> Color.Red
                                RecorderState.Paused -> Color.Gray
                                RecorderState.Idle -> Color.Red
                            }
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Record",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Khi đang ghi hoặc tạm dừng, hiển thị nút Stop để kết thúc session ASR
                if (recorderState != RecorderState.Idle) {
                    Spacer(modifier = Modifier.width(24.dp))
                    Button(onClick = {
                        // Dừng ghi âm và bắt đầu xử lý ASR
                        viewModel.stopRecording()
                        showProcessing.value = true
                        asrVm.finishSession()
                    }) {
                        Text("Dừng & xử lý")
                    }
                }
            }
        }

        // Overlay ProcessingDialog khi đang xử lý
        if (showProcessing.value && asrState is ASRState.Processing) {
            ProcessingDialog(
                percent = 50, // TODO: nếu sau này có progress thật thì binding vào đây
                step = 2,
                details = "Đang chuyển giọng nói thành văn bản...",
                onDismissRequest = { /* không cho đóng tay trong lúc xử lý */ }
            )
        }
    }
}
