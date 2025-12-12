package com.example.notecast.presentation.ui.homescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notecast.presentation.ui.filter.FilterScreen
import com.example.notecast.presentation.ui.sort.SortScreen
import com.example.notecast.presentation.ui.dialog.SelectFolderDialog
import com.example.notecast.presentation.viewmodel.NoteListViewModel
import com.example.notecast.domain.model.Note

/**
 * HomeScreen: nhẹ, không quản lý dialog. Khi user nhấn FAB, HomeScreen gọi onOpenCreateDialog()
 */
@Composable
fun HomeScreen(
    drawerState: DrawerState,
    onOpenCreateDialog: () -> Unit,
    onNoteClick: (Note) -> Unit,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    // --- STATE QUẢN LÝ CHỌN (SELECTION) ---
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedNoteIds = remember { mutableStateListOf<String>() }

    // Loading khi bấm tạo note ghi âm nhưng chưa điều hướng xong
    // Logic điều hướng sang màn Recording đã được xử lý ở MainAppScreen thông qua onOpenCreateDialog.
    // Vì vậy, HomeScreen không còn tự navigate nữa mà chỉ gọi callback.
    var isNavigatingToRecord by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        // NỘI DUNG CHÍNH
        HomeScreenContent(
            drawerState = drawerState,
            state = state,
            isSelectionMode = isSelectionMode,
            selectedNoteIds = selectedNoteIds,
            onEvent = viewModel::onEvent,
            onFilterClick = { showFilterDialog = true },
            onSortClick = { showSortDialog = true },
            onOpenCreateDialog = onOpenCreateDialog,

            // Xử lý click vào note
            onNoteClick = { noteId ->
                if (isSelectionMode) {
                    // Nếu đang chọn -> Toggle
                    if (selectedNoteIds.contains(noteId)) selectedNoteIds.remove(noteId)
                    else selectedNoteIds.add(noteId)

                    // Nếu bỏ chọn hết -> Tắt chế độ chọn
                    if (selectedNoteIds.isEmpty()) isSelectionMode = false
                } else {
                    // Nếu bình thường -> Mở chi tiết
                    val note = state.filteredAndSortedNotes.firstOrNull { it.id == noteId }
                    if (note != null) {
                        onNoteClick(note)
                    }
                }
            },

            // Xử lý nhấn giữ (Long click) -> Bật chế độ chọn
            onNoteLongClick = { noteId ->
                if (!isSelectionMode) {
                    isSelectionMode = true
                    selectedNoteIds.add(noteId)
                }
            },
            // Xử lý chọn tất cả
            onSelectAllClick = {
                if (selectedNoteIds.size == state.filteredAndSortedNotes.size) {
                    selectedNoteIds.clear() // Bỏ chọn tất cả
                } else {
                    selectedNoteIds.clear()
                    selectedNoteIds.addAll(state.filteredAndSortedNotes.map { it.id }) // Chọn tất cả
                }
            },

            // Xử lý xóa nhiều
            onDeleteSelected = {
                viewModel.deleteMultipleNotes(selectedNoteIds.toList())
                isSelectionMode = false
                selectedNoteIds.clear()
            },

            // Xử lý di chuyển nhiều
            onMoveSelected = {
                showMoveDialog = true
            },
            // Xử lý tắt chế độ chọn (nút Back hoặc X)
            onCloseSelectionMode = {
                isSelectionMode = false
                selectedNoteIds.clear()
            },
        )

        // Overlay loading khi chuẩn bị chuyển sang màn ghi âm
        // Giữ lại state này nếu sau này cần hiển thị trong Home khi MainAppScreen đang xử lý điều hướng.
        if (isNavigatingToRecord) {
            Dialog(onDismissRequest = { }) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }


        if (showFilterDialog) {
            FilterScreen(
                currentOptions = state.filterOptions,
                availableFolders = state.allFolders,
                counts = state.filterCounts, // <-- TRUYỀN COUNTS VÀO
                onApply = { newOptions ->
                    viewModel.onEvent(NoteListEvent.OnApplyFilters(newOptions))
                },
                onClose = { showFilterDialog = false }
            )
        }


        if (showSortDialog) {
            SortScreen(
                currentOptions = state.sortOptions,
                onApply = { newOptions ->
                    viewModel.onEvent(NoteListEvent.OnApplySort(newOptions))
                },
                onClose = { showSortDialog = false }
            )
        }
        if (showMoveDialog) {
            SelectFolderDialog(
                folders = state.allFolders,
                onDismiss = { showMoveDialog = false },
                onFolderSelected = { targetFolder ->
                    // Gọi ViewModel di chuyển
                    viewModel.moveNotesToFolder(selectedNoteIds.toList(), targetFolder?.id)
                    showMoveDialog = false
                    isSelectionMode = false
                    selectedNoteIds.clear()
                }
            )
        }
    }
}
