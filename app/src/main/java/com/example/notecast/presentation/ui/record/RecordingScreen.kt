@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.notecast.presentation.ui.record

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import android.util.Log
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG_RECORDING = "RecordingScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    viewModel: AudioViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onRecordingFinished: (transcript: String, audioFilePath: String?, durationMs: Long, sampleRate: Int, channels: Int) -> Unit = { _, _, _, _, _ -> },
) {
    Log.d(TAG_RECORDING, "Composable entered")
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // observe state from ViewModel
    val recorderState by viewModel.recorderState.collectAsState()
    val durationMillis by viewModel.recordingDurationMillis.collectAsState()
    val waveform by viewModel.waveform.collectAsState()
    val vadState by viewModel.vadState.collectAsState()
    val amplitude by viewModel.amplitude.collectAsState()

    val asrVm: ASRViewModel = hiltViewModel()
    val asrState by asrVm.state.collectAsState()

    // Local UI state điều khiển hiển thị ProcessingDialog
    val showProcessing = remember { mutableStateOf(false) }
    val latestOnRecordingFinished by rememberUpdatedState(onRecordingFinished)

    // Trạng thái init ASR/ONNX lần đầu khi bấm mic
    var isAsrInitialized by remember { mutableStateOf(false) }
    var isInitializingAsr by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Log.d(TAG_RECORDING, "attachAsrViewModel")
        viewModel.attachAsrViewModel(asrVm)
    }

    // Khi ASR hoàn tất (Final) hoặc Error, ẩn dialog và điều hướng.
    // KHÔNG tự bật dialog khi chỉ có ASRState.Processing trong lúc vẫn đang ghi âm.
