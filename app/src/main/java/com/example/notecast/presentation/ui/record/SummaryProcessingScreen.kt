package com.example.notecast.presentation.ui.record



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SummaryProcessingScreen.kt
 *
 * Chứa 2 composable tách riêng từ NoteDetailTextScreen:
 * - SummaryProcessingDialog: overlay full-screen khi đang tóm tắt (spinner + percent + progress bar + Hủy)
 * - SummaryCompleteDialog: dialog centered hiển thị khi tóm tắt xong (title + subtitle + OK)
 *
 * Cách dùng:
 * - Import và gọi SummaryProcessingDialog(...) hoặc SummaryCompleteDialog(...) từ NoteDetailTextScreen hoặc ViewModel host.
 * - Hoặc dùng SummaryProcessingHost() để chạy demo mô phỏng tiến trình.
 */

/* ---------- Processing overlay ---------- */
@Composable
fun SummaryProcessingDialog(
    percent: Int,
    statusText: String = "",
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // If percent >= 100 don't render processing dialog (caller should switch to result dialog)
    if (percent >= 100) return

    val gradientTop = Color(0xFFB96CFF)
    val gradientMiddle = Color(0xFF8A4BFF)
    val gradientBottom = Color(0xFF6A2CFF)
    val gradientBrush = Brush.verticalGradient(listOf(gradientTop, gradientMiddle, gradientBottom))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.36f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            tonalElevation = 12.dp
        ) {
            Box(
                modifier = Modifier
                    .background(brush = gradientBrush, shape = RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Đang tóm tắt ghi chú",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "${percent}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = (percent.coerceIn(0, 100) / 100f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.16f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        when {
                            percent < 30 -> if (statusText.isNotEmpty()) statusText else "Đang phân tích nội dung..."
                            percent < 80 -> if (statusText.isNotEmpty()) statusText else "Đang chuyển đổi và tóm tắt..."
                            else -> if (statusText.isNotEmpty()) statusText else "Hoàn tất xử lý..."
                        },
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.12f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("Hủy")
                    }
                }
            }
        }
    }
}

/* ---------- Completion dialog ---------- */
@Composable
fun SummaryCompleteDialog(
    title: String = "Đã tóm tắt xong",
    subtitle: String = "Tập trung vào nội dung trọng tâm của bạn",
    onOk: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientTop = Color(0xFFB96CFF)
    val gradientMiddle = Color(0xFF8A4BFF)
    val gradientBottom = Color(0xFF6A2CFF)
    val gradientBrush = Brush.verticalGradient(listOf(gradientTop, gradientMiddle, gradientBottom))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(300.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(18.dp),
            color = Color.Transparent,
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .background(brush = gradientBrush, shape = RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOk,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = gradientMiddle),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("OK", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/* ---------- Host demo (optional) ---------- */
@Composable
fun SummaryProcessingHost(onDone: (resultText: String) -> Unit = {}) {
    var showProcessing by remember { mutableStateOf(false) }
    var percent by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            showProcessing = true
            percent = 0
            showResult = false
            scope.launch {
                while (percent < 100 && showProcessing) {
                    delay(220L)
                    val step = if (percent < 30) 6 else if (percent < 80) 8 else 4
                    percent = (percent + step).coerceAtMost(100)
                }
                if (percent >= 100) {
                    delay(220)
                    showProcessing = false
                    showResult = true
                    onDone("Tóm tắt hoàn tất")
                }
            }
        }) {
            Text("Bắt đầu tóm tắt")
        }
    }

    if (showProcessing) {
        SummaryProcessingDialog(
            percent = percent,
            onCancel = {
                showProcessing = false
                percent = 0
            }
        )
    }

    if (showResult) {
        SummaryCompleteDialog(
            onOk = { showResult = false }
        )
    }
}

/* ---------- Previews ---------- */
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SummaryProcessingDialogPreview() {
    SummaryProcessingDialog(percent = 42, onCancel = {})
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SummaryCompleteDialogPreview() {
    SummaryCompleteDialog(onOk = {})
}