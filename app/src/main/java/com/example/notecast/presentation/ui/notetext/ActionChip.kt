package com.example.notecast.presentation.ui.notetext

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Composable phụ cho Chips hành động (GIỮ NGUYÊN)
@Composable
fun ActionChip(
    label: String,
    leadingIcon: Painter,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    backgroundBrush: Brush,
    labelColor: Color
) {
    val chipShape = RoundedCornerShape(8.dp)

    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.6f,
        label = "chip-alpha"
    )

    Surface(
        shape = chipShape,
        shadowElevation = 2.dp,
        modifier = Modifier
            .height(32.dp)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(
            modifier = Modifier.background(brush = backgroundBrush),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {

                Icon(
                    painter = leadingIcon,
                    contentDescription = label,
                    tint = labelColor.copy(alpha = alpha),
                    modifier = Modifier.size(20.dp)
                )
                if (isLoading) {
                    Text("Đang xử lý...", fontSize = 14.sp, color = labelColor)
                    Spacer(Modifier.width(8.dp))
                    CircularProgressIndicator(
                        Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = labelColor.copy(alpha = alpha)
                    )
                } else {
                    Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = labelColor.copy(alpha = alpha))
                }
            }
        }
    }
}