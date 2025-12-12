package com.example.notecast.presentation.ui.common_components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.R
import com.example.notecast.domain.model.Folder
import com.example.notecast.presentation.theme.PrimaryAccent
import androidx.core.graphics.toColorInt

/**
 * Component hiển thị nút chọn thư mục và menu xổ xuống.
 */
@Composable
fun FolderSelectionButton(
    currentFolderId: String?,
    availableFolders: List<Folder>,
    onFolderSelected: (Folder?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Lấy folder hiện tại theo id (Option A)
    val currentFolder = remember(currentFolderId, availableFolders) {
        availableFolders.firstOrNull { it.id == currentFolderId }
    }

    val currentFolderName = currentFolder?.name ?: "Chưa phân loại"
    // Dùng helper getComposeColor() từ domain model để không lặp logic
    val currentFolderColor: Color = currentFolder?.getComposeColor() ?: Color(0xFFCCA8FF)

    Box {
        // 1. CHIP HIỂN THỊ
        AssistChip(
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.folder_outline),
                    tint = currentFolderColor,
                    contentDescription = "folder",
                    modifier = Modifier.size(20.dp)
                )
            },
            onClick = { expanded = true },
            label = { Text(currentFolderName, fontSize = 14.sp) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = currentFolderColor.copy(alpha = 0.15f),
                labelColor = currentFolderColor,
                leadingIconContentColor = currentFolderColor.copy(alpha = 0.5f),
                trailingIconContentColor = currentFolderColor.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp),
            border = null
        )

        // 2. DROPDOWN MENU
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .width(180.dp)
                .background(
                    Color.White.copy(0.7f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 10.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            FolderDropdownItem(
                name = "Chưa phân loại",
                color = Color(0xFFCCA8FF),
                onClick = {
                    onFolderSelected(null)
                    expanded = false
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // --- DANH SÁCH THƯ MỤC ---
            availableFolders.forEach { folder ->
                val folderColor = folder.getComposeColor()

                FolderDropdownItem(
                    name = folder.name,
                    color = folderColor,
                    onClick = {
                        onFolderSelected(folder)
                        expanded = false
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun FolderDropdownItem(
    name: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)) // Thêm viền nhẹ
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.15f))
                    .border(1.dp, color.copy(alpha = 0.5f), shape = RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Folder,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.9f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = color,
                maxLines = 1
            )
        }
    }
}