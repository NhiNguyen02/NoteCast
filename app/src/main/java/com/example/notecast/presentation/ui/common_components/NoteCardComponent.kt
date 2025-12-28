package com.example.notecast.presentation.ui.common_components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Folder
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
import com.example.notecast.domain.model.NoteDomain
import com.example.notecast.domain.model.NoteType
import com.example.notecast.presentation.theme.PrimaryAccent
import com.example.notecast.presentation.ui.homescreen.NoteListEvent

@Composable
fun NoteCard(
    note: NoteDomain,
    folderName: String,
    folderColor: Color,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEvent: (NoteListEvent) -> Unit,
) {
    val iconRes = if (note.type == NoteType.AUDIO) R.drawable.outline_mic_24 else R.drawable.file_text
    val cardBorder = if (isSelected) BorderStroke(2.dp, PrimaryAccent) else null

    Card(
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            // HEADER: Icon & Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = Color(0xFF855CF8),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = note.title?.ifBlank { "Chưa có tiêu đề" } ?: "Chưa có tiêu đề",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CONTENT SNIPPET
            val contentPreview = note.normalizedText ?: note.rawText ?: "Không có nội dung"
            Text(
                text = contentPreview,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.7f),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // FOOTER
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Folder chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(
                            border = BorderStroke(1.dp, folderColor.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Folder,
                            contentDescription = null,
                            tint = folderColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = folderName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = folderColor
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (!isSelectionMode) {
                    Row {
                        // Pin icon
                        IconButton(
                            onClick = { onEvent(NoteListEvent.OnTogglePin(note)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pin",
                                tint = if (note.isPinned) Color(0xFF6200EE) else Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Favorite icon
                        IconButton(
                            onClick = { onEvent(NoteListEvent.OnToggleFavorite(note)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.heart),
                                contentDescription = "Favorite",
                                tint = if (note.isFavorite) Color.Red else Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (isSelectionMode && isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = PrimaryAccent,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp)
                            .background(Color.White, CircleShape)
                    )
                }
            }

        }
    }
}