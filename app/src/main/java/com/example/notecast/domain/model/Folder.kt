package com.example.notecast.domain.model

import androidx.compose.ui.graphics.Color

data class Folder(
    val id: Int,
    val name: String,
    val noteCount: Int,
    val color: Color, // Dùng để tô màu cho icon/card
    val isSelected: Boolean = false // Cho chế độ chọn/sửa/xóa
)