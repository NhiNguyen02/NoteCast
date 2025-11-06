package com.example.notecast.presentation.screen.filter

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.notecast.presentation.theme.*
import androidx.compose.foundation.interaction.MutableInteractionSource as InteractionSourceAlias

/** draw a thin divider at bottom */
private fun Modifier.bottomDivider(color: Color, strokeWidth: Dp = 1.dp) = this.then(
    Modifier.drawBehind {
        drawLine(
            color = color,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth.toPx()
        )
    }
)

// Footer button state enum (file-level to avoid Kotlin local-class/modifier errors)
private enum class FilterFooterButton { NONE, CLEAR, APPLY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(onClose: () -> Unit) {
    // sample data & UI state
    val formats = listOf(
        Triple("Ghi âm giọng nói", "23 ghi chú", 23),
        Triple("Ghi chú văn bản", "45 ghi chú", 45)
    )
    var selectedFormats by remember { mutableStateOf(setOf(0)) }

    val folders = remember {
        mutableStateListOf(
            FolderItem("Tất cả thư mục", 68, FolderColor.PURPLE),
            FolderItem("Công việc", 24, FolderColor.ORANGE),
            FolderItem("Cá nhân", 19, FolderColor.BLUE),
            FolderItem("Dự án", 13, FolderColor.GREEN)
        )
    }
    var selectedFolders by remember { mutableStateOf(setOf(0)) }

    val statuses = listOf(
        StatusItem("Ghi chú đã ghim", "Hiển thị ghi chú quan trọng", 7, StatusType.PIN),
        StatusItem("Ghi chú yêu thích", "Ghi chú được đánh dấu yêu thích", 12, StatusType.HEART)
    )
    var selectedStatuses by remember { mutableStateOf(setOf(1)) }

    // Shared horizontal padding for inner content
    val horizontalPadding = 16.dp

    // Colors tuned
    val headerDividerColor = PrimaryAccent.copy(alpha = 0.22f)
    val footerOutlineColor = Color.White.copy(alpha = 0.35f)

    // Dialog gradient colors (use same as CreateNoteDialog)
    val gradientTop = Color(0xFFB96CFF)   // #B96CFF
    val gradientMiddle = Color(0xFF8A4BFF) // #8A4BFF
    val gradientBottom = Color(0xFF6A2CFF) // #6A2CFF
    val footerGradientBrush = Brush.verticalGradient(listOf(gradientTop, gradientMiddle, gradientBottom))

    // Footer button appearance state
    var pressedButton by remember { mutableStateOf(FilterFooterButton.NONE) }
    // initial "light" button background (whiter) for contrast on purple gradient
    val lightWhite22 = Color.White.copy(alpha = 0.22f)

