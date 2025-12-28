package com.example.notecast.presentation.ui.noteaudio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NoteDetailBottomActions(
    onNormalize: () -> Unit,
    onSaveNote: () -> Unit,
    onSummarize: () -> Unit,
    hasMindMap: Boolean,
    onGenerateOrShowMindMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradientTop = Color(0xFFB96CFF)
    val gradientMiddle = Color(0xFF8A4BFF)
    val blueStart = Color(0xFF2EC7FF)
    val blueEnd = Color(0xFF3AA8FF)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.Transparent),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GradientActionButton(
                text = "Chuẩn hóa",
                icon = Icons.Default.AutoFixHigh,
                textColor = Color.White,
                gradientColors = listOf(gradientTop, gradientMiddle),
                onClick = onNormalize,
                modifier = Modifier.weight(1f)
            )

            GradientActionButton(
                text = "Lưu ghi chú",
                icon = Icons.Default.Edit,
                textColor = gradientMiddle,
                gradientColors = listOf(Color(0xFFF2F7FF), Color(0xFFE8F3FF)),
                borderColor = null,
                iconTint = gradientMiddle,
                onClick = onSaveNote,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GradientActionButton(
                text = "Tóm tắt",
                icon = Icons.Default.Description,
                textColor = Color.White,
                gradientColors = listOf(blueStart, blueEnd),
                onClick = onSummarize,
                modifier = Modifier.weight(1f)
            )

            GradientActionButton(
                text = if (hasMindMap) "Xem Mind map" else "Tạo Mind map",
                icon = Icons.Default.DeviceHub,
                textColor = Color(0xFF6EA8D9),
                gradientColors = listOf(Color(0xFFF2F7FF), Color(0xFFE8F3FF)),
                borderColor = Color(0xFFB8CFE6),
                iconTint = gradientMiddle,
                onClick = onGenerateOrShowMindMap,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
