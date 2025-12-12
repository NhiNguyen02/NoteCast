package com.example.notecast.presentation.ui.homescreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.R
import com.example.notecast.presentation.theme.LogoBrush
import com.example.notecast.presentation.theme.Purple
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
