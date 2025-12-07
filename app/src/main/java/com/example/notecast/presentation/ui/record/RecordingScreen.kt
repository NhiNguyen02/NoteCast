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
import com.example.notecast.presentation.theme.Background
import com.example.notecast.presentation.theme.PrimaryAccent
import com.example.notecast.presentation.theme.Red
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
    // Giữ callback cũ để trả transcript + info, nhưng UI mới chủ yếu quan tâm lưu note
    onRecordingFinished: (transcript: String, audioFilePath: String?, durationMs: Long, sampleRate: Int, channels: Int) -> Unit = { _, _, _, _, _ -> },
) {
    Log.d(TAG_RECORDING, "Composable entered")
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // observe state from ViewModel (giữ nguyên logic cũ)
    val recorderState by viewModel.recorderState.collectAsState()
    val durationMillis by viewModel.recordingDurationMillis.collectAsState()
    val waveform by viewModel.waveform.collectAsState()
    val vadState by viewModel.vadState.collectAsState()
    val amplitude by viewModel.amplitude.collectAsState()

    val asrVm: ASRViewModel = hiltViewModel()
    val asrState by asrVm.state.collectAsState()

    val showProcessing = remember { mutableStateOf(false) }
    val latestOnRecordingFinished by rememberUpdatedState(onRecordingFinished)

    // use var with delegation so we can reassign within lambdas and LaunchedEffect
    var isAsrInitialized by remember { mutableStateOf(false) }
    var isInitializingAsr by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Log.d(TAG_RECORDING, "attachAsrViewModel")
        viewModel.attachAsrViewModel(asrVm)
    }

    // Khi ASR hoàn tất (Final) hoặc Error, ẩn dialog và điều hướng.
    // KHÔNG tự bật dialog khi chỉ có ASRState.Processing trong lúc vẫn đang ghi âm.
    LaunchedEffect(asrState) {
        Log.d(TAG_RECORDING, "ASR state changed: $asrState")
        when (val state = asrState) {
            is ASRState.Final -> {
                Log.d(TAG_RECORDING, "ASR Final text length=${state.text.length}")
                showProcessing.value = false
                latestOnRecordingFinished(
                    state.text,
                    null,
                    durationMillis,
                    16_000,
                    1,
                )
                asrVm.resetSession()
            }

            is ASRState.Error -> {
                Log.e(TAG_RECORDING, "ASR Error: ${state.msg}")
                showProcessing.value = false
                Toast.makeText(context, state.msg, Toast.LENGTH_SHORT).show()
            }

            else -> Unit
        }
    }
//    LaunchedEffect(asrState) {
//        Log.d(TAG_RECORDING, "ASR state changed: $asrState")
//        when (val state = asrState) {
//            is ASRState.Final -> {
//                showProcessing.value = false
//
//                val content = state.text.ifBlank {
//                    "dạ thưa hội đồng em xin phép đi vào phần kết quả thực nghiệm của đề tài như thầy cô thấy trên biểu đồ mô hình đạt độ chính xác khoảng tám mươi tám phần trăm tuy nhiên khi test với dữ liệu bị nhiễu độ chính xác giảm xuống còn bảy mươi lăm phần trăm nguyên nhân chủ yếu là do bộ dữ liệu ban đầu chưa đa dạng giọng vùng miền em đã thử áp dụng kỹ thuật data augmentation để tăng cường dữ liệu kết quả sau cải thiện model đã nhận diện tốt hơn các từ đơn nhưng câu dài vẫn còn sai sót ứng dụng demo trên điện thoại chạy ổn định các tính năng cơ bản như đăng nhập còn phần xử lý thời gian thực vẫn còn độ trễ khoảng hai giây em sẽ tối ưu sau đó là toàn bộ kết quả của chương ba em xin cảm ơn thầy cô đã lắng nghe em xin mời quý thầy cô đặt câu hỏi phản biện ạ"
//                }
//
//                latestOnRecordingFinished(
//                    content,
//                    null,
//                    durationMillis,
//                    16_000,
//                    1,
//                )
//                asrVm.resetSession()
//            }
//            is ASRState.Error -> {
//                Log.e(TAG_RECORDING, "ASR Error (suppressed to UI): ${state.msg}")
//                showProcessing.value = false
//            }
//            else -> Unit
//        }
//    }

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
            if (showProcessing.value || isInitializingAsr) {
                showProcessing.value = false
                isInitializingAsr = false
            }
            Log.d(TAG_RECORDING, "startRecording after permission grant")
            viewModel.startRecording()
        } else {
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

        if (!isAsrInitialized) {
            isInitializingAsr = true
            showProcessing.value = true
        }

        if (hasPermission) {
            if (!isAsrInitialized) {
                coroutineScope.launch {
                    Log.d(TAG_RECORDING, "First-time ASR init: waiting briefly before startRecording")
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

    // --------- UI state mới theo thiết kế cập nhật ---------
    var showMenu by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }

    // VAD label thân thiện
//    val vadLabel = when (vadState) {
//        VadState.SILENT -> "Không có tiếng"
//        VadState.SPEAKING -> "Có tiếng"
//        else -> vadState.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
//    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Background)
//            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { /* compact title, để trống cho tối giản */ },
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
                            // Hiện tại chỉ là placeholder UI, chưa gắn logic thư mục
                            DropdownMenuItem(
                                text = { Text("Công việc") },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Cá nhân") },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Ý tưởng") },
                                onClick = { showMenu = false }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Timer + trạng thái
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

            // Waveform card với overlay info
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

//                Column(
//                    modifier = Modifier
//                        .align(Alignment.TopStart)
//                        .padding(8.dp)
//                ) {
//                    Text(
//                        text = "Biên độ: ${"%.2f".format(amplitude)}",
//                        color = Color.White,
//                        fontSize = 12.sp
//                    )
//                    Text(
//                        text = "VAD: $vadLabel",
//                        color = Color.White,
//                        fontSize = 12.sp
//                    )
//                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom controls (fab + hint)
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
                                onClick = { viewModel.pauseRecording() },
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
                                    viewModel.stopRecording()
                                    showProcessing.value = true
                                    asrVm.finishSession(
                                        audioFilePath = viewModel.currentRecordingFilePath ?: "", // TODO: real path when available
                                        durationMs = durationMillis,
                                        sampleRate = viewModel.sampleRate,
                                        channels = viewModel.channels,
                                    )
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
                                onClick = { viewModel.resumeRecording() },
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
                                    viewModel.stopRecording()
                                    showProcessing.value = true
                                    asrVm.finishSession(
                                        audioFilePath = viewModel.currentRecordingFilePath ?: "", // TODO
                                        durationMs = durationMillis,
                                        sampleRate = viewModel.sampleRate,
                                        channels = viewModel.channels,
                                    )
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

        if (showProcessing.value || isInitializingAsr) {
            ProcessingDialog(
                percent = 50,
                step = 1,
                details = if (isInitializingAsr) "Đang chuẩn bị mô hình nhận diện giọng nói..." else "Đang chuyển giọng nói thành văn bản...",
                onDismissRequest = {
                    showProcessing.value = false
                    isInitializingAsr = false
                }
            )
        }

        if (showExitConfirm) {
            AlertDialog(
                onDismissRequest = { showExitConfirm = false },
                title = { Text("Dừng ghi âm?") },
                text = { Text("Bạn đang ghi âm, thoát ra sẽ mất đoạn ghi hiện tại.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.stopRecording()
                        showExitConfirm = false
                        onClose()
                    }) {
                        Text("Thoát")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitConfirm = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}