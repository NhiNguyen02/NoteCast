package com.example.notecast.presentation.screen.sort

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.notecast.presentation.theme.*
import androidx.compose.foundation.interaction.MutableInteractionSource as InteractionSourceAlias

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

private enum class FooterButton { NONE, CANCEL, APPLY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortScreen(onClose: () -> Unit) {
    data class SortOption(val title: String, val subtitle: String, val icon: ImageVector)

    val timeOptions = listOf(
        SortOption("Mới tạo", "Ghi chú được tạo gần đây nhất hiển thị trước", Icons.Default.Event),
        SortOption("Lâu nhất", "Ghi chú được tạo lâu nhất hiển thị trước", Icons.Default.CalendarToday),
        SortOption("Mới cập nhật", "Ghi chú được chỉnh sửa gần đây nhất", Icons.Default.AccessTime)
    )

    val titleOptions = listOf(
        SortOption("Tiêu đề A → Z", "Sắp xếp theo thứ tự bảng chữ cái từ A đến Z", Icons.Default.SortByAlpha),
        SortOption("Tiêu đề Z → A", "Sắp xếp theo thứ tự bảng chữ cái từ Z về A", Icons.Default.SortByAlpha)
    )

    var selectedTimeIndex by remember { mutableStateOf(0) }
    var selectedTitleIndex by remember { mutableStateOf(-1) }

    val horizontalPadding = 16.dp
    val headerDividerColor = PrimaryAccent.copy(alpha = 0.22f)
    val footerOutline = Color.White.copy(alpha = 0.35f)

    // footer gradient
    val gradientTop = Color(0xFFB96CFF)
    val gradientMiddle = Color(0xFF8A4BFF)
    val gradientBottom = Color(0xFF6A2CFF)
    val footerGradientBrush = Brush.verticalGradient(listOf(gradientTop, gradientMiddle, gradientBottom))

    var pressedButton by remember { mutableStateOf(FooterButton.NONE) }
    val lightWhite22 = Color.White.copy(alpha = 0.22f)

    // typography & sizes (aligned with FilterScreen)
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

    val sectionHeadingStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryAccentDark
    )
    val sectionCountStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundPrimary)
            .clickable(indication = null, interactionSource = remember { InteractionSourceAlias() }) { /* consume clicks */ }
    ) {

        Column(modifier = Modifier.fillMaxSize()) {
            // HEADER — kept identical structure to FilterScreen header
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
                            Icons.Default.Sort,
                            contentDescription = null,
                            tint = Color(0xFF6200AE),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "Sắp xếp ghi chú",
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = PrimaryAccentDark)
                    )
                }

                Text(
                    text = "Hủy",
                    color = Color(0xFF8D8DA3),
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onClose() }
                )
            }

            Divider(color = headerDividerColor, thickness = 1.dp, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Theo thời gian", style = sectionHeadingStyle)
                        Text("${if (selectedTimeIndex >= 0) 1 else 0} đã chọn", style = sectionCountStyle)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                itemsIndexed(timeOptions) { index, option ->
                    val selected = selectedTimeIndex == index
                    SortRow(
                        title = option.title,
                        subtitle = option.subtitle,
                        icon = option.icon,
                        selected = selected,
                        onClick = { selectedTimeIndex = index },
                        showArrow = true,
                        cardHeight = cardHeight,
                        titleTextStyle = titleTextStyle,
                        subtitleTextStyle = subtitleTextStyle
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Theo tiêu đề", style = sectionHeadingStyle)
                        Text(if (selectedTitleIndex >= 0) "1 đã chọn" else "0 đã chọn", style = sectionCountStyle)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                itemsIndexed(titleOptions) { index, option ->
                    val selected = selectedTitleIndex == index
                    SortRow(
                        title = option.title,
                        subtitle = option.subtitle,
                        icon = option.icon,
                        selected = selected,
                        onClick = {
                            selectedTitleIndex = if (selected) -1 else index
                        },
                        showArrow = false,
                        cardHeight = cardHeight,
                        titleTextStyle = titleTextStyle,
                        subtitleTextStyle = subtitleTextStyle
                    )
                }

                item { Spacer(modifier = Modifier.height(96.dp)) }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = footerGradientBrush)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pressedButton == FooterButton.CANCEL) {
                        Button(
                            onClick = {
                                selectedTimeIndex = 0
                                selectedTitleIndex = -1
                                pressedButton = FooterButton.CANCEL
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = Color.White),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier
                                .height(44.dp)
                                .weight(1f)
                        ) {
                            Text("Hủy")
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                selectedTimeIndex = 0
                                selectedTitleIndex = -1
                                pressedButton = FooterButton.CANCEL
                            },
                            border = BorderStroke(1.dp, footerOutline),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = lightWhite22, contentColor = Color.White),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier
                                .height(44.dp)
                                .weight(1f)
                        ) {
                            Text("Hủy")
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            pressedButton = FooterButton.APPLY
                            onClose()
                        },
                        colors = if (pressedButton == FooterButton.APPLY) {
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

@Composable
private fun SortRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    showArrow: Boolean = true,
    cardHeight: Dp = 68.dp,
    titleTextStyle: TextStyle = TextStyle(fontSize = 16.sp, color = Color(0xFF222222), fontWeight = FontWeight.SemiBold),
    subtitleTextStyle: TextStyle = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
) {
    val iconTileRadius = 10.dp
    val arrowSelectedTint = PrimaryAccent.copy(alpha = 0.85f)
    val iconUnselectedTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val iconSelectedTint = PrimaryAccent.copy(alpha = 0.95f)

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
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(14.dp)).background(if (selected) PrimaryAccent else Color.Transparent), contentAlignment = Alignment.Center) {
            if (selected) Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(Color.White)) else Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(6.dp)))
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(iconTileRadius)), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = if (selected) iconSelectedTint else iconUnselectedTint, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = titleTextStyle)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = subtitleTextStyle)
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (showArrow) {
            Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(imageVector = if (selected) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward, contentDescription = null, tint = if (selected) arrowSelectedTint else iconUnselectedTint, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SortScreenPreview() {
    SortScreen(onClose = {})
}