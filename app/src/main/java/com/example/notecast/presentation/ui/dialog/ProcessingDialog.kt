package com.example.notecast.presentation.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.presentation.theme.PopUpBackgroundBrush
import kotlin.let
import kotlin.ranges.coerceIn

@Composable
fun ProcessingDialog1(
    percent: Int,
    step: Int,
    onDismissRequest: () -> Unit
) {
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
                    .background(brush = PopUpBackgroundBrush, shape = RoundedCornerShape(dialogCorner))
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
                        ProcessingStepRow(
                            index = 1,
                            title = "Phân tích âm thanh",
                            done = percent >= 35
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProcessingStepRow(
                            index = 2,
                            title = "Chuyển đổi giọng nói sang văn bản",
                            done = percent >= 85
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProcessingStepRow(
                            index = 3,
                            title = "Xử lý nội dung hiển thị",
                            done = percent >= 100
                        )
                    }
                }
            }
        }
    }
}


// riêng cho giai đoạn 2
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingDialog(
    percent: Int,
    step: Int,
    details: String? = null,
    onDismissRequest: () -> Unit
) {
    val pct = percent.coerceIn(0, 100)
    val stepLabel = when (step) {
        1 -> "Chuẩn bị dữ liệu âm thanh..."
        2 -> "Lưu file (ghi I/O)..."
        3 -> "Hoàn tất và xác nhận..."
        else -> "Đang xử lý..."
    }

    // sizes matching CreateNoteDialog
    val dialogWidth = 320.dp
    val dialogCorner = 20.dp
    val innerPadding = 18.dp

    // Full-screen overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
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
                    .background(brush = PopUpBackgroundBrush, shape = RoundedCornerShape(dialogCorner))
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Title
                    Text(
                        "Đang xử lý",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Step label
                    Text(
                        stepLabel,
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Spinner + percent
                    CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "${pct}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // progress bar
                    LinearProgressIndicator(
                        progress = pct / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.16f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Details if provided
                    details?.let {
                        Text(
                            text = "Chi tiết: $it",
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Dismiss button
                }
            }
        }
    }
}
