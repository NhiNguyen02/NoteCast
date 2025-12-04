package com.example.notecast.presentation.ui.filter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.presentation.theme.*
import com.example.notecast.presentation.ui.homescreen.FilterOptions
import com.example.notecast.presentation.ui.homescreen.NoteTypeFilter
import com.example.notecast.presentation.ui.homescreen.StatusFilter
import com.example.notecast.domain.model.Folder
import com.example.notecast.presentation.ui.homescreen.FilterCounts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ... (Các hàm helper và enum giữ nguyên) ...
private fun Modifier.bottomDivider(color: Color, strokeWidth: Dp = 1.dp) = this.then(Modifier.drawBehind { /*...*/ })
private enum class FilterFooterButton { NONE, CLEAR, APPLY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    currentOptions: FilterOptions,
    availableFolders: List<Folder>,
    counts: FilterCounts,
    onApply: (FilterOptions) -> Unit,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // 2. DÙNG COUNTS THẬT CHO FORMATS
    val formats = listOf(
        Triple("Ghi âm giọng nói", "Chỉ hiện ghi chú âm thanh", counts.voiceCount),
        Triple("Ghi chú văn bản", "Chỉ hiện ghi chú văn bản", counts.textCount)
    )


    val displayFolders = remember(availableFolders, counts) {
        val allItem = FolderItem(
            id = null, // ID null đại diện cho "Tất cả"
            name = "Tất cả thư mục",
            count = counts.allFoldersCount,
            color = FolderColor.PURPLE
        )

        val realItems = availableFolders.map { folder ->
            FolderItem(
                id = folder.id,
                name = folder.name,
                count = counts.folderCounts[folder.id] ?: 0,
                color = FolderColor.ORANGE
            )
        }
        listOf(allItem) + realItems
    }

    // State UI (Giữ nguyên)
    var selectedFormat by remember {
        mutableIntStateOf(when (currentOptions.noteType) {
            NoteTypeFilter.ALL -> -1; NoteTypeFilter.VOICE -> 0; NoteTypeFilter.TEXT -> 1
        })
    }

    var selectedFolderIndex by remember {
        val index = if (currentOptions.folderId == null) 0
        else displayFolders.indexOfFirst { it.id == currentOptions.folderId }
        // Nếu không tìm thấy (ví dụ folder bị xóa), mặc định về 0
        mutableIntStateOf(if (index >= 0) index else 0)
    }


    // 4. DÙNG COUNTS THẬT CHO STATUSES
    val statuses = listOf(
        StatusItem("Ghi chú đã ghim", "Hiển thị ghi chú quan trọng", counts.pinnedCount, StatusType.PIN),
        StatusItem("Ghi chú yêu thích", "Ghi chú được đánh dấu yêu thích", counts.favoriteCount, StatusType.HEART)
    )

    var selectedStatus by remember {
        mutableIntStateOf(when (currentOptions.status) {
            StatusFilter.NONE -> -1; StatusFilter.PINNED -> 0; StatusFilter.FAVORITE -> 1
        })
    }


