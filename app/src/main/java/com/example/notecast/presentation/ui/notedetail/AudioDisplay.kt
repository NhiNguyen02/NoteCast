package com.example.notecast.presentation.ui.notedetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.utils.formatTime

@Composable
fun AudioDisplay(
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    progress: Float,
    totalSeconds: Int,
    gradientTop: Color,
    gradientMiddle: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF3EEFF))
            .border(BorderStroke(1.dp, Color(0xFFEEE9FB)), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "File âm thanh",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF5D1AAE)
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Controls row (prev / big play / next)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1E7FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Prev",
                        tint = gradientMiddle,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(18.dp))
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(gradientTop, gradientMiddle))),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onTogglePlay) {
                        if (isPlaying) {
                            Icon(
                                Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(18.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1E7FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = gradientMiddle,
                        modifier = Modifier.size(16.dp)
                    )
                }
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
                if (filledW > 0f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(listOf(Color(0xFFB96CFF), gradientMiddle)),
                        topLeft = Offset(0f, 0f),
                        size = Size(filledW, trackH),
                        cornerRadius = corner
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