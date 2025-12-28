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
import com.example.notecast.domain.model.NoteDomain

/**
 * HomeScreen: nhẹ, không quản lý điều hướng. Khi user nhấn FAB, HomeScreen gọi onOpenCreateDialog()
 * onNoteClick trả về NoteDomain để MainAppScreen quyết định route (NoteText vs NoteAudio).
 */
@Composable
fun HomeScreen(
    drawerState: DrawerState,
    onOpenCreateDialog: () -> Unit,
    onNoteClick: (NoteDomain) -> Unit,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    // --- STATE QUẢN LÝ CHỌN (SELECTION) ---
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedNoteIds = remember { mutableStateListOf<String>() }

    // Loading khi bấm tạo note ghi âm nhưng chưa điều hướng xong (giữ để dùng sau nếu cần)
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
                    if (selectedNoteIds.contains(noteId)) selectedNoteIds.remove(noteId)
                    else selectedNoteIds.add(noteId)

                    if (selectedNoteIds.isEmpty()) isSelectionMode = false
                } else {
                    // Bình thường -> Mở chi tiết: tìm NoteDomain trong state và trả lên callback
                    val note = state.filteredAndSortedNotes.firstOrNull { it.id == noteId }
                    if (note != null) {
                        onNoteClick(note)
                    }
                }
            },

            onNoteLongClick = { noteId ->
                if (!isSelectionMode) {
                    isSelectionMode = true
                    selectedNoteIds.add(noteId)
                }
            },
            onSelectAllClick = {
                if (selectedNoteIds.size == state.filteredAndSortedNotes.size) {
                    selectedNoteIds.clear()
                } else {
                    selectedNoteIds.clear()
                    selectedNoteIds.addAll(state.filteredAndSortedNotes.map { it.id })
                }
            },
            onDeleteSelected = {
                viewModel.deleteMultipleNotes(selectedNoteIds.toList())
                isSelectionMode = false
                selectedNoteIds.clear()
            },
            onMoveSelected = {
                showMoveDialog = true
            },
            onCloseSelectionMode = {
                isSelectionMode = false
                selectedNoteIds.clear()
            },
        )

        // Optional overlay loading khi chuẩn bị chuyển sang màn ghi âm
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
                counts = state.filterCounts,
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
                    viewModel.moveNotesToFolder(selectedNoteIds.toList(), targetFolder?.id)
                    showMoveDialog = false
                    isSelectionMode = false
                    selectedNoteIds.clear()
                }
            )
        }
    }
}
