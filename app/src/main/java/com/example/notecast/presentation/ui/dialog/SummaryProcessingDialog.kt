package com.example.notecast.presentation.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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