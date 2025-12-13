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

// Reusable processing types so different flows can share the same dialog
sealed class ProcessingType {
    data object Asr : ProcessingType()            // Recording -> ASR/transcript
    data object MindMap : ProcessingType()        // Generate mind map
    data object Summarize : ProcessingType()      // Summarize note
    data object Normalize : ProcessingType()      // Normalize note
    data class Custom(
        val title: String,
        val stepLabel: String? = null
    ) : ProcessingType()
}

private fun ProcessingType.titleText(): String = when (this) {
    ProcessingType.Asr -> "Đang chuyển giọng nói thành văn bản"
    ProcessingType.MindMap -> "Đang tạo sơ đồ tư duy"
    ProcessingType.Summarize -> "Đang tóm tắt nội dung"
    ProcessingType.Normalize -> "Đang chuẩn hóa nội dung"
    is ProcessingType.Custom -> this.title
}

private fun ProcessingType.defaultStepLabel(step: Int): String = when (this) {
    ProcessingType.Asr -> when (step) {
        1 -> "Phân tích âm thanh..."
        2 -> "Chuyển giọng nói sang văn bản..."
        3 -> "Xử lý nội dung hiển thị..."
        else -> "Đang xử lý..."
    }
    ProcessingType.MindMap -> when (step) {
        1 -> "Đang phân tích nội dung ghi chú..."
        2 -> "Tạo cấu trúc sơ đồ tư duy..."
        3 -> "Hoàn tất sơ đồ tư duy..."
        else -> "Đang xử lý sơ đồ tư duy..."
    }
    ProcessingType.Summarize -> when (step) {
        1 -> "Đang phân tích nội dung để tóm tắt..."
        2 -> "Sinh bản tóm tắt..."
        3 -> "Hoàn thiện bản tóm tắt..."
        else -> "Đang tóm tắt..."
    }
    ProcessingType.Normalize -> when (step) {
        1 -> "Đang phân tích nội dung để chuẩn hóa..."
        2 -> "Đang chuẩn hóa câu chữ..."
        3 -> "Hoàn thiện bản đã chuẩn hóa..."
        else -> "Đang chuẩn hóa..."
    }
    is ProcessingType.Custom -> this.stepLabel ?: "Đang xử lý..."
}

// Main reusable dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingDialog(
    percent: Int,
    step: Int,
    type: ProcessingType = ProcessingType.Asr,
    details: String? = null,
    onDismissRequest: () -> Unit
) {
    val pct = percent.coerceIn(0, 100)
    val title = type.titleText()
    val stepLabel = type.defaultStepLabel(step)

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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Step label
                    Text(
                        text = stepLabel,
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Spinner + percent
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${pct}%",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
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

                    // Optional extra details
                    details?.let {
                        Text(
                            text = it,
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
