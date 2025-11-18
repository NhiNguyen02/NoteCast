package com.example.notecast.presentation.ui.record

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.notecast.domain.vad.VadState

@Composable
fun WaveformVisualizer(waveform: List<Float>, vad: VadState, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerY = h / 2f
        val count = waveform.size.takeIf { it > 0 } ?: 1
        val stepX = w / count.toFloat()
        if (vad == VadState.SPEAKING) {
            drawRect(color = Color(0x2233FF33))
        }
        for (i in waveform.indices) {
            val x = i * stepX
            val norm = waveform[i].coerceIn(0f, 1f)
            val lineHeight = norm * h * 0.9f
            drawLine(
                color = Color.White,
                start = Offset(x, centerY - lineHeight / 2f),
                end = Offset(x, centerY + lineHeight / 2f),
                strokeWidth = stepX.coerceAtLeast(1f)
            )
        }
    }
}