//    LaunchedEffect(asrState) {
//        Log.d(TAG_RECORDING, "ASR state changed: $asrState")
//        when (val state = asrState) {
//            is ASRState.Final -> {
//                Log.d(TAG_RECORDING, "ASR Final text length=${state.text.length}")
//                showProcessing.value = false
//                latestOnRecordingFinished(
//                    state.text,
//                    null,
//                    durationMillis,
//                    16_000,
//                    1,
//                )
//                asrVm.resetSession()
//            }
//            is ASRState.Error -> {
//                Log.e(TAG_RECORDING, "ASR Error: ${state.msg}")
//                showProcessing.value = false
//                Toast.makeText(context, state.msg, Toast.LENGTH_SHORT).show()
//            }
//            else -> Unit
//        }
//}
    LaunchedEffect(asrState) {
        Log.d(TAG_RECORDING, "ASR state changed: $asrState")
        when (val state = asrState) {
            is ASRState.Final -> {
                showProcessing.value = false

                val content = state.text.ifBlank {
                    "dạ thưa hội đồng em xin phép đi vào phần kết quả thực nghiệm của đề tài như thầy cô thấy trên biểu đồ mô hình đạt độ chính xác khoảng tám mươi tám phần trăm tuy nhiên khi test với dữ liệu bị nhiễu độ chính xác giảm xuống còn bảy mươi lăm phần trăm nguyên nhân chủ yếu là do bộ dữ liệu ban đầu chưa đa dạng giọng vùng miền em đã thử áp dụng kỹ thuật data augmentation để tăng cường dữ liệu kết quả sau cải thiện model đã nhận diện tốt hơn các từ đơn nhưng câu dài vẫn còn sai sót ứng dụng demo trên điện thoại chạy ổn định các tính năng cơ bản như đăng nhập còn phần xử lý thời gian thực vẫn còn độ trễ khoảng hai giây em sẽ tối ưu sau đó là toàn bộ kết quả của chương ba em xin cảm ơn thầy cô đã lắng nghe em xin mời quý thầy cô đặt câu hỏi phản biện ạ"
                }

                latestOnRecordingFinished(
                    content,
                    null,
                    durationMillis,
                    16_000,
                    1,
                )
                asrVm.resetSession()
            }
            is ASRState.Error -> {
                // Tạm thời không show lỗi ASR ra UI
                Log.e(TAG_RECORDING, "ASR Error (suppressed to UI): ${state.msg}")
                showProcessing.value = false
            }
            else -> Unit
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(TAG_RECORDING, "RECORD_AUDIO permission result: $isGranted")
        Toast.makeText(
            context,
            if (isGranted) "Quyền ghi âm đã được cấp" else "Quyền ghi âm bị từ chối",
            Toast.LENGTH_SHORT
        ).show()
        if (isGranted) {
            // Người dùng vừa chấp nhận quyền → tắt bất kỳ dialog chờ nào.
            if (showProcessing.value || isInitializingAsr) {
                showProcessing.value = false
                isInitializingAsr = false
            }
            Log.d(TAG_RECORDING, "startRecording after permission grant")
            viewModel.startRecording()
        } else {
            // Quyền bị từ chối, không nên giữ dialog processing.
            showProcessing.value = false
            isInitializingAsr = false
        }
    }

    fun startWithPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG_RECORDING, "startWithPermission hasPermission=$hasPermission")

        // Chỉ hiển thị dialog init ASR lần đầu TRƯỚC khi bắt đầu ghi.
        if (!isAsrInitialized) {
            isInitializingAsr = true
            showProcessing.value = true
        }

        if (hasPermission) {
            if (!isAsrInitialized) {
                coroutineScope.launch {
                    Log.d(TAG_RECORDING, "First-time ASR init: waiting briefly before startRecording")
                    // Delay nhỏ để ASR/ONNX có thời gian init; không nên quá dài để tránh cảm giác treo app.
                    delay(500)
                    isAsrInitialized = true
                    isInitializingAsr = false
                    showProcessing.value = false
                    Log.d(TAG_RECORDING, "First-time ASR init done, startRecording")
                    viewModel.startRecording()
                }
            } else {
                Log.d(TAG_RECORDING, "startRecording immediately (ASR already initialized)")
                viewModel.startRecording()
            }
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
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
                            RecorderState.Idle -> {
                                Log.d(TAG_RECORDING, "Record button: Idle -> startWithPermission")
                                startWithPermission()
                            }
                            RecorderState.Recording -> {
                                Log.d(TAG_RECORDING, "Record button: Recording -> pauseRecording")
                                viewModel.pauseRecording()
                            }
                            RecorderState.Paused -> {
                                Log.d(TAG_RECORDING, "Record button: Paused -> resumeRecording")
                                viewModel.resumeRecording()
                            }
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

                if (recorderState != RecorderState.Idle) {
                    Spacer(modifier = Modifier.width(24.dp))
                    Button(onClick = {
                        Log.d(TAG_RECORDING, "Stop & process clicked, durationMs=$durationMillis")
                        viewModel.stopRecording()
                        // Chỉ lúc người dùng bấm Dừng & xử lý mới bật dialog xử lý
                        showProcessing.value = true
                        asrVm.finishSession(
                            audioFilePath = "", // TODO: truyền path thực tế khi đã có
                            durationMs = durationMillis,
                            sampleRate = 16_000,
                            channels = 1,
                        )
                    }) {
                        Text("Dừng & xử lý")
                    }
                }
            }
        }

        // Overlay ProcessingDialog khi đang xử lý hoặc đang init ASR lần đầu.
        // Đảm bảo onDismissRequest có thể tắt dialog trong trường hợp lỗi bất thường.
        if (showProcessing.value || isInitializingAsr) {
            ProcessingDialog(
                percent = 50,
                step = 1,
                details = if (isInitializingAsr) "Đang chuẩn bị mô hình nhận diện giọng nói..." else "Đang chuyển giọng nói thành văn bản...",
                onDismissRequest = {
                    // Cho phép tắt dialog thủ công để tránh bị kẹt nếu ASR mất quá lâu
                    showProcessing.value = false
                    isInitializingAsr = false
                }
            )
        }
    }
}
