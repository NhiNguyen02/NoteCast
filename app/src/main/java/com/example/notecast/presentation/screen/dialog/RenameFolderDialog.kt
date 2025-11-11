package com.example.notecast.presentation.screen.dialog


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.notecast.domain.model.Folder // Dùng để truyền thư mục cần sửa
import com.example.notecast.presentation.theme.backgroundDialog


@Composable
fun RenameFolderDialog(
    folderToRename: Folder, // Thư mục hiện tại (để lấy tên mặc định)
    onDismiss: () -> Unit,
    onConfirm: (newName: String) -> Unit
) {
    // State cho tên thư mục, khởi tạo bằng tên thư mục cũ
    var newFolderName by remember { mutableStateOf(folderToRename.name) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundDialog)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Tiêu đề
            Text(
                text = "Đổi tên thư mục",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Mô tả
            Text(
                text = "Nhập tên mới cho thư mục",
                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // --- Tên thư mục Input ---
            Text(
                text = "Tên thư mục",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = newFolderName,
                onValueChange = { newFolderName = it },
                placeholder = { Text("Tên thư mục", color = Color(0xff6B7280)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = Color.Black,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // --- Nút Hành động ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Nút Hủy
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Hủy", fontWeight = FontWeight.Bold)
                }

                // Nút Đổi tên
                Button(
                    onClick = { onConfirm(newFolderName) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF4B0082)
                    ),
                    // Chỉ bật nút nếu tên mới khác tên cũ và không rỗng
                    enabled = newFolderName.isNotBlank() && newFolderName != folderToRename.name,
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Đổi tên", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewRenameFolderDialog() {
    MaterialTheme {
        // Dữ liệu giả lập cho Preview
        val mockFolder = Folder(
            id = 1,
            name = "Công việc",
            noteCount = 5,
            color = Color.Blue
        )
        RenameFolderDialog(
            folderToRename = mockFolder,
            onDismiss = {},
            onConfirm = { newName -> println("Đổi tên thành: $newName") }
        )
    }
}