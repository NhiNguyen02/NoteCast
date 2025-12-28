package com.example.notecast.presentation.ui.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.notecast.domain.model.Folder
import com.example.notecast.presentation.theme.PopUpBackgroundBrush
import com.example.notecast.presentation.theme.PrimaryAccent
import androidx.core.graphics.toColorInt

@Composable
fun SelectFolderDialog(
    folders: List<Folder>,
    onDismiss: () -> Unit,
    onFolderSelected: (Folder?) -> Unit // Trả về null nếu chọn "Chưa phân loại"
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(PopUpBackgroundBrush)
                .padding(20.dp)
        ) {
            Text(
                text = "Chọn thư mục",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            if (folders.isEmpty()) {
                // Nếu chưa có folder nào, vẫn hiện tùy chọn "Chưa phân loại"
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Text(
                            text = "Chưa có thư mục nào khác",
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(20.dp).align(Alignment.CenterHorizontally)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {


//                    Danh sách thư mục
                    items(folders) { folder ->
                        val folderColor = try {
                            if (folder.colorHex.isNullOrBlank()) PrimaryAccent
                            else Color(folder.colorHex.toColorInt())
                        } catch (_: Exception) {
                            PrimaryAccent }

                        FolderSelectionItem(
                            name = folder.name,
                            color = folderColor,
                            icon = Icons.Outlined.Folder,
                            onClick = { onFolderSelected(folder) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Hủy", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FolderSelectionItem(
    name: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = color
            )
        }
    }
}