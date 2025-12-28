package com.example.notecast.presentation.ui.folderscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.notecast.presentation.theme.Purple

@Composable
fun SelectionBar(
    selectedCount: Int,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
        Spacer(modifier = Modifier.height(8.dp))

        // Nút Sửa và Xóa
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Sửa
            ActionChip(
                icon = Icons.Outlined.NoteAlt,
                label = "Sửa",
                onClick = onEditClick,
                enabled = selectedCount == 1 // Thường chỉ sửa khi chọn 1 mục
            )

            Spacer(modifier = Modifier.width(32.dp))

            // Nút Xóa
            ActionChip(
                icon = Icons.Outlined.Delete,
                label = "Xóa",
                onClick = onDeleteClick,
                enabled = selectedCount > 0
            )
        }
}


@Composable
private fun ActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean
) {

    val colorEdit = if (enabled) Color.Red else Color.Gray.copy(alpha = 0.6f)
    val colorRemove = if (enabled) Purple else Color.Gray.copy(alpha = 0.6f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if(label == "Sửa") colorEdit else colorRemove,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = if(label == "Sửa") colorEdit else colorRemove,
            style = MaterialTheme.typography.labelSmall
        )
    }
}