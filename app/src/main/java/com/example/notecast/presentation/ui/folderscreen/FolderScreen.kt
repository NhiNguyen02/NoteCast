package com.example.notecast.presentation.ui.folderscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notecast.domain.model.Folder
import com.example.notecast.presentation.ui.dialog.CreateFolderDialog
import com.example.notecast.presentation.ui.dialog.FolderColors
import com.example.notecast.presentation.ui.dialog.SelectFolderDialog // <-- IMPORT DIALOG CHỌN
import com.example.notecast.presentation.theme.Purple
import com.example.notecast.presentation.theme.TitleBrush
import com.example.notecast.presentation.viewmodel.FolderViewModel
import com.example.notecast.presentation.ui.common_components.NoteCard
// Import NoteSelectionBar (nếu bạn để ở homescreen package)
import com.example.notecast.presentation.ui.common_components.NoteSelectionBar
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    onBackClick: () -> Unit,
    onFolderSelected: ((Folder) -> Unit)? = null,
    onNoteClick: (String) -> Unit = {},
    viewModel: FolderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // --- State UI ---
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var folderToEdit by remember { mutableStateOf<Folder?>(null) }

    // State chọn Folder
    var isFolderSelectionMode by remember { mutableStateOf(false) }
    val selectedFolderIds = remember { mutableStateListOf<String>() }

    // State chọn Note (khi mở folder)
    var isNoteSelectionMode by remember { mutableStateOf(false) }
    val selectedNoteIds = remember { mutableStateListOf<String>() }
    var showMoveNoteDialog by remember { mutableStateOf(false) } // Dialog di chuyển note

    var openedFolder by remember { mutableStateOf<Folder?>(null) }

    // Tải note khi mở folder
    LaunchedEffect(openedFolder) {
        if (openedFolder != null) {
            viewModel.loadNotesByFolder(openedFolder!!.id)
        } else {
            // Khi quay lại list folder, tắt chế độ chọn note
            isNoteSelectionMode = false
            selectedNoteIds.clear()
        }
    }

    // Helper Hex -> Color
    fun hexToColor(hex: String?): Color {
        return try {
            if (hex.isNullOrBlank()) FolderColors.first()
            else Color(hex.toColorInt())
        } catch (_: Exception) { FolderColors.first() }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (openedFolder == null) {
                // --- TOP BAR DANH SÁCH FOLDER ---
                FolderTopAppBar(
                    isSelectionMode = isFolderSelectionMode,
                    selectedCount = selectedFolderIds.size,
                    onBackClick = onBackClick,
                    onCloseSelectionMode = {
                        isFolderSelectionMode = false
                        selectedFolderIds.clear()
                    },

                    onSelectAllClick = {
                        if (selectedFolderIds.size == state.folders.size)
                            selectedFolderIds.clear()
                        else {
                            selectedFolderIds.clear()
                            selectedFolderIds.addAll(state.folders.map { it.id })
                        }
                    }
                )
            } else {
                // --- TOP BAR DANH SÁCH NOTE (BÊN TRONG FOLDER) ---
                NotesTopAppBar(
                    folder = openedFolder!!,
                    isSelectionMode = isNoteSelectionMode,
                    selectedCount = selectedNoteIds.size,
                    onBack = {
                        if (isNoteSelectionMode) {
                            isNoteSelectionMode = false
                            selectedNoteIds.clear()
                        } else {
                            openedFolder = null
                        }
                    },
                    onSelectAllClick = {
                        if (selectedNoteIds.size == state.folderNotes.size) selectedNoteIds.clear()
                        else { selectedNoteIds.clear(); selectedNoteIds.addAll(state.folderNotes.map { it.id }) }
                    },
                )
            }
        },
        bottomBar = {
            // --- BOTTOM BAR KHI CHỌN FOLDER ---
            if (isFolderSelectionMode && openedFolder == null) {
                SelectionBar(
                    selectedCount = selectedFolderIds.size,
                    onEditClick = {
                        val folderId = selectedFolderIds.firstOrNull()
                        if (folderId != null) {
                            folderToEdit = state.folders.find { it.id == folderId }
                            if (folderToEdit != null) showEditDialog = true
                        }
                    },
                    onDeleteClick = {
                        selectedFolderIds.forEach { id -> viewModel.deleteFolder(id) }
                        isFolderSelectionMode = false; selectedFolderIds.clear()
                    },
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)

                )
            }

            // --- BOTTOM BAR KHI CHỌN NOTE ---
            if (isNoteSelectionMode && openedFolder != null) {
                NoteSelectionBar(
                    selectedCount = selectedNoteIds.size,
                    onMoveClick = { showMoveNoteDialog = true }, // Mở Dialog chọn folder đích
                    onDeleteClick = {
                        viewModel.deleteMultipleNotes(selectedNoteIds.toList())
                        isNoteSelectionMode = false
                        selectedNoteIds.clear()
                    },
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Spacer(Modifier.height(8.dp))
            // DANH SÁCH GHI CHÚ (BÊN TRONG FOLDER)
            if (openedFolder != null && onFolderSelected == null) {
                // Parse màu của folder hiện tại
                val currentFolderColor = hexToColor(openedFolder!!.colorHex)

                if (state.folderNotes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Không có ghi chú trong thư mục này", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding( horizontal = 16.dp)
                            .let {
                                if (!isNoteSelectionMode) it.windowInsetsPadding(WindowInsets.navigationBars)
                                else it.padding(bottom = 5.dp)
                            }
                        ,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = if (isNoteSelectionMode) 80.dp else 0.dp)
                    ) {
                        items(state.folderNotes, key = { it.id }) { note ->
                            val isSelected = selectedNoteIds.contains(note.id)

                            NoteCard(
                                note = note,
                                folderName = openedFolder!!.name,
                                folderColor = currentFolderColor,
                                // Logic chọn Note
                                isSelectionMode = isNoteSelectionMode,
                                isSelected = isSelected,
                                onLongClick = {
                                    if (!isNoteSelectionMode) {
                                        isNoteSelectionMode = true
                                        selectedNoteIds.add(note.id)
                                    }
                                },
                                onClick = {
                                    if (isNoteSelectionMode) {
                                        if (isSelected) selectedNoteIds.remove(note.id)
                                        else selectedNoteIds.add(note.id)
                                        if (selectedNoteIds.isEmpty()) isNoteSelectionMode = false
                                    } else {
                                        onNoteClick(note.id) // Mở chi tiết note
                                    }
                                },
                                onFavoriteClick = { viewModel.toggleFavorite(note) },
                                onPinClick = { viewModel.togglePin(note) }
                            )
                        }
                    }

                }
            }

            // --- MÀN HÌNH 2: DANH SÁCH FOLDER (MẶC ĐỊNH) ---
            else {
                // ... (Phần hiển thị danh sách Folder giữ nguyên như cũ) ...
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xffE5E7EB)))
                Box(modifier = Modifier.fillMaxWidth().background(Color.Transparent), contentAlignment = Alignment.Center) {
                    if (!isFolderSelectionMode) CreateFolderButton(onNewFolderClick = { showCreateDialog = true })
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xffE5E7EB)))

                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else if (state.folders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Chưa có thư mục nào", color = Color.Gray) }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)
                            .let {
                                if (!isFolderSelectionMode) it.windowInsetsPadding(WindowInsets.navigationBars)
                                else it.padding(bottom = 5.dp)
                            },
                        contentPadding = PaddingValues(bottom = if (isFolderSelectionMode) 80.dp else 0.dp)
                    ) {
                        items(state.folders, key = { it.id }) { folder ->
                            val isSelected = selectedFolderIds.contains(folder.id)
                            FolderCard(
                                folder = folder,
                                isSelectionMode = isFolderSelectionMode,
                                isSelected = isSelected,
                                noteCount = state.noteCounts[folder.id] ?: 0,
                                onFolderClick = { clickedFolder ->
                                    if (isFolderSelectionMode) {
                                        if (isSelected) selectedFolderIds.remove(clickedFolder.id)
                                        else selectedFolderIds.add(clickedFolder.id)
                                        if (selectedFolderIds.isEmpty()) isFolderSelectionMode = false
                                    } else {
                                        if (onFolderSelected != null) {
                                            onFolderSelected(clickedFolder); onBackClick()
                                        } else {
                                            openedFolder = clickedFolder // Mở Folder
                                        }
                                    }
                                },
                                onFolderLongClick = { longClickedFolder ->
                                    if (!isFolderSelectionMode) {
                                        isFolderSelectionMode = true
                                        selectedFolderIds.add(longClickedFolder.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        // --- CÁC DIALOG ---

        // 1. Dialog Tạo Folder
        if (showCreateDialog) {
            CreateFolderDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { name, color ->
                    val hex = String.format("#%06X", 0xFFFFFF and color.toArgb())
                    viewModel.createFolder(Folder(id = "", name = name, colorHex = hex, createdAt = 0, updatedAt = 0))
                    showCreateDialog = false
                }
            )
        }

        // 2. Dialog Sửa Folder
        if (showEditDialog && folderToEdit != null) {
            CreateFolderDialog(
                onDismiss = { showEditDialog = false; folderToEdit = null },
                initialName = folderToEdit!!.name,
                initialColor = hexToColor(folderToEdit!!.colorHex),
                title = "Đổi tên thư mục", confirmButtonText = "Lưu thay đổi",
                onConfirm = { name, color ->
                    val hex = String.format("#%06X", 0xFFFFFF and color.toArgb())
                    viewModel.createFolder(folderToEdit!!.copy(name = name, colorHex = hex, updatedAt = System.currentTimeMillis()))
                    showEditDialog = false; folderToEdit = null; isFolderSelectionMode = false; selectedFolderIds.clear()
                }
            )
        }

        // 3. Dialog Di chuyển Note (Mới)
        if (showMoveNoteDialog) {
            SelectFolderDialog(
                folders = state.folders, // Danh sách folder để chọn đích đến
                onDismiss = { showMoveNoteDialog = false },
                onFolderSelected = { targetFolder ->
                    // Gọi ViewModel để di chuyển các note đang chọn
                    viewModel.moveNotesToFolder(selectedNoteIds.toList(), targetFolder?.id)

                    showMoveNoteDialog = false
                    isNoteSelectionMode = false
                    selectedNoteIds.clear()
                }
            )
        }
    }

}

// Cập nhật TopBar để hiển thị số lượng đã chọn
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderTopAppBar(
    isSelectionMode: Boolean,
    selectedCount: Int = 0, // Thêm tham số này
    onBackClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onCloseSelectionMode: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                if (isSelectionMode) "Đã chọn $selectedCount" else "Quản lý thư mục",
                style = TextStyle(brush = TitleBrush, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
        },
        navigationIcon = {
            IconButton(onClick = if (isSelectionMode) onCloseSelectionMode else onBackClick) {
                Icon(if (isSelectionMode) Icons.Default.Close else Icons.Filled.ArrowBackIos, "Back", tint = Purple)
            }
        },
        actions = {
            if (isSelectionMode) {
                IconButton(onClick = onSelectAllClick) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Select All",
                        tint = Purple
                    )
                }
            } else {
                // Bình thường hiển thị Search như cũ
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Search, "Search", tint = Purple)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesTopAppBar(
    folder: Folder,
    isSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    onSelectAllClick: () -> Unit,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                if (isSelectionMode) "Đã chọn $selectedCount" else folder.name,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = Purple
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(if (isSelectionMode) Icons.Default.Close else Icons.Filled.ArrowBackIos, "Back", tint = Purple)
            }
        },
        actions = {
            if (isSelectionMode) {
                IconButton(onClick = onSelectAllClick) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Select All",
                        tint = Purple
                    )
                }
            }
            else { Icon(Icons.Default.Search, "Search", tint = Purple) } },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}


@Composable
fun CreateFolderButton(onNewFolderClick: () -> Unit) {
    Button(
        onClick = onNewFolderClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(brush = TitleBrush, shape = RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CreateNewFolder, null, tint = Color.White)
                Spacer(Modifier.width(10.dp))
                Text("Tạo thư mục mới", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}