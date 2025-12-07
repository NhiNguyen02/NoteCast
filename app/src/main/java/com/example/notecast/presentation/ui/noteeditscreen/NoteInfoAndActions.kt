package com.example.notecast.presentation.ui.noteeditscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Pageview
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.notecast.R
import com.example.notecast.domain.model.Folder
import com.example.notecast.presentation.theme.PopUpBackgroundBrush
import com.example.notecast.presentation.ui.common_components.FolderSelectionButton

@Composable
fun NoteInfoAndActions(
    folderName: String,
    isProcessing: Boolean,
    onFolderSelected: (Folder?) -> Unit,
    availableFolders: List<Folder>,
    onSummarize: () -> Unit,
    onNormalize: () -> Unit,
    hasMindMap: Boolean,
    onMindMap: () -> Unit
) {
//    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
    ){
        FolderSelectionButton(
            currentFolderName = folderName,
            availableFolders = availableFolders,
            onFolderSelected = onFolderSelected
        )
    }
    Divider(thickness = 1.dp, color = Color(0xffE5E7EB))

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        item {
            ActionChip(
                leadingIcon = painterResource(R.drawable.file_text), // Đảm bảo có icon
                label = "Tóm tắt",
                onClick = onSummarize,
                isLoading = isProcessing,
                backgroundBrush = PopUpBackgroundBrush,
                labelColor = Color.White,
            )
        }
        item {
            ActionChip(
                label = "Chuẩn hóa",
                leadingIcon = rememberVectorPainter(Icons.Outlined.AutoFixHigh),
                onClick = onNormalize,
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
    Divider(thickness = 1.dp, color = Color(0xffE5E7EB))
}