    val horizontalPadding = 16.dp
    val headerDividerColor = PrimaryAccent.copy(alpha = 0.22f)
    val footerOutlineColor = Color.White.copy(alpha = 0.35f)
    val gradientTop = Color(0xFFB96CFF); val gradientMiddle = Color(0xFF8A4BFF); val gradientBottom = Color(0xFF6A2CFF)
    val footerGradientBrush = Brush.verticalGradient(listOf(gradientTop, gradientMiddle, gradientBottom))
    var pressedButton by remember { mutableStateOf(FilterFooterButton.NONE) }
    val lightWhite22 = Color.White.copy(alpha = 0.22f)
    val cardHeight = 72.dp
    val titleTextStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF222222))
    val subtitleTextStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
    val countTextStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    val sectionHeadingStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryAccentDark)
    val sectionCountStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

    Box(
        modifier = Modifier.fillMaxSize().background(brush = Background)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // HEADER (Giữ nguyên)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(PrimaryAccent.copy(alpha = 0.06f)).border(BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.22f)), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.FilterList, null, tint = Color(0xFF6200AE), modifier = Modifier.size(18.dp)) }
                    Text("Bộ lọc", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, brush = TitleBrush))
                }
                Text("Hủy", color = Color(0xFF8D8DA3), fontSize = 14.sp, modifier = Modifier.clickable { onClose() })
            }
            Divider(color = headerDividerColor, thickness = 1.dp, modifier = Modifier.fillMaxWidth())

            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // SECTION 1: FORMAT (Đã dùng counts thật)
                item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Định dạng", style = sectionHeadingStyle); Text("${if (selectedFormat >= 0) 1 else 0} đã chọn", style = sectionCountStyle) }; Spacer(Modifier.height(8.dp)) }
                itemsIndexed(formats) { index, triple ->
                    val (title, subtitle, count) = triple // Count thật
                    val selected = selectedFormat == index
                    val iconFixedTint = if (index == 0) Color(0xFF8555FF) else Color(0xFF3ECF9A)
                    val countFixedColor = iconFixedTint

                    Row(
                        modifier = Modifier.fillMaxWidth().height(cardHeight).clip(RoundedCornerShape(12.dp)).background(Color.White)
                            .then(if (selected) Modifier.border(BorderStroke(2.dp, PrimaryAccent), RoundedCornerShape(12.dp)) else Modifier.border(BorderStroke(1.dp, Color(0xFFE8E8F0)), RoundedCornerShape(12.dp)))
                            .clickable { selectedFormat = if (selected) -1 else index }.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(18.dp)).background(if (selected) PrimaryAccent else Color.Transparent).border(1.dp, if (selected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f) else Color(0xffD1D5DB), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                            if (selected) Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(Color.White)) else Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)))
                        }
                        Spacer(Modifier.width(12.dp))
                        Icon(if (index == 0) Icons.Default.Mic else Icons.Default.Description, null, tint = iconFixedTint, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) { Text(title, style = titleTextStyle); Spacer(Modifier.height(4.dp)); Text(subtitle, style = subtitleTextStyle) }
                        Text(count.toString(), style = countTextStyle, color = countFixedColor) // Hiển thị số lượng thật
                    }
                }

                // FOLDERS (Đã dùng counts thật và list thật)
                item { Spacer(Modifier.height(4.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Thư mục", style = sectionHeadingStyle); Text(if (selectedFolderIndex == 0) "Tất cả" else "1 đã chọn", style = sectionCountStyle) }; Spacer(Modifier.height(8.dp)) }
                itemsIndexed(displayFolders) { idx, folder ->
                    // So sánh index hiện tại với index đang được chọn
                    val selected = selectedFolderIndex == idx

                    FolderRow(
                        name = folder.name,
                        count = folder.count,
                        color = folder.color,
                        selected = selected,
                        onClick = {
                            // Nếu nhấn vào item đang chọn -> Không làm gì (hoặc bỏ chọn về Tất cả nếu muốn)
                            // Nếu nhấn vào item khác -> Chọn item đó
                            selectedFolderIndex = idx
                        },
                        cardHeight = cardHeight,
                        titleTextStyle = titleTextStyle,
                        countTextStyle = countTextStyle
                    )
                }

                // SECTION 3: STATUS (Đã dùng counts thật)
                item { Spacer(Modifier.height(4.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Trạng thái", style = sectionHeadingStyle); Text("${if (selectedStatus >= 0) 1 else 0} đã chọn", style = sectionCountStyle) }; Spacer(Modifier.height(8.dp)) }
                itemsIndexed(statuses) { index, status ->
                    val selected = selectedStatus == index
                    StatusRow(status.title, status.subtitle, status.count, status.type, selected, { selectedStatus = if (selected) -1 else index }, cardHeight, titleTextStyle, subtitleTextStyle, countTextStyle)
                }
                item { Spacer(Modifier.height(86.dp)) }
            }

            // FOOTER (Logic Apply giữ nguyên)
            Box(modifier = Modifier.fillMaxWidth().background(footerGradientBrush).padding(horizontal = horizontalPadding, vertical = 14.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    val clearClick = {
                        selectedFormat = -1; selectedFolderIndex = 0; selectedStatus = -1;
                        pressedButton = FilterFooterButton.CLEAR
                    }
                    if (pressedButton == FilterFooterButton.CLEAR) Button(onClick = clearClick, colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = Color.White), shape = RoundedCornerShape(28.dp), modifier = Modifier.height(44.dp).weight(1f)) { Text("Xóa bộ lọc") }
                    else OutlinedButton(onClick = clearClick, border = BorderStroke(1.dp, footerOutlineColor), colors = ButtonDefaults.outlinedButtonColors(containerColor = lightWhite22, contentColor = Color.White), shape = RoundedCornerShape(28.dp), modifier = Modifier.height(44.dp).weight(1f)) { Text("Xóa bộ lọc") }

                    Spacer(Modifier.width(12.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                pressedButton = FilterFooterButton.APPLY
                                // Lấy ID Folder (trừ index 0 là Tất cả)
                                val chosenFolderId = if (selectedFolderIndex > 0 && selectedFolderIndex < displayFolders.size) {
                                    displayFolders[selectedFolderIndex].id
                                } else {
                                    null // Index 0 hoặc lỗi -> null (Tất cả)
                                }

                                val newOptions = FilterOptions(
                                    noteType = when (selectedFormat) {
                                        0 -> NoteTypeFilter.VOICE
                                        1 -> NoteTypeFilter.TEXT
                                        else -> NoteTypeFilter.ALL
                                    },
                                    status = when (selectedStatus) {
                                        0 -> StatusFilter.PINNED
                                        1 -> StatusFilter.FAVORITE
                                        else -> StatusFilter.NONE
                                    },
                                    folderId = chosenFolderId // Truyền ID thật ra ngoài
                                )
                                onApply(newOptions)
                                delay(140); onClose()
                            }
                        },
                        colors = if (pressedButton == FilterFooterButton.APPLY) ButtonDefaults.buttonColors(containerColor = PrimaryAccent) else ButtonDefaults.buttonColors(containerColor = lightWhite22),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.height(44.dp).weight(1.2f)
                    ) { Text("Áp dụng") }
                }
            }
        }
    }
}


