package com.example.notecast.presentation.ui.common_components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.notecast.R
import com.example.notecast.domain.model.Note



@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onPinClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(

                        when (note.noteType) {
                            "VOICE" -> R.drawable.outline_mic_24
                            else -> R.drawable.file_text
                        }
                    ),
                    contentDescription = null,
                    tint = Color(0xFF855CF8),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Tiêu đề (Giữ nguyên)
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Nội dung
            Text(
                // Hiển thị content (text note) hoặc rawText (voice note)
                text = note.content ?: (note.rawText ?: ""),
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


                Spacer(modifier = Modifier.weight(1f))

                // Hàng chứa các nút Pin và Favorite
                Row {
                    // Nút Pin
                    IconButton(onClick = onPinClick) {
                        Icon(

                            imageVector = if (note.pinTimestamp != null) Icons.Filled.PushPin
                            else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = if (note.pinTimestamp != null) Color(0xFF6200EE) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // Nút Favorite
                    IconButton(onClick = onFavoriteClick) {
                        Icon(

                            imageVector = if (note.isFavorite) Icons.Filled.Favorite
                            else Icons.Outlined.FavoriteBorder,
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