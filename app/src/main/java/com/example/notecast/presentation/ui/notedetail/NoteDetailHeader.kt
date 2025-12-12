package com.example.notecast.presentation.ui.notedetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.PushPin
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.R
import com.example.notecast.domain.model.Folder
import com.example.notecast.presentation.theme.Purple
import com.example.notecast.presentation.ui.common_components.FolderSelectionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailHeader(
    onBack: () -> Unit,
    onSaveClick: () -> Unit,
    folderId: String?,
    availableFolders: List<Folder>,
    onFolderSelected: (Folder?) -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "Text",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Transparent // Ẩn title để giống design của bạn
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
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

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun NoteDetailHeader(
//    title: String,
//    onTitleChange: (String) -> Unit,
//    tagBg: Color,
//    tagBorder: Color,
//    tagText: Color,
//    gradientMiddle: Color,
//    onBack: () -> Unit,
//) {
//    TopAppBar(
//        title = {
//            BasicTextField(
//                value = title,
//                onValueChange = onTitleChange,
//                textStyle = TextStyle(
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.ExtraBold,
//                    color = Color.Black
//                ),
//                decorationBox = { innerTextField ->
//                    if (title.isEmpty()) {
//                        Text(
//                            "Tiêu đề ghi chú...",
//                            fontSize = 20.sp,
//                            fontWeight = FontWeight.ExtraBold,
//                            color = Color.Gray.copy(alpha = 0.5f)
//                        )
//                    }
//                    innerTextField()
//                }
//            )
//        },
//        navigationIcon = {
//            IconButton(onClick = onBack) {
//                Icon(
//                    Icons.Default.ArrowBack,
//                    contentDescription = "Back",
//                    tint = Color(0xFF4B2D80),
//                    modifier = Modifier.size(18.dp)
//                )
//            }
//        },
//        actions = {
//            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                IconButton(onClick = { /* pin */ }, modifier = Modifier.size(36.dp)) {
//                    Icon(Icons.Default.PushPin, contentDescription = "Pin", tint = gradientMiddle)
//                }
//                IconButton(onClick = { /* delete */ }, modifier = Modifier.size(36.dp)) {
//                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F))
//                }
//            }
//        },
//        colors = TopAppBarDefaults.topAppBarColors(
//            containerColor = Color.Transparent,
//        )
//    )
//}
