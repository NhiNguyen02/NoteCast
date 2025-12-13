package com.example.notecast.presentation.ui.notedetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.presentation.theme.MainButtonBrush
import com.example.notecast.presentation.theme.SubTitleColor
import com.example.notecast.utils.formatTime

@Composable
fun AudioDisplay(
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    progress: Float,
    totalSeconds: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(0.5f))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Controls row (prev / big play / next)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "Prev",
                    tint = SubTitleColor,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(18.dp))
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .border(BorderStroke(4.dp, MainButtonBrush), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onTogglePlay,
                        colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = SubTitleColor
                        )
                    ) {
                        if (isPlaying) {
                            Icon(
                                Icons.Default.Pause,
                                contentDescription = "Pause",
                                modifier = Modifier
                                    .size(54.dp).padding(4.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                modifier = Modifier
                                    .size(54.dp).padding(4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(18.dp))
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = SubTitleColor,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress track (Canvas runtime)
            val trackHeight = 8.dp
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
            ) {
                val trackH = trackHeight.toPx()
                val corner = CornerRadius(trackH / 2f, trackH / 2f)
                drawRoundRect(
                    color = Color(0xFFDDE1E6),
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, trackH),
                    cornerRadius = corner
                )
                val clamped = progress.coerceIn(0f, 1f)
                val filledW = size.width * clamped
                val audioDisplayBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFF00D2FF), Color(0xFF307FE3), Color(0xFF7532FB), Color(0xFF8A4AE1)),
                    start = Offset(0f, 0f),
                    end = Offset(filledW, 0f)   // gradient chạy theo chiều dài phần đã fill
                )
                if (filledW > 0f) {
                    drawRoundRect(
                        brush = audioDisplayBrush,
                        topLeft = Offset(0f, 0f),
                        size = Size(filledW, trackH),
                        cornerRadius = corner,
                    )
                }
                val thumbR = 7.dp.toPx()
                val cx = filledW.coerceIn(thumbR, size.width - thumbR)
                val cy = trackH / 2f
                drawCircle(color = Color.White, radius = thumbR, center = Offset(cx, cy))
                drawCircle(
                    color = Color(0x66B96CFF),
                    radius = thumbR + 1.2f,
                    center = Offset(cx, cy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentSec = (progress.coerceIn(0f, 1f) * totalSeconds).toInt()
                Text(
                    formatTime(currentSec),
                    style = TextStyle(fontSize = 12.sp, color = Color(0xFF6B6B6B))
                )
                Text(
                    formatTime(totalSeconds),
                    style = TextStyle(fontSize = 12.sp, color = Color(0xFF6B6B6B))
                )
            }
        }
    }
}