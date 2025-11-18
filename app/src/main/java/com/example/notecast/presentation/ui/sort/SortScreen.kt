package com.example.notecast.presentation.ui.sort

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.presentation.theme.*
import com.example.notecast.presentation.ui.homescreen.SortBy
import com.example.notecast.presentation.ui.homescreen.SortDirection
import com.example.notecast.presentation.ui.homescreen.SortOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class FooterButton { NONE, CANCEL, APPLY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortScreen(
    currentOptions: SortOptions,
    onApply: (SortOptions) -> Unit,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()

    data class SortOptionItem(val title: String, val subtitle: String, val icon: ImageVector)

    // UI Options
    val timeOptions = listOf(
        SortOptionItem("Mới tạo", "Ghi chú được tạo gần đây nhất hiển thị trước", Icons.Default.Event),// Index 0
        SortOptionItem("Lâu nhất", "Ghi chú được tạo lâu nhất hiển thị trước", Icons.Default.CalendarToday), //Index 1
        SortOptionItem("Mới cập nhật", "Ghi chú được chỉnh sửa gần đây nhất", Icons.Default.AccessTime), //Index 2
    )

    val titleOptions = listOf(
        SortOptionItem("Tiêu đề A → Z", "Sắp xếp theo thứ tự bảng chữ cái từ A đến Z", Icons.Default.SortByAlpha), // Index 0
        SortOptionItem("Tiêu đề Z → A", "Sắp xếp theo thứ tự bảng chữ cái từ Z về A", Icons.Default.SortByAlpha)  // Index 1
    )

    // 4. KHỞI TẠO STATE UI TỪ DOMAIN OPTIONS
    var selectedTimeIndex by remember {
        mutableIntStateOf(
            if (currentOptions.sortBy == SortBy.TITLE) -1
            else when (currentOptions.sortBy) {
                SortBy.DATE_UPDATED -> 0
                SortBy.DATE_CREATED -> if (currentOptions.direction == SortDirection.DESCENDING) 1 else 2
                else -> 0
            }
        )
    }

    var selectedTitleIndex by remember {
        mutableIntStateOf(
            if (currentOptions.sortBy != SortBy.TITLE) -1
            else if (currentOptions.direction == SortDirection.ASCENDING) 0 else 1
        )
    }

    // UI Constants
    val horizontalPadding = 16.dp
    val headerDividerColor = PrimaryAccent.copy(alpha = 0.22f)
    val footerOutline = Color.White.copy(alpha = 0.35f)
    var pressedButton by remember { mutableStateOf(FooterButton.NONE) }
    val lightWhite22 = Color.White.copy(alpha = 0.22f)
    val cardHeight = 72.dp
    val titleTextStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF222222))
    val subtitleTextStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
    val sectionHeadingStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryAccentDark)
    val sectionCountStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Background)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // HEADER
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(PrimaryAccent.copy(alpha = 0.06f)).border(BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.22f)), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Sort, contentDescription = null, tint = Color(0xFF6200AE), modifier = Modifier.size(18.dp))
                    }
                    Text("Sắp xếp ghi chú", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, brush = TitleBrush))
                }
                Text("Hủy", color = Color(0xFF8D8DA3), fontSize = 14.sp, modifier = Modifier.clickable { onClose() })
            }
            Divider(color = headerDividerColor, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SECTION: TIME
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Theo thời gian", style = sectionHeadingStyle)
                        Text("${if (selectedTimeIndex >= 0) 1 else 0} đã chọn", style = sectionCountStyle)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                itemsIndexed(timeOptions) { index, option ->
                    SortRow(
                        title = option.title,
                        subtitle = option.subtitle,
                        icon = option.icon,
                        selected = selectedTimeIndex == index,
                        onClick = {
                            selectedTimeIndex = index
                            selectedTitleIndex = -1 // Reset title selection
                        },
                        cardHeight = cardHeight, titleTextStyle = titleTextStyle, subtitleTextStyle = subtitleTextStyle
                    )
                }

                // SECTION: TITLE
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Theo tiêu đề", style = sectionHeadingStyle)
                        Text("${if (selectedTitleIndex >= 0) 1 else 0} đã chọn", style = sectionCountStyle)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                itemsIndexed(titleOptions) { index, option ->
                    SortRow(
                        title = option.title,
                        subtitle = option.subtitle,
                        icon = option.icon,
                        selected = selectedTitleIndex == index,
                        onClick = {
                            selectedTitleIndex = index
                            selectedTimeIndex = -1 // Reset time selection
                        },
                        showArrow = false,
                        cardHeight = cardHeight, titleTextStyle = titleTextStyle, subtitleTextStyle = subtitleTextStyle
                    )
                }
                item { Spacer(modifier = Modifier.height(96.dp)) }
            }

            // FOOTER
            Box(modifier = Modifier.fillMaxWidth().background(brush = FooterGradientBrush)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Nút Hủy
                    if (pressedButton == FooterButton.CANCEL) {
                        Button(onClick = { onClose() }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent), shape = RoundedCornerShape(28.dp), modifier = Modifier.height(44.dp).weight(1f)) { Text("Hủy") }
                    } else {
                        OutlinedButton(onClick = { onClose() }, border = BorderStroke(1.dp, footerOutline), colors = ButtonDefaults.outlinedButtonColors(containerColor = lightWhite22, contentColor = Color.White), shape = RoundedCornerShape(28.dp), modifier = Modifier.height(44.dp).weight(1f)) { Text("Hủy") }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                pressedButton = FooterButton.APPLY

                                // === LOGIC TẠO SortOptions MỚI ===
                                val newOptions = if (selectedTimeIndex != -1) {
                                    when (selectedTimeIndex) {
                                        0 -> SortOptions(SortBy.DATE_UPDATED, SortDirection.DESCENDING)
                                        1 -> SortOptions(SortBy.DATE_CREATED, SortDirection.DESCENDING)
                                        else -> SortOptions(SortBy.DATE_CREATED, SortDirection.ASCENDING)
                                    }
                                } else {
                                    when (selectedTitleIndex) {
                                        0 -> SortOptions(SortBy.TITLE, SortDirection.ASCENDING) // A-Z
                                        else -> SortOptions(SortBy.TITLE, SortDirection.DESCENDING) // Z-A
                                    }
                                }
                                onApply(newOptions) // <-- Gửi ra ngoài
                                // ================================

                                delay(140)
                                onClose()
                            }
                        },
                        colors = if (pressedButton == FooterButton.APPLY) ButtonDefaults.buttonColors(containerColor = PrimaryAccent) else ButtonDefaults.buttonColors(containerColor = lightWhite22),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.height(44.dp).weight(1.2f)
                    ) {
                        Text("Áp dụng")
                    }
                }
            }
        }
    }
}

