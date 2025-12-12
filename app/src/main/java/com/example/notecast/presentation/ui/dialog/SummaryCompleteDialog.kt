package com.example.notecast.presentation.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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