package com.example.notecast.presentation.ui.homescreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notecast.R
import com.example.notecast.presentation.ui.common_components.NoteCard
import android.graphics.Color as AndroidColor
import com.example.notecast.presentation.ui.filter.FilterScreen
import com.example.notecast.presentation.ui.sort.SortScreen
import com.example.notecast.presentation.theme.LogoBrush
import com.example.notecast.presentation.theme.MainButtonBrush
import com.example.notecast.presentation.theme.PrimaryAccent
import com.example.notecast.presentation.theme.Purple
import com.example.notecast.presentation.theme.TabButton2Brush
import com.example.notecast.presentation.ui.common_components.NoteSelectionBar
import com.example.notecast.presentation.ui.dialog.SelectFolderDialog
import com.example.notecast.presentation.viewmodel.NoteListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import com.example.notecast.presentation.navigation.Screen

/**
 * HomeScreen: nhẹ, không quản lý dialog. Khi user nhấn FAB, HomeScreen gọi onOpenCreateDialog()
 */
@Composable
fun HomeScreen(
    drawerState: DrawerState,
    onOpenCreateDialog: () -> Unit,
    onNoteClick: (String) -> Unit,
    navController: NavController,
    // ViewModel được inject tự động tại đây
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
            onOpenCreateDialog = {
                // Ủy quyền cho MainAppScreen hiển thị CreateNoteDialog và điều hướng tương ứng
                onOpenCreateDialog()
            },

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
                    onNoteClick(noteId)
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

            // Xử lý mở màn hình TokenizerDebug
            onOpenTokenizerDebug = {
                navController.navigate(Screen.TokenizerDebug.route)
            }
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

@Composable
private fun HomeScreenContent(
    drawerState: DrawerState,
    state: NoteListState,
    isSelectionMode: Boolean,
    selectedNoteIds: List<String>,

    onEvent: (NoteListEvent) -> Unit,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    onOpenCreateDialog: () -> Unit,
    onNoteClick: (String) -> Unit,
    onNoteLongClick: (String) -> Unit,
    onSelectAllClick: () -> Unit,
    onDeleteSelected: () -> Unit,
    onMoveSelected: () -> Unit,
    onCloseSelectionMode: () -> Unit,
    onOpenTokenizerDebug: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val search = ""

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (!isSelectionMode) {
                Column {
                    // New debug button on top
//                    Box(
//                        modifier = Modifier
//                            .padding(bottom = 8.dp)
//                            .size(56.dp)
//                            .shadow(elevation = 6.dp, shape = CircleShape)
//                            .clip(CircleShape)
//                            .background(Color.Gray)
//                            .clickable { onOpenTokenizerDebug() },
//                        contentAlignment = Alignment.Center,
//                    ) {
//                        Text(text = "Test\nTok", color = Color.White, fontSize = 10.sp)
//                    }
                    // Existing FAB below
                    Box(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .size(56.dp)
                            .shadow(elevation = 6.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(brush = MainButtonBrush)
                            .clickable { onOpenCreateDialog() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(painter = painterResource(id = R.drawable.baseline_add_24), contentDescription = "Thêm ghi chú", tint = Color.White)
                    }
                }
            }
        },
        bottomBar = {
            // Hiển thị thanh thao tác khi ở chế độ chọn
            if (isSelectionMode) {
                NoteSelectionBar(
                    selectedCount = selectedNoteIds.size,
                    onMoveClick = onMoveSelected,
                    onDeleteClick = onDeleteSelected,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)

                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            // --- HEADER ---
            HomeTopAppBar(
                isSelectionMode = isSelectionMode,
                selectedCount = selectedNoteIds.size,
                drawerState = drawerState,
                scope = scope,
                onCloseSelectionMode = {
                    onCloseSelectionMode()
                },
                onSelectAllClick = onSelectAllClick,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- SEARCH BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(brush = TabButton2Brush)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )

                BasicTextField(
                    value = state.searchQuery,
                    onValueChange = { newQuery ->
                        onEvent(NoteListEvent.OnSearchQueryChanged(newQuery))
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 16.sp),
                    cursorBrush = SolidColor(Color.White),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (state.searchQuery.isEmpty()) {
                                Text(text = "Tìm kiếm ghi chú...", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
                            }
                            innerTextField()
                        }
                    }
                )
                Icon(
                    painter = painterResource(id = R.drawable.outline_mic_24),
                    contentDescription = "Voice Search",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- BUTTONS ROW ---
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(brush = MainButtonBrush)
                        .clickable(onClick = onFilterClick)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_filter_alt_24),
                            contentDescription = "Bộ lọc",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bộ lọc", color = Color.White)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(brush = MainButtonBrush)
                        .clickable(onClick = onSortClick)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_up_down),
                            contentDescription = "Sắp xếp",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sắp xếp", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- CONTENT LIST ---
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.allNotes.isEmpty()) {
                // Chưa có ghi chú nào (Empty Data)
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(painter = painterResource(id = R.drawable.bg_empty), contentDescription = "No notes", modifier = Modifier.size(180.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Không có ghi chú nào ở đây", color = Color(0xff724C7F).copy(alpha = 0.45f))
                }
            } else if (state.filteredAndSortedNotes.isEmpty()) {
                //Có ghi chú nhưng Lọc không ra (No Match)
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = "No results",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Không tìm thấy kết quả phù hợp", color = Color.White.copy(alpha = 0.7f))
                }
            } else {
                LazyColumn(

                    modifier = Modifier
                        .let {
                            if (!isSelectionMode) it.windowInsetsPadding(WindowInsets.navigationBars)
                            else it.padding(bottom = 5.dp)
                        }
                        .weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.filteredAndSortedNotes, key = { it.id }) { note ->
                        val folder = state.allFolders.find { it.id == note.folderId }
                        val folderName = folder?.name ?: "Chưa phân loại"
                        val folderColor = try {
                            if (folder?.colorHex != null) Color(AndroidColor.parseColor(folder.colorHex))
                            else Color(0xFFCCA8FF) // Màu tím nhạt cho "Chưa phân loại"
                        } catch (e: Exception) {
                            PrimaryAccent
                        }
                        val isSelected = selectedNoteIds.contains(note.id)
                        NoteCard(
                            note = note,
                            folderName = folderName,
                            folderColor = folderColor,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            onClick = { onNoteClick(note.id) },
                            onLongClick = { onNoteLongClick(note.id) },
                            onFavoriteClick = { onEvent(NoteListEvent.OnToggleFavorite(note)) },
                            onPinClick = { onEvent(NoteListEvent.OnTogglePin(note)) }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    isSelectionMode: Boolean,
    selectedCount: Int,
    drawerState: DrawerState,
    scope: CoroutineScope,
    onSelectAllClick: () -> Unit,
    onCloseSelectionMode: () -> Unit
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSelectionMode) "Đã chọn $selectedCount" else "NOTECAST",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(brush = LogoBrush)
                )
            }
        },

        navigationIcon = {
            Icon(
                painter = painterResource(
                    id = if (isSelectionMode) R.drawable.baseline_close_24
                    else R.drawable.outline_menu_24
                ),
                contentDescription = "menu",
                tint = Color(0xff6200AE),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable {
                        if (isSelectionMode) onCloseSelectionMode()
                        else scope.launch { drawerState.open() }
                    }
            )
        },
        actions = {
            if (!isSelectionMode) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(36.dp)
                )
            }else{
                IconButton(onClick = onSelectAllClick) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Select All",
                        tint = Purple
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}