// --- Helpers: FolderRow / StatusRow + models ---

private enum class FolderColor { PURPLE, ORANGE, BLUE, GREEN }
private data class FolderItem(val id: String?, val name: String, val count: Int, val color: FolderColor)

@Composable
private fun FolderRow(
    name: String,
    count: Int,
    color: FolderColor,
    selected: Boolean,
    onClick: () -> Unit,
    cardHeight: Dp,
    titleTextStyle: TextStyle,
    countTextStyle: TextStyle
) {
    val folderTint = when (color) {
        FolderColor.PURPLE -> PrimaryAccent
        FolderColor.ORANGE -> Color(0xFFF59E2B)
        FolderColor.BLUE -> Color(0xFF2F9BFF)
        FolderColor.GREEN -> Color(0xFF2ECF9A)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Transparent)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .then(
                    if (selected) Modifier.border(BorderStroke(2.dp, PrimaryAccent), RoundedCornerShape(10.dp))
                    else Modifier.border(BorderStroke(1.dp, Color(0xFFE8E8F0)), RoundedCornerShape(10.dp))
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // checkbox square (consistent size)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) PrimaryAccent else Color.Transparent)
                    .border(1.dp, if (selected) PrimaryAccent else Color(0xffD1D5DB), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selected) Icon(Icons.Default.Check, contentDescription = "selected", tint = Color.White, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // folder icon tile (consistent)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = folderTint.copy(alpha = 0.9f), modifier = Modifier.size(25.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(name, style = titleTextStyle)
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(count.toString(), style = countTextStyle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}

private data class StatusItem(val title: String, val subtitle: String, val count: Int, val type: StatusType)
private enum class StatusType { PIN, HEART }

@Composable
private fun StatusRow(
    title: String,
    subtitle: String,
    count: Int,
    type: StatusType,
    selected: Boolean,
    onClick: () -> Unit,
    cardHeight: Dp,
    titleTextStyle: TextStyle,
    subtitleTextStyle: TextStyle,
    countTextStyle: TextStyle
) {
    val iconTint = when (type) {
        StatusType.PIN -> Color(0xFFFFA726)
        StatusType.HEART -> Color(0xFFE91E63)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Transparent)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .then(
                    if (selected) Modifier.border(BorderStroke(2.dp, PrimaryAccent), RoundedCornerShape(10.dp))
                    else Modifier.border(BorderStroke(1.dp, Color(0xFFE8E8F0)), RoundedCornerShape(10.dp))
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // square checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) PrimaryAccent else Color.Transparent)
                    .border(1.dp, if (selected) PrimaryAccent else Color(0xffD1D5DB), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                when (type) {
                    StatusType.PIN -> Icon(Icons.Outlined.PushPin, contentDescription = null, tint = iconTint.copy(alpha = 0.9f), modifier = Modifier.size(25.dp))
                    StatusType.HEART -> Icon(Icons.Outlined.Favorite, contentDescription = null, tint = iconTint.copy(alpha = 0.9f), modifier = Modifier.size(25.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(title, style = titleTextStyle)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, style = subtitleTextStyle)
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(count.toString(), style = countTextStyle, color = iconTint)
        }
    }
}
//
//@Preview(showBackground = true, widthDp = 360, heightDp = 800)
//@Composable
//private fun FilterScreenPreview() {
//    FilterScreen(currentOptions = FilterOptions(), onApply = {}, onClose = {})
//}