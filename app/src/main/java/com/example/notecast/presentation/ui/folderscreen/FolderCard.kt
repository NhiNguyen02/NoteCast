package com.example.notecast.presentation.ui.folderscreen

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.unit.dp
import com.example.notecast.domain.model.Folder
import com.example.notecast.presentation.theme.PrimaryAccent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderCard(
    folder: Folder,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    noteCount: Int = 0,
    onFolderClick: (Folder) -> Unit,
    onFolderLongClick: (Folder) -> Unit
) {
    // Helper chuyển Hex String -> Color
    fun hexToColor(hex: String?): Color {
        return try {
            if (hex.isNullOrBlank()) PrimaryAccent
            else Color(AndroidColor.parseColor(hex))
        } catch (_: Exception) {
            PrimaryAccent
        }
    }

    val folderTint = hexToColor(folder.colorHex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Transparent)
            .combinedClickable(
                onClick = { onFolderClick(folder) },
                onLongClick = { onFolderLongClick(folder) }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp) // Card Height cố định
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .then(
                    if (isSelected) Modifier.border(BorderStroke(2.dp, PrimaryAccent), RoundedCornerShape(10.dp))
                    else Modifier.border(BorderStroke(1.dp, Color(0xFFE8E8F0)), RoundedCornerShape(10.dp))
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Checkbox
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) PrimaryAccent else Color(0xFFF0F0F4)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            // 2. Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(folderTint.copy(alpha = 0.15f))
                    .border(1.dp, folderTint.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Folder, contentDescription = null, tint = folderTint.copy(alpha = 0.9f), modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 3. Tên & Số lượng
            Text(
                text = folder.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = folderTint
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$noteCount ghi chú",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xff757575)
            )
        }
    }
}