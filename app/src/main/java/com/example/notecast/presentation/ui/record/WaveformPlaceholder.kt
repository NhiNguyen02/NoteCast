package com.example.notecast.presentation.ui.record

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import com.example.notecast.presentation.theme.PrimaryAccent

/** Waveform & helpers reused from RecordingScreen **/
@Composable
fun WaveformPlaceholder(modifier: Modifier = Modifier, active: Boolean) {
    val transition = rememberInfiniteTransition()
    val anim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes { durationMillis = 900; 1f at 450 with FastOutSlowInEasing },
            repeatMode = RepeatMode.Reverse
        )
    )

    val baseHeights = listOf(0.2f,0.5f,0.9f,0.6f,0.7f,0.4f,0.8f,0.5f,0.3f,0.6f,0.9f,0.4f,0.2f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val spacing = w / (baseHeights.size * 2f)
        val barWidth = spacing
        val centerY = h / 2f
        val color = PrimaryAccent

        baseHeights.forEachIndexed { i, rh ->
            val cx = spacing + i * spacing * 2f
            val factor = if (active) (0.7f + 0.3f * anim) else 0.7f
            val barH = (rh * h * 0.9f * factor).coerceAtMost(h)
            drawLine(color = color, start = Offset(cx, centerY - barH / 2f), end = Offset(cx, centerY + barH / 2f), strokeWidth = barWidth, cap = StrokeCap.Round)
        }
    }
}