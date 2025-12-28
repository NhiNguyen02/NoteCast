package com.example.notecast.presentation.ui.noteaudio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.presentation.theme.Blue
import com.example.notecast.presentation.theme.BluePurple

@Composable
fun TranscriptRow(time: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.horizontalGradient(listOf(BluePurple, Blue)))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(time, style = TextStyle(color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(text = text, style = TextStyle(color = Color(0xFF222222), fontSize = 14.sp, lineHeight = 20.sp), modifier = Modifier.weight(1f))
    }
}