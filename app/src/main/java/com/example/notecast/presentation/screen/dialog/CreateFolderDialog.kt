package com.example.notecast.presentation.screen.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.notecast.presentation.theme.backgroundDialog

// --- Hằng số UI ---

// Danh sách màu sắc cho thư mục
val FolderColors = listOf(
    Color(0xFF1E90FF), // Xanh dương
    Color(0xFF3CB371), // Xanh lá
    Color(0xFFFF8C00), // Cam
    Color(0xFFDC143C), // Đỏ tươi
    Color(0xFF9370DB)  // Tím nhạt
)
// --- End Hằng số UI ---

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: Color) -> Unit
) {
    // State cho tên thư mục
    var folderName by remember { mutableStateOf("") }
    // State cho màu sắc được chọn (mặc định là màu đầu tiên)
    var selectedColor by remember { mutableStateOf(FolderColors.first()) }

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
                text = "Tạo thư mục mới",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Mô tả
            Text(
                text = "Nhập tên cho thư mục của bạn",
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
                value = folderName,
                onValueChange = { folderName = it },
                placeholder = {Text("Tên thư mục", color = Color(0xff6B7280))},
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

            Spacer(modifier = Modifier.height(20.dp))

            // --- Bộ chọn Màu sắc ---
            Text(
                text = "Màu sắc",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White) // Nền mờ cho khu vực màu
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FolderColors.forEach { color ->
                    ColorCircle(
                        color = color,
                        isSelected = color == selectedColor,
                        onClick = { selectedColor = color }
                    )
                }
            }

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

                // Nút Tạo thư mục
                Button(
                    onClick = { onConfirm(folderName, selectedColor) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF4B0082) // Màu Tím đậm cho chữ
                    ),
                    enabled = folderName.isNotBlank(),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Tạo thư mục", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Composable cho một vòng tròn màu sắc
@Composable
private fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val size = 36.dp
    val borderThickness = 4.dp

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Vòng viền trắng/xanh khi được chọn
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(size - borderThickness)
                    .clip(CircleShape)
                    .background(color), // Background bên trong
            )
            // Vòng viền ngoài
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .border(borderThickness / 2, Color.White, CircleShape)
            )
        }
    }
}

@Preview
@Composable
fun PreviewCreateFolderDialog() {
    MaterialTheme {
        CreateFolderDialog(
            onDismiss = {},
            onConfirm = { name, color -> println("Tạo: $name, $color") }
        )
    }
}

