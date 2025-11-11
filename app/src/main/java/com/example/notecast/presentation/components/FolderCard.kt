package com.example.notecast.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.notecast.domain.model.Folder

// --- Hằng số giả lập ---
val cardHeight: Dp = 80.dp
val PrimaryAccent = Color(0xFF7B68EE) // Màu Tím chủ đạo (Accent Color)
// ------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderCard(
    folder: Folder,
    isSelectionMode: Boolean,
    onFolderClick: (Folder) -> Unit,
    onFolderLongClick: (Folder) -> Unit,
    onToggleSelect: (Folder) -> Unit,
    modifier: Modifier = Modifier
) {
    val folderTint = folder.color
    val isSelected = folder.isSelected

    val clickHandler = {
        if (isSelectionMode) {
            onToggleSelect(folder)
        } else {
            onFolderClick(folder)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Transparent)
            .combinedClickable(
                onClick = clickHandler,
                onLongClick = { onFolderLongClick(folder) }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .then(
                    // Viền: PrimaryAccent nếu được chọn, hoặc màu xám nhạt
                    if (isSelected) Modifier.border(BorderStroke(2.dp, PrimaryAccent), RoundedCornerShape(10.dp))
                    else Modifier.border(BorderStroke(1.dp, Color(0xFFE8E8F0)), RoundedCornerShape(10.dp))
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 1. Checkbox (Chỉ hiển thị khi đang ở chế độ chọn)
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) PrimaryAccent else Color(0xFFF0F0F4)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "selected",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            // 2. Folder Icon Tile
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(folderTint.copy(0.15f))
                    .border(1.dp,folderTint.copy(0.5f), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Folder,
                    contentDescription = null,
                    tint = folderTint.copy(alpha = 0.9f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 3. Tên và số lượng ghi chú
            Text(
                text = folder.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${folder.noteCount} ghi chú",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xff757575)
            )



//            // 4. Số lượng ở cuối
//            Text(
//                text = folder.noteCount.toString(),
//                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//            )
        }
    }
}