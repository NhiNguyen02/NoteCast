package com.example.notecast.domain.model

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import kotlinx.serialization.Serializable

@Serializable
data class Folder(
    val id: String,
    val name: String,
    val colorHex: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
) {
    /**
     * SỬA: Thêm hàm tiện ích (Helper function) để UI (Compose)
     * có thể dễ dàng lấy đối tượng Color từ chuỗi Hex.
     */
    fun getComposeColor(): Color {
        return try {
            // Chuyển đổi Hex String (ví dụ: "#FFFFFF" hoặc "FFFFFF") sang Color
            val colorString = this.colorHex?.removePrefix("#") ?: "808080" // Màu xám nếu null
            Color(("#$colorString").toColorInt())
        } catch (e: Exception) {
            Color.Gray // Màu mặc định nếu Hex bị lỗi
        }
    }
}