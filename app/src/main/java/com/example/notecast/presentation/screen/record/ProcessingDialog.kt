package com.example.notecast.presentation.screen.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.style.TextOverflow

/**
 * Full-screen processing overlay (NOT a Dialog)
 *
 * - Renders a full-screen scrim that covers entire Compose root (no gaps).
 * - Centers a gradient card (width 320.dp, corner 20.dp, padding 18.dp) like CreateNoteDialog.
 * - Use this by calling ProcessingDialog(percent = ..., step = ..., onDismissRequest = { /*...*/ })
 *   from inside the same Compose root (e.g. RecordingScreen) so it overlays everything.
 */
@Composable
fun ProcessingDialog(
    percent: Int,
    step: Int,
    onDismissRequest: () -> Unit
) {
    val gradientTop = Color(0xFFB96CFF)
    val gradientMiddle = Color(0xFF8A4BFF)
    val gradientBottom = Color(0xFF6A2CFF)
    val gradientBrush = Brush.verticalGradient(listOf(gradientTop, gradientMiddle, gradientBottom))

    // sizes matching CreateNoteDialog
    val dialogWidth = 320.dp
    val dialogCorner = 20.dp
    val innerPadding = 18.dp

    // Full-screen overlay (must be rendered inside same root as the screen to fully cover it)
    Box(
        modifier = Modifier
            .fillMaxSize()
            // scrim covers entire compose root (adjust alpha to taste)
            .background(Color.Black.copy(alpha = 0.36f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(dialogWidth)
                .wrapContentHeight(),
            shape = RoundedCornerShape(dialogCorner),
            color = Color.Transparent,
            tonalElevation = 12.dp
        ) {
            Box(
                modifier = Modifier
                    .background(brush = gradientBrush, shape = RoundedCornerShape(dialogCorner))
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Title
                    Text(
                        "Đang chuyển giọng nói\nthành văn bản và xử lý...",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Spinner + percent
                    CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "${percent}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // progress bar
                    LinearProgressIndicator(
                        progress = (percent.coerceIn(0, 100) / 100f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.16f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Steps list
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ProcessingStepRow(index = 1, title = "Phân tích âm thanh", done = percent >= 35)
                        Spacer(modifier = Modifier.height(8.dp))
                        ProcessingStepRow(index = 2, title = "Chuyển đổi giọng nói sang văn bản", done = percent >= 85)
                        Spacer(modifier = Modifier.height(8.dp))
                        ProcessingStepRow(index = 3, title = "Xử lý nội dung hiển thị", done = percent >= 100)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessingStepRow(index: Int, title: String, done: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        if (done) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(index.toString(), color = Color.White, fontSize = 9.sp)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(title, color = Color.White, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}