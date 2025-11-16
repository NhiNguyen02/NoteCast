package com.example.notecast.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.notecast.R

// --- DỮ LIỆU TẠM THỜI (để preview) ---
// TODO: Xóa các lớp này khi đã có model từ 'domain'
enum class NoteType { IDEA, VOICE }
enum class NoteCategory(val title: String, val color: Color) {
    IDEA("Ý tưởng", Color(0xFFFFDD57)),
    RESEARCH("Nghiên cứu", Color(0xFF9BC6FB))
}
data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val type: NoteType,
    val category: NoteCategory,
    val isFavorite: Boolean,
    val isPinned: Boolean // <-- Thêm trạng thái Pin
)
// Dữ liệu mẫu cho Preview
val sampleNotes = listOf(
    Note(1, "Ý tưởng sản phẩm mới", "Brainstorm về tính năng AI...", NoteType.IDEA, NoteCategory.IDEA, false, true), // Pinned
    Note(2, "Nghiên cứu thị trường", "Phân tích đối thủ cạnh tranh...", NoteType.IDEA, NoteCategory.RESEARCH, true, false), // Favorited
    Note(3, "Ghi âm cuộc họp", "Nội dung cuộc họp team...", NoteType.VOICE, NoteCategory.IDEA, false, false)
)
// --- KẾT THÚC DỮ LIỆU TẠM THỜI ---


@Composable
fun NoteCard(
    note: Note,
    onFavoriteClick: () -> Unit,
    onPinClick: () -> Unit // <-- Thêm sự kiện Pin
) {
    Card(
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.5f)),


    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,

                ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon and category tag
                Icon(
                    painter = painterResource(
                        when (note.type) {
                            NoteType.IDEA -> R.drawable.file_text
                            NoteType.VOICE -> R.drawable.outline_mic_24
                        }
                    ),
                    contentDescription = null,
                    tint = Color(0xFF855CF8),
                    modifier = Modifier.size(24.dp)

                )


                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.content,
                maxLines = 1,
                minLines = 1,
                overflow = TextOverflow.Ellipsis,

                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Tag badge
                Box(
                    modifier = Modifier
                        // Thêm .clip để bo góc cho background
                        .clip(RoundedCornerShape(8.dp))
                        .border(border = BorderStroke(2.dp, note.category.color), shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        note.category.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))

                // Hàng chứa các nút Pin và Favorite
                Row{
                    // Nút Pin (Ghim)
                    IconButton(onClick = onPinClick,) {
                        Icon(
                            // TODO: Thêm icon pin_filled và pin_border vào res/drawable
                            painter = painterResource( R.drawable.pin ),
                            contentDescription = "Pin",
                            tint = if (note.isPinned) Color(0xFF6200EE) else Color.Gray, // Màu tím khi Pin
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // Nút Favorite (Yêu thích)
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            // TODO: Thêm icon heart_filled và heart_border vào res/drawable
                            painter = painterResource(R.drawable.heart ),
                            contentDescription = "Favorite",
                            tint = if (note.isFavorite) Color.Red else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}