package com.example.notecast.presentation.screen.homescreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.R
import com.example.notecast.presentation.components.Note
import com.example.notecast.presentation.components.NoteCard
import com.example.notecast.presentation.components.sampleNotes
import com.example.notecast.presentation.theme.backgroundTertiary
import com.example.notecast.presentation.theme.lightPurple
import com.example.notecast.presentation.theme.textGradient
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    drawerState: DrawerState, // Nhận DrawerState từ MainAppScreen
    notes: List<Note>,
    searchQuery: String,
    onSearch: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
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
                    .background(brush = backgroundTertiary)
                    .clickable(onClick = onAddNoteClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(R.drawable.baseline_add_24), contentDescription = "Thêm ghi chú", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Áp dụng padding của Scaffold
                .padding(horizontal = 16.dp) // Padding riêng của màn hình
        ) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                // Icon mở menu (sử dụng drawerState được truyền vào)
                Icon(
                    painter = painterResource(R.drawable.outline_menu_24),
                    contentDescription = "menu",
                    tint = Color(0xff6200AE),
                    modifier = Modifier.clickable {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )

                Text(
                    text = "NOTECAST",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(brush = textGradient)
                )
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Thanh tìm kiếm
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(brush = Brush.verticalGradient(
                        0.0f to Color(0xff4AC5EE),
                        1.0f to Color(0xff5C6FD5)
                    ))
                    .padding(horizontal = 12.dp), // Padding cho icon
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Khoảng cách giữa các icon và text
            ) {
                Icon(
                    painter = painterResource(R.drawable.search),
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )

                BasicTextField(
                    value = searchQuery,
                    onValueChange = {
                        onSearch(it)
                    },
                    modifier = Modifier.weight(1f), // << SỬA QUAN TRỌNG
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(Color.White),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        // Box này để căn chỉnh placeholder và text
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Tìm kiếm ghi chú...",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField() // Chữ bạn gõ
                        }
                    }
                )

                Icon(
                    painter = painterResource(R.drawable.outline_mic_24),
                    contentDescription = "Voice Search",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            if(!search.isEmpty()){
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Tìm thấy 3 kết quả phù hợp trong 10 ghi chú",
                    color = lightPurple,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Buttons row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(brush = backgroundTertiary)
                        .clickable(onClick = onFilterClick)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(R.drawable.outline_filter_alt_24), contentDescription = "Bộ lọc", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bộ lọc", color = Color.White)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(brush = backgroundTertiary)
                        .clickable(onClick = onSortClick)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ){
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(R.drawable.arrow_up_down), contentDescription = "Sắp xếp", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sắp xếp", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (notes.isEmpty()) {
                // Empty state message
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bg_empty),
                        contentDescription = "No notes",
                        modifier = Modifier.size(220.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Không có ghi chú nào ở đây", color = Color(0xff724C7F).copy(alpha = 0.45f))
                }
            } else {
                // Notes list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes) { note ->
                        NoteCard(
                            note = note,
                            onFavoriteClick = { onToggleFavorite(note) },
                            onPinClick = { onTogglePin(note) }
                        )
                    }
                }
            }
        }
    }
}


// --- PREVIEW ---
// (Cập nhật Preview để dùng nền tối, giả lập nền gradient)
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun PreviewHomeScreenEmpty() {
    val previewDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.DarkGray)) {
        HomeScreen(
            drawerState = previewDrawerState,
            notes = emptyList(),
            searchQuery = "",
            onSearch = {},
            onFilterClick = {},
            onSortClick = {},
            onAddNoteClick = {},
            onToggleFavorite = {},
            onTogglePin = {}
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun PreviewHomeScreenWithNotes() {
    val previewDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.DarkGray)) {
        HomeScreen(
            drawerState = previewDrawerState,
            notes = sampleNotes,
            searchQuery = "",
            onSearch = {},
            onFilterClick = {},
            onSortClick = {},
            onAddNoteClick = {},
            onToggleFavorite = {},
            onTogglePin = {}
        )
    }
}