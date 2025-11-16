package com.example.notecast.presentation.ui.folderscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.domain.model.Folder
import com.example.notecast.presentation.ui.common_components.FolderCard
import com.example.notecast.presentation.ui.common_components.SelectionBar
import com.example.notecast.presentation.ui.dialog.CreateFolderDialog
import com.example.notecast.presentation.theme.Purple
import com.example.notecast.presentation.theme.TitleBrush


// Giả lập ViewModel State
data class FolderScreenState(
    val folders: List<Folder> = emptyList(),
    val isSelectionMode: Boolean = false,
    val folderToRename: Folder? = null,
)

@Composable
fun FolderScreen(
    onBackClick: () -> Unit = {},
    onNewFolderClick: () -> Unit = {},
    onFolderOpened: (Folder) -> Unit = {}
) {

    var showCreateDialog by remember { mutableStateOf(false) }

    // --- State Mock-up (Thay thế bằng ViewModel State thực tế) ---
    val initialFolders = remember {
        listOf(
            Folder(1, "Công việc", 2, Color(0xFF9370DB)),
            Folder(2, "Ý tưởng", 4, Color(0xFFDC143C)),
            Folder(3, "Tài liệu", 10, Color(0xFF3CB371)),
            Folder(4, "Lịch học", 4, Color(0xFFFF8C00)),
            Folder(5, "Họp nhóm", 3, Color(0xFF4682B4))
        )
    }

    var folders by remember { mutableStateOf(initialFolders) }
    var isSelectionMode by remember { mutableStateOf(false) }

    val selectedFolders = folders.filter { it.isSelected }
    // -----------------------------------------------------------------

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            FolderTopAppBar(
                isSelectionMode = isSelectionMode,
                onBackClick = onBackClick,
                onCloseSelectionMode = {
                    isSelectionMode = false
                    folders = folders.map { it.copy(isSelected = false) } // Bỏ chọn tất cả
                }
            )
        },
        bottomBar = {
            if (isSelectionMode) {
                SelectionBar(
                    selectedCount = selectedFolders.size,
                    onSelectAllClick = {
                        val allSelected = selectedFolders.size == folders.size
                        folders = folders.map { it.copy(isSelected = !allSelected) }
                    },
                    onEditClick = { /* Handle Edit */ },
                    onDeleteClick = { /* Handle Delete */ }
                )
            }
        },
    ) { paddingValues ->

        Column (
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xffE5E7EB))
            ){}
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center,


            ){

                if (!isSelectionMode) {
                    CreateFolderButton(
                        onNewFolderClick = {showCreateDialog = true}
                    )
                }

            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xffE5E7EB))
            ){}

            // Background chính
            LazyColumn(
                modifier = Modifier
                .fillMaxSize()
                    .padding(vertical = 12.dp ),
                // Thêm padding cho FAB nếu không ở chế độ chọn
                contentPadding = PaddingValues(bottom = if (!isSelectionMode) 80.dp else 0.dp)
            ) {
                items(folders, key = { it.id }) { folder ->
                    FolderCard(
                        folder = folder,
                        isSelectionMode = isSelectionMode,
                        onFolderClick = {
                            if (!isSelectionMode) {
                                onFolderOpened(it) // Mở thư mục
                            } else {
                                // Toggle select
                                folders = folders.map { f ->
                                    if (f.id == it.id) f.copy(isSelected = !f.isSelected) else f
                                }
                                // Thoát chế độ chọn nếu không còn gì được chọn
                                if (folders.none { f -> f.isSelected }) {
                                    isSelectionMode = false
                                }
                            }
                        },
                        onFolderLongClick = {
                            isSelectionMode = true
                            folders = folders.map { f ->
                                if (f.id == it.id) f.copy(isSelected = true) else f
                            }
                        },
                        onToggleSelect = {
                            folders = folders.map { f ->
                                if (f.id == it.id) f.copy(isSelected = !f.isSelected) else f
                            }
                            if (folders.none { f -> f.isSelected }) {
                                isSelectionMode = false
                            }
                        }
                    )
                }
            }
        }

    }

    if (showCreateDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateDialog = false }, // Hủy/Đóng Dialog
            onConfirm = { name, color ->

                
                // --- Logic Mock-up thêm thư mục mới ---
                val newFolder = Folder(
                    id = folders.size + 1,
                    name = name,
                    noteCount = 0,
                    color = color
                )
                folders = folders + newFolder
                // --- End Logic Mock-up ---

                showCreateDialog = false // Đóng Dialog sau khi tạo
            }
        )
    }
}


//TopAppBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderTopAppBar(
    isSelectionMode: Boolean,
    onBackClick: () -> Unit,
    onCloseSelectionMode: () -> Unit
) {
    TopAppBar(
        title = { Text("Quản lý thư mục", style = TextStyle(brush = TitleBrush, fontSize = 18.sp, fontWeight = FontWeight.Bold))},
        navigationIcon = {
            IconButton(onClick = if (isSelectionMode) onCloseSelectionMode else onBackClick) {
                // Sử dụng Icons.Filled.ArrowBackIos như trong mã của bạn
                Icon(
                    imageVector =if (isSelectionMode) Icons.Default.Close else Icons.Filled.ArrowBackIos,
                    contentDescription = "Quay lại",
                    tint = Purple
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Handle Search */ }) {
                Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = Purple)
            }
            if (!isSelectionMode) {
                IconButton(onClick = { /* Handle More */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Thêm", tint = Purple)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,

        ),
//
    )
}


//Tạo thư mục
@Composable
fun CreateFolderButton(onNewFolderClick: () -> Unit) {
    Button(
        onClick = onNewFolderClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = TitleBrush,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CreateNewFolder, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(10.dp))
                Text("Tạo thư mục mới", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Preview ()
@Composable
fun PreviewFolderScreen() {
    MaterialTheme {
        FolderScreen()
    }
}