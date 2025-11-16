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
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.notecast.R
import com.example.notecast.presentation.ui.common_components.Note
import com.example.notecast.presentation.ui.common_components.NoteCard
import com.example.notecast.presentation.ui.common_components.sampleNotes
import com.example.notecast.presentation.theme.LogoBrush
import com.example.notecast.presentation.theme.MainButtonBrush
import com.example.notecast.presentation.theme.TabButton2Brush
import kotlinx.coroutines.launch

/**
 * HomeScreen: nhẹ, không quản lý dialog. Khi user nhấn FAB, HomeScreen gọi onOpenCreateDialog()
 */
@Composable
fun HomeScreen(
    drawerState: DrawerState,
    notes: List<Note>,
    searchQuery: String,
    onSearch: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    onOpenCreateDialog: () -> Unit,
    onAddNoteClick: () -> Unit,
    onToggleFavorite: (Note) -> Unit,
    onTogglePin: (Note) -> Unit
) {
    val scope = rememberCoroutineScope()
    val search = ""

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

            // Search bar & UI (giữ nguyên logic cũ)
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
                    value = searchQuery,
                    onValueChange = { onSearch(it) },
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 16.sp),
                    cursorBrush = SolidColor(Color.White),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty()) {
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

            // Buttons row & notes list
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
                        Icon(painter = painterResource(id = R.drawable.arrow_up_down), contentDescription = "Sắp xếp", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sắp xếp", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (notes.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(painter = painterResource(id = R.drawable.bg_empty), contentDescription = "No notes", modifier = Modifier.size(220.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Không có ghi chú nào ở đây", color = Color(0xff724C7F).copy(alpha = 0.45f))
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(notes) { note ->
                        NoteCard(note = note, onFavoriteClick = { onToggleFavorite(note) }, onPinClick = { onTogglePin(note) })
                    }
                }
            }
        }
    }
}

// Previews (giữ nguyên nếu cần)
@Preview()
@Composable
fun PreviewHomeScreen() {
    val previewDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    HomeScreen(
        drawerState = previewDrawerState,
        notes = sampleNotes,
        searchQuery = "",
        onSearch = {},
        onFilterClick = {},
        onSortClick = {},
        onOpenCreateDialog = {},
        onAddNoteClick = {},
        onToggleFavorite = {},
        onTogglePin = {}
    )
}