@Composable
private fun SortRow(title: String, subtitle: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit, showArrow: Boolean = true, cardHeight: Dp, titleTextStyle: TextStyle, subtitleTextStyle: TextStyle) {
    val iconSelectedTint = PrimaryAccent.copy(alpha = 0.95f)
    val iconUnselectedTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    Row(
        modifier = Modifier.fillMaxWidth().height(cardHeight).clip(RoundedCornerShape(12.dp)).background(Color.White)
            .then(if (selected) Modifier.border(BorderStroke(2.dp, PrimaryAccent), RoundedCornerShape(12.dp)) else Modifier.border(BorderStroke(1.dp, Color(0xFFE8E8F0)), RoundedCornerShape(12.dp)))
            .clickable { onClick() }.padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(18.dp)).background(if (selected) PrimaryAccent else Color.Transparent).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
            if (selected) Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(Color.White)) else Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = if (selected) iconSelectedTint else iconUnselectedTint, modifier = Modifier.size(25.dp)) }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) { Text(title, style = titleTextStyle); Spacer(Modifier.height(4.dp)); Text(subtitle, style = subtitleTextStyle) }
        if (showArrow) { Spacer(Modifier.width(8.dp)); Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) { Icon(if (selected) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward, null, tint = if (selected) PrimaryAccent.copy(alpha = 0.85f) else iconUnselectedTint, modifier = Modifier.size(18.dp)) } }
    }
}