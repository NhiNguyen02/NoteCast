package com.example.notecast.presentation.ui.noteeditscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.notecast.R
import com.example.notecast.domain.model.Folder
import com.example.notecast.presentation.theme.Purple
import com.example.notecast.presentation.ui.common_components.FolderSelectionButton

// --- Composable: Top AppBar tùy chỉnh ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditTopBar(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    folderId: String?,
    availableFolders: List<Folder>,
    onFolderSelected: (Folder?) -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "Text Notes App",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Transparent // Ẩn title để giống design của bạn
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBackIos, contentDescription = "Quay lại", tint = Purple)
            }
        },
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FolderSelectionButton(
                    currentFolderId = folderId,
                    availableFolders = availableFolders,
                    onFolderSelected = onFolderSelected
                )
                // Nút Lưu
                IconButton(onClick = onSaveClick) {
                    Icon(
                        painter = painterResource(R.drawable.save), // Đảm bảo bạn có icon này
                        contentDescription = "Lưu",
                        tint = Purple,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        )
    )
}