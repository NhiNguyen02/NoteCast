package com.example.notecast.presentation.ui.folderscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.domain.model.Folder
import com.example.notecast.presentation.ui.dialog.CreateFolderDialog
import com.example.notecast.presentation.theme.Purple
import com.example.notecast.presentation.theme.TitleBrush
import java.util.UUID


data class UiFolder(
    val folder: Folder,
    val isSelected: Boolean = false
)

data class Note(
    val id: String,
    val folderId: String,
    val title: String,
    val contentPreview: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Composable
fun FolderScreen(
    onBackClick: () -> Unit = {},
    onNewFolderClick: () -> Unit = {},
    onFolderOpened: (Folder) -> Unit = {},
    onFolderSelected: ((Folder) -> Unit)? = null // when non-null, screen acts as "picker"
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    val initialDomains = remember {
        listOf(
            Folder(
                id = "1",
                name = "Công việc",
                colorHex = "#9370DB",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Folder(
                id = "2",
                name = "Ý tưởng",
                colorHex = "#DC143C",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Folder(
                id = "3",
                name = "Tài liệu",
                colorHex = "#3CB371",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Folder(
                id = "4",
                name = "Lịch học",
                colorHex = "#FF8C00",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Folder(
                id = "5",
                name = "Họp nhóm",
                colorHex = "#4682B4",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    var uiFolders by remember { mutableStateOf(initialDomains.map { UiFolder(it) }) }
    var isSelectionMode by remember { mutableStateOf(false) }

    var openedFolder by remember { mutableStateOf<Folder?>(null) }

    val allNotes = remember {
        listOf(
            Note(id = "n1", folderId = "1", title = "Meeting kết quả", contentPreview = "Ghi chú cuộc họp..."),
            Note(id = "n2", folderId = "1", title = "Task tuần", contentPreview = "Các task cần làm..."),
            Note(id = "n3", folderId = "2", title = "Ý tưởng app X", contentPreview = "Mô tả ý tưởng..."),
            Note(id = "n4", folderId = "3", title = "Tài liệu A", contentPreview = "Tổng hợp tài liệu..."),
            Note(id = "n5", folderId = "4", title = "Lịch học HK1", contentPreview = "Lịch và môn học...")
        )
    }

    val selectedFolders = uiFolders.filter { it.isSelected }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            if (openedFolder == null) {
                FolderTopAppBar(
                    isSelectionMode = isSelectionMode,
                    onBackClick = onBackClick,
                    onCloseSelectionMode = {
                        isSelectionMode = false
                        uiFolders = uiFolders.map { it.copy(isSelected = false) }
                    }
                )
            } else {
                NotesTopAppBar(
                    folder = openedFolder!!,
                    onBack = { openedFolder = null }
                )
            }
        },
        bottomBar = {
            if (isSelectionMode && openedFolder == null) {
                SelectionBar(
                    selectedCount = selectedFolders.size,
                    onSelectAllClick = {
                        val allSelected = selectedFolders.size == uiFolders.size
                        uiFolders = uiFolders.map { it.copy(isSelected = !allSelected) }
                    },
                    onEditClick = { /* Handle Edit */ },
                    onDeleteClick = { /* Handle Delete */ }
                )
            }
        },
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xffE5E7EB))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                if (openedFolder == null && !isSelectionMode) {
                    CreateFolderButton(onNewFolderClick = { showCreateDialog = true })
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xffE5E7EB))
            )

            if (openedFolder != null && onFolderSelected == null) {
                val folderNotes = allNotes.filter { it.folderId == openedFolder!!.id }
                NotesScreen(folder = openedFolder!!, notes = folderNotes, onNoteClick = { /* handle open note */ })
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                    contentPadding = PaddingValues(bottom = if (!isSelectionMode) 80.dp else 0.dp)
                ) {
                    items(uiFolders, key = { it.folder.id }) { uiFolder ->
                        FolderCard(
                            folder = uiFolder.folder,
                            isSelectionMode = isSelectionMode,
                            isSelected = uiFolder.isSelected,
                            onFolderClick = { folderClicked ->
                                // If the screen is used as a picker, selecting a folder should return it
                                if (onFolderSelected != null) {
                                    onFolderSelected(folderClicked)
                                    // Optionally navigate back - caller decides; calling onBackClick helps common navigation patterns
                                    onBackClick()
                                } else if (!isSelectionMode) {
                                    openedFolder = folderClicked
                                    onFolderOpened(folderClicked)
                                } else {
                                    uiFolders = uiFolders.map {
                                        if (it.folder.id == folderClicked.id) it.copy(isSelected = !it.isSelected) else it
                                    }
                                    if (uiFolders.none { it.isSelected }) isSelectionMode = false
                                }
                            },
                            onFolderLongClick = { folderLongClicked ->
                                isSelectionMode = true
                                uiFolders = uiFolders.map {
                                    if (it.folder.id == folderLongClicked.id) it.copy(isSelected = true) else it
                                }
                            },
                            onToggleSelect = { folderToToggle ->
                                uiFolders = uiFolders.map {
                                    if (it.folder.id == folderToToggle.id) it.copy(isSelected = !it.isSelected) else it
                                }
                                if (uiFolders.none { it.isSelected }) isSelectionMode = false
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, color ->
                val hex = String.format("#%06X", 0xFFFFFF and color.toArgb())
                val newDomainFolder = Folder(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    colorHex = hex,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                uiFolders = uiFolders + UiFolder(newDomainFolder)
                showCreateDialog = false
            }
        )
    }
}

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
                Icon(
                    imageVector = if (isSelectionMode) Icons.Default.Close else Icons.Filled.ArrowBackIos,
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
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesTopAppBar(folder: Folder, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(folder.name, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold), color = Purple)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBackIos, contentDescription = "Back", tint = Purple)
            }
        },
        actions = {
            IconButton(onClick = { /* search notes in folder */ }) {
                Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = Purple)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun NotesScreen(folder: Folder, notes: List<Note>, onNoteClick: (Note) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (notes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không có ghi chú trong thư mục này", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(notes, key = { it.id }) { note ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                        ) {
                            Text(text = note.title, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(text = note.contentPreview, color = Color.Gray, maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}

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

@Preview
@Composable
fun PreviewFolderScreen() {
    MaterialTheme {
        FolderScreen()
    }
}