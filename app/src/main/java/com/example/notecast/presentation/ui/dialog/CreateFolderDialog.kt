package com.example.notecast.presentation.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.notecast.presentation.theme.PopUpBackgroundBrush
import com.example.notecast.presentation.theme.folderColors

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: Color) -> Unit,
    // 1. THÊM CÁC THAM SỐ ĐỂ HỖ TRỢ SỬA (có giá trị mặc định cho lúc Tạo mới)
    initialName: String = "",
    initialColor: Color = folderColors.first(),
    title: String = "Tạo thư mục mới",
    confirmButtonText: String = "Tạo thư mục"
) {
    // 2. KHỞI TẠO STATE TỪ THAM SỐ INITIAL
    var folderName by remember { mutableStateOf(initialName) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    val listState = rememberLazyListState()

    LaunchedEffect(selectedColor) {
        val index = folderColors.indexOf(selectedColor)
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(PopUpBackgroundBrush)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tiêu đề (Dùng tham số title)
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

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
                modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp)
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
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White) // Nền cho khu vực màu
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(folderColors) { color ->
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
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(50.dp).padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Hủy", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onConfirm(folderName, selectedColor) },
                    modifier = Modifier.weight(1f).height(50.dp).padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF4B0082)),
                    enabled = folderName.isNotBlank(),
                    shape = RoundedCornerShape(50)
                ) {
                    // Text nút (Dùng tham số confirmButtonText)
                    Text(confirmButtonText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// (ColorCircle giữ nguyên)
@Composable
private fun ColorCircle(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    val size = 40.dp
    val borderThickness = 4.dp
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(Color.Transparent).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(size - borderThickness).clip(CircleShape).background(color))
        if (isSelected) {

            Box(modifier = Modifier.size(size).clip(CircleShape).background(color).border(borderThickness, Color.White.copy(0.7f), CircleShape))
        }
    }
}