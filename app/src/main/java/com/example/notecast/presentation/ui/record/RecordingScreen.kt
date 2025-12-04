@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.notecast.presentation.ui.record

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notecast.domain.vad.VadState
import com.example.notecast.presentation.theme.*
import com.example.notecast.presentation.ui.dialog.ProcessingDialog
import com.example.notecast.presentation.viewmodel.AudioViewModel
import com.example.notecast.utils.formatElapsed
// Xóa import NavController vì không dùng nữa

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    viewModel: AudioViewModel = hiltViewModel(),
    // SỬA 1: Thay đổi callback để trả về noteId
    onRecordingFinished: (String) -> Unit = {},
    onClose: () -> Unit = {}, // Callback đóng màn hình
    // Xóa navController và onSaveFile cũ vì không cần thiết
    onBack: (() -> Unit)? = null, // Giữ lại để tương thích nếu muốn
) {
    val context = LocalContext.current
    val effectiveOnBack = onBack ?: onClose // Sửa lại logic back

    // Single source of truth: AudioRepository / ViewModel state
    val recordingState by viewModel.recordingState.collectAsState()
    val processing by viewModel.processing.collectAsState()
    val processingPercent by viewModel.processingPercent.collectAsState()
    val amplitude by viewModel.amplitude.collectAsState(initial = 0f)
    val vadState by viewModel.vadState.collectAsState(initial = VadState.SILENT)
    val waveform by viewModel.waveform.collectAsState(initial = emptyList())
    val bufferAvailable by viewModel.bufferAvailableSamples.collectAsState(initial = 0)

    // KHÔNG CẦN coroutineScope nữa vì ViewModel tự xử lý scope
    // val coroutineScope = rememberCoroutineScope()

    var elapsedMs by remember { mutableLongStateOf(0L) }
    var showMenu by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Toast.makeText(context, if (isGranted) "Quyền ghi âm đã được cấp" else "Quyền ghi âm bị từ chối", Toast.LENGTH_SHORT).show()
        if (isGranted) viewModel.startRecording() // Tự động start nếu cấp quyền
    }

    // human-readable VAD label in Vietnamese
    val vadLabel = when (vadState) {
        VadState.SILENT -> "Không có tiếng"
        VadState.SPEAKING -> "Có tiếng"
        else -> vadState.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
    }

    // Timer: chạy dựa trên recordingState
    LaunchedEffect(recordingState) {
        if (recordingState == RecordingState.Recording) {
            // Reset timer logic để chính xác hơn (dùng diff time)
            val startTime = System.currentTimeMillis() - elapsedMs
            while (true) {
                elapsedMs = System.currentTimeMillis() - startTime
                delay(100L) // Update nhanh hơn cho mượt
                if (viewModel.recordingState.value != RecordingState.Recording) break
            }
        } else if (recordingState == RecordingState.Idle) {
            elapsedMs = 0L
        }
    }

    // Recording control handlers
    val startRecording: () -> Unit = {
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) viewModel.startRecording() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // SỬA 2: Logic STOP và SAVE gộp vào hàm handleSave
    val handleSave: (String?) -> Unit = { folderName ->
        viewModel.stopRecording()

        // Gọi ViewModel processAndSave
        viewModel.processAndSave(
            prePadding = 1,
            postPadding = 1,
            folderId = null // TODO: Map folderName sang ID nếu cần
        ) { noteId ->
            if (noteId != null) {
                Toast.makeText(context, "Đã lưu ghi chú!", Toast.LENGTH_SHORT).show()
                onRecordingFinished(noteId) // Điều hướng ra ngoài
            } else {
                Toast.makeText(context, "Lỗi khi lưu! (File rỗng?)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Logic Pause/Resume
    val togglePauseResume: () -> Unit = {
        when (recordingState) {
            RecordingState.Recording -> viewModel.pauseRecording()
            RecordingState.Paused -> viewModel.resumeRecording()
            else -> {}
        }
    }

    // Hàm confirmSave trong Menu
    fun confirmSave(folder: String) {
        showMenu = false
        handleSave(folder) // Gọi handleSave thay vì logic cũ
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Background)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { /* compact */ },
                navigationIcon = {
                    IconButton(onClick = {
                        if (recordingState == RecordingState.Recording || recordingState == RecordingState.Paused) showExitConfirm = true
                        else effectiveOnBack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryAccent, modifier = Modifier.size(18.dp))
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.padding(end = 6.dp)) {
                            Icon(Icons.Default.AddBox, contentDescription = "Thêm",
                                tint = PrimaryAccent,
                                modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            offset = DpOffset(x = (-8).dp, y = 8.dp),
                            modifier = Modifier.width(180.dp).background(color = Color.Transparent)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                tonalElevation = 8.dp,
                                modifier = Modifier.padding(6.dp)
                            ) {
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    MenuCardRow("Công việc", icon = Icons.Default.Bookmark, iconTint = LightGreen) { confirmSave("Công việc") }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    MenuCardRow("Cá nhân", icon = Icons.Default.GroupWork, iconTint = Red) { confirmSave("Cá nhân") }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    MenuCardRow("Ý tưởng", icon = Icons.Default.Lightbulb, iconTint = PrimaryAccent) { confirmSave("Ý tưởng") }
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatElapsed(elapsedMs),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                when (recordingState) {
                    RecordingState.Recording -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Đang ghi âm...", color = Red, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Red))
                        }
                    }
                    RecordingState.Paused -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Tạm dừng", color = Red, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Red.copy(alpha = 0.6f)))
                        }
                    }
                    else -> {
                        Text(text = "Chất lượng tiêu chuẩn", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryAccent.copy(alpha = 0.08f))
            ) {
                WaveformVisualizer(
                    waveform = waveform,
                    vad = vadState,
                    modifier = Modifier.fillMaxSize()
                )

                Text(
                    text = "Biên độ (amp): ${"%.2f".format(amplitude)}" +
                            "\nTrạng thái VAD: $vadLabel" +
                            "\nMẫu còn trong bộ đệm: $bufferAvailable",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom controls
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

                when (recordingState) {
                    RecordingState.Idle -> {
                        // UNCHANGED IDLE FAB
                        val infiniteTransition = rememberInfiniteTransition()
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.06f,
                            animationSpec = infiniteRepeatable(
                                animation = keyframes {
                                    durationMillis = 900
                                    1.06f at 450 using FastOutSlowInEasing
                                },
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        FloatingActionButton(
                            onClick = { startRecording() },
                            modifier = Modifier
                                .size(idleOuterSize)
                                .scale(scale)
                                .shadow(12.dp, CircleShape),
                            containerColor = Color(0xFFF04C4C),
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(8.dp)
                        ) {
                            Box(modifier = Modifier.size(idleInnerSize).background(Color.White, CircleShape))
                        }
                    }

                    RecordingState.Recording -> {
                        // Show Pause and Stop
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 56.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FloatingActionButton(
                                onClick = { togglePauseResume() },
                                modifier = Modifier.size(actionOuterSize).shadow(8.dp, CircleShape),
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

                            // SỬA 3: NÚT STOP GỌI handleSave
                            FloatingActionButton(
                                onClick = { handleSave(null) },
                                modifier = Modifier.size(actionOuterSize).shadow(8.dp, CircleShape),
                                containerColor = Color.DarkGray,
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(actionIconSize))
                            }
                        }
                    }

                    RecordingState.Paused -> {
                        // Show Resume and Save
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 56.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FloatingActionButton(
                                onClick = { togglePauseResume() },
                                modifier = Modifier.size(actionOuterSize).shadow(8.dp, CircleShape),
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

                            // SỬA 4: NÚT SAVE GỌI handleSave
                            FloatingActionButton(
                                onClick = { handleSave(null) },
                                modifier = Modifier.size(actionOuterSize).shadow(8.dp, CircleShape),
                                containerColor = PrimaryAccent,
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Save", tint = Color.White, modifier = Modifier.size(actionIconSize))
                            }
                        }
                    }

                    RecordingState.Stopping -> {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (elapsedMs > 0L) "Bấm biểu tượng thêm (góc phải) để chọn thư mục lưu" else "Nhấn nút ghi âm để bắt đầu thu âm",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Processing dialog (Giữ nguyên)
    if (processing) ProcessingDialog(percent = processingPercent, step = 1, onDismissRequest = {})

    // Exit Confirm (Giữ nguyên)
    if (showExitConfirm) {
        ExitConfirm(visible = true, onConfirm = {
            viewModel.stopRecording()
            effectiveOnBack()
        }, onDismiss = { showExitConfirm = false })
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RecordingScreenPreview() {
    RecordingScreen()
}