    // Consistent sizing & typography for cards
    val cardHeight = 72.dp
    val titleTextStyle = MaterialTheme.typography.titleMedium.copy(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF222222)
    )
    val subtitleTextStyle = MaterialTheme.typography.bodySmall.copy(
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
    )
    val countTextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )

    // Headings "Định dạng" / "Trạng thái" style (bigger & bolder per request)
    val sectionHeadingStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryAccentDark
    )
    val sectionCountStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

    Box(
        // Use the same background as HomeScreen by using backgroundPrimary from theme
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundPrimary)
            // consume clicks so events don't fall through
            .clickable(
                indication = null,
                interactionSource = remember { InteractionSourceAlias() }
            ) { /* consume clicks to prevent click-through */ }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // HEADER (content padded, divider full-width)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryAccent.copy(alpha = 0.06f))
                            .border(BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.22f)), RoundedCornerShape(8.dp))
                            .clickable(
                                indication = null,
                                interactionSource = remember { InteractionSourceAlias() }
                            ) { /* consume click */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            tint = Color(0xFF6200AE),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "Bộ lọc",
                        style = TextStyle(brush = textGradient, fontSize = 20.sp)
                    )
                }

                Text(
                    text = "Hủy",
                    color = Color(0xFF8D8DA3),
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onClose() }
                )
            }

            Divider(
                color = headerDividerColor,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "Định dạng" heading (bigger & bolder)
                        Text("Định dạng", style = sectionHeadingStyle)
                        Text("${selectedFormats.size} đã chọn", style = sectionCountStyle)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Định dạng rows (consistent size and typography) — SELECTION INDICATOR IS CIRCULAR
                itemsIndexed(formats) { index, triple ->
                    val (title, subtitle, count) = triple
                    val selected = selectedFormats.contains(index)
                    val iconFixedTint = if (index == 0) Color(0xFF8555FF) else Color(0xFF3ECF9A)
                    val countFixedColor = iconFixedTint

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(cardHeight)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .then(
                                if (selected) Modifier.border(BorderStroke(2.dp, PrimaryAccent), RoundedCornerShape(12.dp))
                                else Modifier.border(BorderStroke(1.dp, Color(0xFFE8E8F0)), RoundedCornerShape(12.dp))
                            )
                            .clickable {
                                selectedFormats = if (selected) selectedFormats - index else selectedFormats + index
                            }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // selection indicator: circular now (was square)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (selected) PrimaryAccent else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                // white inner dot when selected (smaller)
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color.White))
                            } else {
                                // outlined circle when not selected
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(
                            imageVector = if (index == 0) Icons.Default.Mic else Icons.Default.Description,
                            contentDescription = null,
                            tint = iconFixedTint,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, style = titleTextStyle)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(subtitle, style = subtitleTextStyle)
                        }

                        Text(
                            text = count.toString(),
                            style = countTextStyle,
                            color = countFixedColor
                        )
                    }
                }

                // Thư mục title
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Thư mục", style = sectionHeadingStyle)
                        Text("Tất cả", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Thư mục rows
                itemsIndexed(folders) { idx, folder ->
                    FolderRow(
                        name = folder.name,
                        count = folder.count,
                        color = folder.color,
                        selected = selectedFolders.contains(idx),
                        onClick = {
                            selectedFolders = if (selectedFolders.contains(idx)) selectedFolders - idx else selectedFolders + idx
                        },
                        cardHeight = cardHeight,
                        titleTextStyle = titleTextStyle,
                        countTextStyle = countTextStyle
                    )
                }

                // Trạng thái title
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        // "Trạng thái" heading (bigger & bolder)
                        Text("Trạng thái", style = sectionHeadingStyle)
                        Text("${selectedStatuses.size} đã chọn", style = sectionCountStyle)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Trạng thái rows
                itemsIndexed(statuses) { index, status ->
                    val selected = selectedStatuses.contains(index)
                    StatusRow(
                        title = status.title,
                        subtitle = status.subtitle,
                        count = status.count,
                        type = status.type,
                        selected = selected,
                        onClick = {
                            selectedStatuses = if (selected) selectedStatuses - index else selectedStatuses + index
                        },
                        cardHeight = cardHeight,
                        titleTextStyle = titleTextStyle,
                        subtitleTextStyle = subtitleTextStyle,
                        countTextStyle = countTextStyle
                    )
                }

                item { Spacer(modifier = Modifier.height(86.dp)) }
            } // end LazyColumn

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(footerGradientBrush)
                    .padding(horizontal = horizontalPadding, vertical = 14.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    if (pressedButton == FilterFooterButton.CLEAR) {
                        Button(
                            onClick = {
                                selectedFormats = emptySet()
                                selectedFolders = emptySet()
                                selectedStatuses = emptySet()
                                pressedButton = FilterFooterButton.CLEAR
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = Color.White),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier
                                .height(44.dp)
                                .weight(1f)
                        ) {
                            Text("Xóa bộ lọc")
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                selectedFormats = emptySet()
                                selectedFolders = emptySet()
                                selectedStatuses = emptySet()
                                pressedButton = FilterFooterButton.CLEAR
                            },
                            border = BorderStroke(1.dp, footerOutlineColor),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = lightWhite22,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier
                                .height(44.dp)
                                .weight(1f)
                        ) {
                            Text("Xóa bộ lọc")
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            pressedButton = FilterFooterButton.APPLY
                            onClose()
                        },
                        colors = if (pressedButton == FilterFooterButton.APPLY) {
                            ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = Color.White)
                        } else {
                            ButtonDefaults.buttonColors(containerColor = lightWhite22, contentColor = Color.White)
                        },
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .weight(1.2f)
                    ) {
                        Text("Áp dụng")
                    }
                }
            }
        }
    }
}

// --- Helpers: FolderRow / StatusRow + models ---

private enum class FolderColor { PURPLE, ORANGE, BLUE, GREEN }
private data class FolderItem(val name: String, val count: Int, val color: FolderColor)

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
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) PrimaryAccent else Color(0xFFF0F0F4)),
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
                Icon(Icons.Default.Folder, contentDescription = null, tint = folderTint.copy(alpha = 0.9f), modifier = Modifier.size(20.dp))
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
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) PrimaryAccent else Color(0xFFF0F0F4)),
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
                    StatusType.PIN -> Icon(Icons.Default.PushPin, contentDescription = null, tint = iconTint.copy(alpha = 0.9f), modifier = Modifier.size(18.dp))
                    StatusType.HEART -> Icon(Icons.Default.Favorite, contentDescription = null, tint = iconTint.copy(alpha = 0.9f), modifier = Modifier.size(18.dp))
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FilterScreenPreview() {
    FilterScreen(onClose = {})
}