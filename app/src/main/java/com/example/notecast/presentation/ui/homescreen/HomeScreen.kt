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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notecast.R
import com.example.notecast.domain.model.Note
import com.example.notecast.presentation.ui.common_components.NoteCard

import com.example.notecast.presentation.ui.filter.FilterScreen
import com.example.notecast.presentation.ui.sort.SortScreen

import com.example.notecast.presentation.theme.LogoBrush
import com.example.notecast.presentation.theme.MainButtonBrush
import com.example.notecast.presentation.theme.TabButton2Brush
import com.example.notecast.presentation.viewmodel.NoteListViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    drawerState: DrawerState,
    onOpenCreateDialog: () -> Unit,
    onNoteClick: (String) -> Unit,
    // ViewModel được inject tự động tại đây
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // HomeScreen tự quản lý việc hiển thị Dialog của nó
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. NỘI DUNG CHÍNH
        HomeScreenContent(
            drawerState = drawerState,
            state = state,
            onEvent = viewModel::onEvent,
            // Khi nhấn nút ở UI, chỉ cần bật cờ local tại đây
            onFilterClick = { showFilterDialog = true },
            onSortClick = { showSortDialog = true },
            onOpenCreateDialog = onOpenCreateDialog,
            onNoteClick = onNoteClick
        )

        // 2. LỚP PHỦ FILTER (Hiển thị khi cờ bật)
        if (showFilterDialog) {
            FilterScreen(
                currentOptions = state.filterOptions,
                onApply = { newOptions ->
                    viewModel.onEvent(NoteListEvent.OnApplyFilters(newOptions))
                },
                onClose = { showFilterDialog = false }
            )
        }

        // 3. LỚP PHỦ SORT (Hiển thị khi cờ bật)
        if (showSortDialog) {
            SortScreen(
                currentOptions = state.sortOptions,
                onApply = { newOptions ->
                    viewModel.onEvent(NoteListEvent.OnApplySort(newOptions))
                },
                onClose = { showSortDialog = false }
            )
        }
    }
}

@Composable
private fun HomeScreenContent(
    drawerState: DrawerState,
    state: NoteListState,
    onEvent: (NoteListEvent) -> Unit,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    onOpenCreateDialog: () -> Unit,
    onNoteClick: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(brush = MainButtonBrush)
                    .clickable { onOpenCreateDialog() },
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(id = R.drawable.baseline_add_24), contentDescription = "Thêm ghi chú", tint = Color.White)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_menu_24),
                    contentDescription = "menu",
                    tint = Color(0xff6200AE),
                    modifier = Modifier.clickable {
                        scope.launch { drawerState.open() }
                    }
                )
                Text(
                    text = "NOTECAST",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(brush = LogoBrush)
                )
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(36.dp)
                )
            }

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
                // Trường hợp 1: Chưa có ghi chú nào (Empty Data)
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(painter = painterResource(id = R.drawable.bg_empty), contentDescription = "No notes", modifier = Modifier.size(220.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Không có ghi chú nào ở đây", color = Color(0xff724C7F).copy(alpha = 0.45f))
                }
            } else if (state.filteredAndSortedNotes.isEmpty()) {
                // Trường hợp 2: Có ghi chú nhưng Lọc không ra (No Match)
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
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.filteredAndSortedNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onFavoriteClick = { onEvent(NoteListEvent.OnToggleFavorite(note)) },
                            onPinClick = { onEvent(NoteListEvent.OnTogglePin(note)) }
                        )
                    }
                }
            }
        }
    }
}

//@Preview()
//@Composable
//fun PreviewHomeScreen() {
//    val previewDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
//    val notes = listOf<Note>()
//    val previewState = NoteListState(
//        isLoading = false,
//        allNotes = notes,
//        filteredAndSortedNotes = notes
//    )
//
//    HomeScreenContent(
//        drawerState = previewDrawerState,
//        state = previewState,
//        onEvent = {},
//        onFilterClick = {},
//        onSortClick = {},
//        onOpenCreateDialog = {},
//        onNoteClick = {}
//    )
//}