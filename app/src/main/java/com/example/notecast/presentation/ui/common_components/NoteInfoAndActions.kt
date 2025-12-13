package com.example.notecast.presentation.ui.common_components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Pageview
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.notecast.R
import com.example.notecast.presentation.theme.TabButton3Brush
import com.example.notecast.presentation.ui.noteeditscreen.ActionChip

@Composable
fun NoteInfoAndActions(
    isProcessing: Boolean,
    onSummarize: () -> Unit,
    onNormalize: () -> Unit,
    hasMindMap: Boolean,
    isNormalizing: Boolean,
    onMindMap: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        item {
            ActionChip(
                label = "Chuẩn hóa",
                leadingIcon = rememberVectorPainter(Icons.Outlined.AutoFixHigh),
                onClick = onNormalize,
                isLoading = isNormalizing,
                backgroundBrush = Brush.verticalGradient(
                    0.0f to Color(0xff00D2FF),
                    0.59f to Color(0xff307FE3),
                    1.0f to Color(0xff7532FB),
                ),
                labelColor = Color.White
            )
        }
        item {
            ActionChip(
                leadingIcon = painterResource(R.drawable.file_text), // Đảm bảo có icon
                label = "Tóm tắt",
                onClick = onSummarize,
                isLoading = isProcessing,
                backgroundBrush = TabButton3Brush,
                labelColor = Color(0xff307FE3),
            )
        }
        item {
            ActionChip(
                label = if (hasMindMap) "Xem Mindmap" else "Tạo Mindmap",
                leadingIcon = if (hasMindMap)
                    rememberVectorPainter(Icons.Outlined.Pageview)// Đảm bảo có icon
                else
                    painterResource(R.drawable.icon_park_mindmap_map), // Icon Map

                onClick = onMindMap,
                backgroundBrush = Brush.verticalGradient(
                    0.0f to Color(0xffC2D1EC),
                    1.0f to Color(0xff6A92C8)
                ),
                labelColor = Color.White
            )
        }
    }
    HorizontalDivider(
        color = Color(0xffE5E7EB),
        thickness = 1.dp,
        modifier = Modifier.fillMaxWidth()
    )
}