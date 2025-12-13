package com.example.notecast.presentation.ui.notedetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.presentation.theme.SubTitleColor
import com.example.notecast.utils.formatTime
@Composable
fun TranscriptionDisplay(
    chunks: List<com.example.notecast.domain.model.ChunkResult>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(0.5f))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                "Bản chép lời",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SubTitleColor
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (chunks.isEmpty()) {
                Text(
                    "Chưa có dữ liệu đoạn thoại.",
                    style = TextStyle(fontSize = 14.sp, color = Color(0xFF777777))
                )
            } else {
                chunks.forEach { chunk ->
                    TranscriptRow(
                        time = formatTime(chunk.startSec.toInt()),
                        text = chunk.text
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}