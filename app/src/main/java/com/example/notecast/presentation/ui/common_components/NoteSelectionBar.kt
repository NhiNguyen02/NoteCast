package com.example.notecast.presentation.ui.common_components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileMove // Icon di chuyển
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.notecast.presentation.theme.Purple

@Composable
fun NoteSelectionBar(
    selectedCount: Int,
    onSelectAllClick: () -> Unit,
    onMoveClick: () -> Unit, // Sự kiện di chuyển
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectAllClick() }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Chọn tất cả",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Di chuyển
            ActionChip(
                icon = Icons.Outlined.DriveFileMove,
                label = "Di chuyển",
                onClick = onMoveClick,
                enabled = selectedCount > 0,
                color = Purple
            )

            Spacer(modifier = Modifier.width(48.dp))

            // Nút Xóa
            ActionChip(
                icon = Icons.Outlined.Delete,
                label = "Xóa",
                onClick = onDeleteClick,
                enabled = selectedCount > 0,
                color = Color.Red
            )
        }
    }
}

@Composable
private fun ActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    color: Color
) {
    val finalColor = if (enabled) color else Color.Gray.copy(alpha = 0.6f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = finalColor, modifier = Modifier.size(24.dp))
        Text(text = label, color = finalColor, style = MaterialTheme.typography.labelSmall)
    }
}