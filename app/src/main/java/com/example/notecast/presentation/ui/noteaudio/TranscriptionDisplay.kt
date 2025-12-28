package com.example.notecast.presentation.ui.noteaudio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.domain.model.AudioChunk
import com.example.notecast.presentation.theme.SubTitleColor
import com.example.notecast.utils.formatTime

@Composable
fun TranscriptionDisplay(
    chunks: List<AudioChunk>,
    modifier: Modifier = Modifier,
    currentTimeSec: Double? = null,
    onChunkClick: ((AudioChunk) -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(0.5f))
            .padding(horizontal = 20.dp, vertical = 12.dp)
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
                val isActive = currentTimeSec?.let { t -> t >= chunk.start && t < chunk.end } ?: false
                val bgColor = if (isActive) Color(0xFFE0ECFF) else Color.Transparent

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor, RoundedCornerShape(8.dp))
                        .clickable(enabled = onChunkClick != null) { onChunkClick?.invoke(chunk) }
                        .padding(8.dp)
                ) {
                    Text(
                        text = formatTime(chunk.start.toInt()),
                        style = MaterialTheme.typography.labelSmall.copy(color = SubTitleColor)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = chunk.text,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                    )
                }
            }
        }
    }
}