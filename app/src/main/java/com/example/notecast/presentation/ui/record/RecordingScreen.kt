@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.notecast.presentation.ui.record

import android.annotation.SuppressLint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.presentation.theme.*

private enum class RecordingState { Idle, Recording, Paused }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    onClose: () -> Unit,
    availableFolders: List<String> = listOf("Công việc", "Cá nhân", "Ý tưởng"),
    onSaveFile: (folderName: String, recordedMs: Long) -> Unit = { _, _ -> }
) {
    val inspection: Boolean = LocalInspectionMode.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    var state by remember { mutableStateOf(RecordingState.Idle) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var pendingRecordedMs by remember { mutableStateOf<Long?>(null) }

    var showMenu by remember { mutableStateOf(false) }

    var showProcessing by remember { mutableStateOf(false) }
    var processingPercent by remember { mutableIntStateOf(0) }
    var processingStep by remember { mutableIntStateOf(1) }

    // Timer
    LaunchedEffect(state, inspection) {
        if (!inspection) {
            while (state == RecordingState.Recording) {
                delay(1000L)
                elapsedMs += 1000L
            }
        }
    }

    val startRecording: () -> Unit = {
        pendingRecordedMs = null
        elapsedMs = 0L
        state = RecordingState.Recording
    }

    val togglePauseResume: () -> Unit = {
        state = if (state == RecordingState.Recording) RecordingState.Paused else RecordingState.Recording
    }

    val pressSaveCheck: () -> Unit = {
        val capturedMs: Long = elapsedMs
        showProcessing = true
        processingPercent = 0
        processingStep = 1

        coroutineScope.launch {
            for (p in 0..35 step 5) {
                processingPercent = p
                processingStep = 1
                delay(180L)
            }
            for (p in 36..85 step 5) {
                processingPercent = p
                processingStep = 2
                delay(160L)
            }
            for (p in 86..100 step 2) {
                processingPercent = p
                processingStep = 3
                delay(120L)
            }
            pendingRecordedMs = capturedMs
            showProcessing = false
            state = RecordingState.Idle
        }
    }

    val openMenuFromHeader: () -> Unit = { showMenu = true }

    fun confirmSave(folder: String) {
        pendingRecordedMs?.let { ms ->
            onSaveFile(folder, ms)
            pendingRecordedMs = null
            elapsedMs = 0L
        }
        showMenu = false
    }

    // sizes: make action buttons match the idle button size as requested
    val idleOuterSize = 80.dp
    val idleInnerSize = 34.dp
    val actionOuterSize = idleOuterSize   // now same as idle
    val actionIconSize = idleInnerSize    // now same as idle inner icon
    val actionGap = 12.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { /* empty */ },
                navigationIcon = {
                    IconButton(onClick = onClose, modifier = Modifier.padding(start = 4.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryAccent, modifier = Modifier.size(18.dp))
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { openMenuFromHeader() }, modifier = Modifier.padding(end = 6.dp)) {
                            Icon(Icons.Default.AddBox, contentDescription = "Thêm",
                                tint = if (pendingRecordedMs != null) PrimaryAccent else PrimaryAccent.copy(alpha = 0.9f),
                                modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            offset = DpOffset(x = (-8).dp, y = 8.dp),
                            modifier = Modifier.width(180.dp)
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

            Spacer(modifier = Modifier.height(28.dp))

            // Timer
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = formatElapsed(elapsedMs), fontSize = 46.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(6.dp))
                when (state) {
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

            Spacer(modifier = Modifier.height(18.dp))

            // Waveform
            if (state == RecordingState.Recording || state == RecordingState.Paused) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(200.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PrimaryAccent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    val animate = !inspection && state == RecordingState.Recording
                    WaveformPlaceholder(modifier = Modifier.fillMaxWidth(0.92f).height(120.dp).clip(RoundedCornerShape(8.dp)), active = animate)
                }
            } else {
                Spacer(modifier = Modifier.height(200.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom controls
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                when (state) {
                    RecordingState.Idle -> {
                        // Idle pulsing floating action button (circular)
                        val infiniteTransition = rememberInfiniteTransition()
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.06f,
                            animationSpec = infiniteRepeatable(
                                animation = keyframes { durationMillis = 900; 1.06f at 450 with FastOutSlowInEasing },
                                repeatMode = RepeatMode.Reverse
                            )
                        )

                        FloatingActionButton(
                            onClick = { startRecording() },
                            modifier = Modifier.size(idleOuterSize).scale(scale).shadow(12.dp, CircleShape),
                            containerColor = Color(0xFFF04C4C),
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(8.dp)
                        ) {
                            Box(modifier = Modifier.size(idleInnerSize).background(Color.White, CircleShape))
                        }
                    }
                    RecordingState.Recording, RecordingState.Paused -> {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 56.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            FloatingActionButton(
                                onClick = { togglePauseResume() },
                                modifier = Modifier.size(actionOuterSize).shadow(8.dp, CircleShape),
                                containerColor = Red,
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(6.dp)
                            ) {
                                Icon(imageVector = if (state == RecordingState.Recording) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(actionIconSize))
                            }

                            Spacer(modifier = Modifier.width(actionGap))

                            FloatingActionButton(
                                onClick = { pressSaveCheck() },
                                modifier = Modifier.size(actionOuterSize).shadow(8.dp, CircleShape),
                                containerColor = PrimaryAccent,
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(actionIconSize))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = if (pendingRecordedMs != null) "Bấm biểu tượng thêm (góc phải) để chọn thư mục lưu" else "Nhấn nút ghi âm để bắt đầu thu âm", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
            }
        }

        // Processing dialog
        if (showProcessing) {
            ProcessingDialog(percent = processingPercent, step = processingStep, onDismissRequest = { /* ignore */ })
        }
    }
}

/** Waveform & helpers **/
@Composable
private fun WaveformPlaceholder(modifier: Modifier = Modifier, active: Boolean) {
    val transition = rememberInfiniteTransition()
    val anim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes { durationMillis = 900; 1f at 450 with FastOutSlowInEasing },
            repeatMode = RepeatMode.Reverse
        )
    )

    val baseHeights = listOf(0.2f,0.5f,0.9f,0.6f,0.7f,0.4f,0.8f,0.5f,0.3f,0.6f,0.9f,0.4f,0.2f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val spacing = w / (baseHeights.size * 2f)
        val barWidth = spacing
        val centerY = h / 2f
        val color = PrimaryAccent

        baseHeights.forEachIndexed { i, rh ->
            val cx = spacing + i * spacing * 2f
            val factor = if (active) (0.7f + 0.3f * anim) else 0.7f
            val barH = (rh * h * 0.9f * factor).coerceAtMost(h)
            drawLine(color = color, start = Offset(cx, centerY - barH / 2f), end = Offset(cx, centerY + barH / 2f), strokeWidth = barWidth, cap = StrokeCap.Round)
        }
    }
}

/** MenuCardRow (kept local) **/
@Composable
private fun MenuCardRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, iconTint: Color, onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).clickable { onClick() }) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color.White).border(BorderStroke(1.5.dp, iconTint), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                icon?.let { Icon(imageVector = it, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp)) }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, color = iconTint, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

/** Timer helper **/
@SuppressLint("DefaultLocale")
private fun formatElapsed(ms: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RecordingScreenPreview() {
    RecordingScreen(onClose = {}, availableFolders = listOf("Công việc", "Cá nhân", "Ý tưởng"))
}