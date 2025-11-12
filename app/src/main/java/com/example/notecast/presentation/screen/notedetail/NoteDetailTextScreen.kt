package com.example.notecast.presentation.screen.notedetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.presentation.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailTextScreen(
    title: String,
    date: String,
    content: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onNormalize: () -> Unit,
    onSummarize: () -> Unit,
    onMindMap: () -> Unit,
    modifier: Modifier = Modifier,
    initialTab: Int = 0, // 0 = Văn bản, 1 = Âm thanh (use for Preview)

    // Preview helpers: allow preview to show result card without user interaction
    showResultCardPreview: Boolean = false,
    previewResultTitle: String = "",
    previewResultContent: String = ""
) {
    val horizontalPadding = 16.dp

    // colors/gradients
    val gradientTop = Color(0xFFB96CFF)
    val gradientMiddle = Color(0xFF8A4BFF)
    val gradientBottom = Color(0xFF6A2CFF)
    val blueStart = Color(0xFF2EC7FF)
    val blueEnd = Color(0xFF3AA8FF)

    val headerDividerColor = PrimaryAccent.copy(alpha = 0.22f)
    val tagBg = Color(0xFFFFF5DF)
    val tagBorder = Color(0xFFFFC84D)
    val tagText = Color(0xFF7A3E00)

    val isInPreview = LocalInspectionMode.current

    var selectedTab by remember { mutableStateOf(initialTab.coerceIn(0, 1)) }

    // AUDIO demo state (for preview/demo; replace with real player state)
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0.42f) } // 0..1
    val totalSeconds = 347

    // demo progress increment while playing; safe because it only runs if isPlaying true
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                delay(250L)
                progress = (progress + 0.004f).coerceAtMost(1f)
                if (progress >= 1f) {
                    isPlaying = false
                    break
                }
            }
        }
    }

    // heights (kept as you requested)
    val contentCardHeight = 520.dp
    val transcriptCardHeight = 460.dp // changed to 460 as requested

    // result card state (shown after pressing Tóm tắt or Mind map)
    var showResultCard by remember { mutableStateOf(showResultCardPreview) }
    var resultTitle by remember { mutableStateOf(previewResultTitle) }
    var resultContent by remember { mutableStateOf(previewResultContent) }

    // keep preview-provided values in sync
    LaunchedEffect(showResultCardPreview, previewResultTitle, previewResultContent) {
        if (showResultCardPreview) {
            showResultCard = true
            resultTitle = previewResultTitle
            resultContent = previewResultContent
        }
    }

    // LazyColumn state + coroutine scope for auto-scroll to result
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Bottom buttons as a composable so we can reuse in Scaffold.bottomBar
    @Composable
    fun BottomActionButtons() {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(Color.Transparent)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // restore gradient styles for buttons
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(gradientTop, gradientMiddle)))
                        .clickable {
                            onNormalize()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chuẩn hóa", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFF2F7FF), Color(0xFFE8F3FF))))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)), RoundedCornerShape(12.dp))
                        .clickable {
                            onEdit()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = gradientMiddle)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sửa văn bản", color = gradientMiddle, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(blueStart, blueEnd)))
                        .clickable {
                            // set result content for summary and show it
                            onSummarize()
                            resultTitle = "Bản tóm tắt"
                            resultContent = "Đây là nội dung tóm tắt tự động (ví dụ)."
                            showResultCard = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tóm tắt", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFF2F7FF), Color(0xFFE8F3FF))))
                        .border(BorderStroke(1.dp, Color(0xFFB8CFE6)), RoundedCornerShape(12.dp))
                        .clickable {
                            // set result content for mindmap and show it
                            onMindMap()
                            resultTitle = "Mind map"
                            resultContent = "Mind map (preview): Node A → Node B → Node C"
                            showResultCard = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeviceHub, contentDescription = null, tint = gradientMiddle)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mind map", color = Color(0xFF6EA8D9), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // auto-scroll to the result card when it appears
    LaunchedEffect(showResultCard) {
        if (showResultCard) {
            // small delay to allow LazyColumn to compose the new item
            delay(120)
            coroutineScope.launch {
                val lastIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
                listState.animateScrollToItem(lastIndex)
            }
        }
    }

    // Scaffold keeps bottomBar fixed; show it only on Văn bản tab (selectedTab == 0)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (selectedTab == 0) {
                Surface(color = Color.Transparent, tonalElevation = 0.dp) {
                    BottomActionButtons()
                }
            } else {
                Spacer(modifier = Modifier.height(0.dp))
            }
        }
    ) { paddingValues ->
        // Main layout: header + tabs fixed outside the scrollable LazyColumn.
        // LazyColumn below is the only scrolling area and uses scaffold paddingValues
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundPrimary)
        ) {
            // HEADER (fixed)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF4B2D80),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.height(IntrinsicSize.Min), verticalArrangement = Arrangement.Center) {
                            Text(text = title, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, brush = textGradient))
                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .height(22.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(tagBg)
                                    .border(BorderStroke(1.dp, tagBorder), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp)
                                    .widthIn(max = 120.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Label, contentDescription = "Tag", tint = gradientMiddle, modifier = Modifier.size(12.dp))
                                    Text("Ý tưởng", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = tagText))
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { /* pin */ }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.PushPin, contentDescription = "Pin", tint = gradientMiddle)
                        }
                        IconButton(onClick = { /* delete */ }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F))
                        }
                    }
                }
            }

            // Divider + Tabs (fixed)
            HorizontalDivider(color = headerDividerColor, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                Column(modifier = Modifier.padding(end = 24.dp).clickable { selectedTab = 0 }) {
                    Text("Văn bản", style = TextStyle(fontSize = 14.sp, fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Medium, color = if (selectedTab == 0) gradientBottom else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)))
                    Spacer(modifier = Modifier.height(6.dp))
                    if (selectedTab == 0) Box(modifier = Modifier.height(3.dp).width(56.dp).clip(RoundedCornerShape(4.dp)).background(gradientBottom))
                }
                Column(modifier = Modifier.padding(end = 24.dp).clickable { selectedTab = 1 }) {
                    Text("Âm thanh", style = TextStyle(fontSize = 14.sp, fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Medium, color = if (selectedTab == 1) gradientBottom else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)))
                    Spacer(modifier = Modifier.height(6.dp))
                    if (selectedTab == 1) Box(modifier = Modifier.height(3.dp).width(56.dp).clip(RoundedCornerShape(4.dp)).background(gradientBottom))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CONTENT (scrollable area only). Pass scaffold paddingValues as contentPadding so LazyColumn
            // respects bottomBar height (prevents content from extending under bottom bar).
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = paddingValues
            ) {
                item {
                    if (selectedTab == 0) {
                        // Văn bản card — fixed height, internal scroll so text stays bounded
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(contentCardHeight)
                                .padding(horizontal = horizontalPadding)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFF3EEFF))
                                .border(BorderStroke(1.dp, Color(0x33B96CFF)), RoundedCornerShape(18.dp))
                                .padding(16.dp)
                        ) {
                            val innerScroll = rememberScrollState()
                            Column(modifier = Modifier.fillMaxSize().verticalScroll(innerScroll)) {
                                Text("Nội dung", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5D1AAE)))
                                Spacer(modifier = Modifier.height(8.dp))

                                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                    Text(text = content, style = TextStyle(fontSize = 14.sp, color = Color(0xFF222222), lineHeight = 20.sp))
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }
                        }
                    } else {
                        // Audio UI
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding)) {
                            // Player card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF3EEFF))
                                    .border(BorderStroke(1.dp, Color(0xFFEEE9FB)), RoundedCornerShape(14.dp))
                                    .padding(16.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("File âm thanh", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5D1AAE)))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Controls row (prev / big play / next)
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFF1E7FF)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = gradientMiddle, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(18.dp))
                                        Box(modifier = Modifier.size(64.dp).shadow(elevation = 8.dp, shape = CircleShape).clip(CircleShape).background(Brush.radialGradient(listOf(gradientTop, gradientMiddle))), contentAlignment = Alignment.Center) {
                                            IconButton(onClick = { isPlaying = !isPlaying }) {
                                                if (isPlaying) Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White, modifier = Modifier.size(28.dp))
                                                else Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(28.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(18.dp))
                                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFF1E7FF)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = gradientMiddle, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Progress track (Canvas runtime)
                                    val trackHeight = 8.dp
                                    Canvas(modifier = Modifier.fillMaxWidth().height(trackHeight)) {
                                        val trackH = trackHeight.toPx()
                                        val corner = CornerRadius(trackH / 2f, trackH / 2f)
                                        drawRoundRect(color = Color(0xFFDDE1E6), topLeft = Offset(0f, 0f), size = Size(size.width, trackH), cornerRadius = corner)
                                        val filledW = size.width * progress.coerceIn(0f, 1f)
                                        if (filledW > 0f) {
                                            drawRoundRect(brush = Brush.horizontalGradient(listOf(Color(0xFFB96CFF), gradientMiddle)), topLeft = Offset(0f, 0f), size = Size(filledW, trackH), cornerRadius = corner)
                                        }
                                        val thumbR = 7.dp.toPx()
                                        val cx = filledW.coerceIn(thumbR, size.width - thumbR)
                                        val cy = trackH / 2f
                                        drawCircle(color = Color.White, radius = thumbR, center = Offset(cx, cy))
                                        drawCircle(color = Color(0x66B96CFF), radius = thumbR + 1.2f, center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        val currentSec = (progress * totalSeconds).toInt()
                                        Text(formatTime(currentSec), style = TextStyle(fontSize = 12.sp, color = Color(0xFF6B6B6B)))
                                        Text(formatTime(totalSeconds), style = TextStyle(fontSize = 12.sp, color = Color(0xFF6B6B6B)))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Transcript card — FIXED HEIGHT; internal scroll allowed (safe)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(transcriptCardHeight)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF3EEFF))
                                    .border(BorderStroke(1.dp, Color(0x33B96CFF)), RoundedCornerShape(14.dp))
                                    .padding(16.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                                    Text("Bản chép lời", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5D1AAE)))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    TranscriptRow(time = "00:00", text = "Chào mọi người, hôm nay chúng ta sẽ thảo luận về kế hoạch dự án quý 4. Đầu tiên, tôi muốn chia sẻ về tiến độ hiện tại của các milestone chính.")
                                    Spacer(modifier = Modifier.height(12.dp))
                                    TranscriptRow(time = "01:38", text = "Team marketing đã hoàn thành 85% công việc cho chiến dịch Q4, còn team development đang trong giai đoạn testing phase cho version 2.1.")
                                    Spacer(modifier = Modifier.height(12.dp))
                                    TranscriptRow(time = "03:58", text = "Chúng ta cần tập trung vào việc optimize performance và fix các critical bugs trước khi release. Deadline chính thức là ngày 30 tháng 11.")
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                        }
                    }
                }

                // Result card (shown after pressing Tóm tắt or Mind map)
                item {
                    if (showResultCard) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFFFFFF))
                                .border(BorderStroke(1.dp, Color(0x33B96CFF)), RoundedCornerShape(14.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(resultTitle, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = gradientBottom))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(resultContent, style = TextStyle(fontSize = 14.sp, color = Color(0xFF222222), lineHeight = 20.sp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { showResultCard = false }) {
                                        Text("Đóng")
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun TranscriptRow(time: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFFB96CFF), Color(0xFF8A4BFF))))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(time, style = TextStyle(color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(text = text, style = TextStyle(color = Color(0xFF222222), fontSize = 14.sp, lineHeight = 20.sp), modifier = Modifier.weight(1f))
    }
}

private fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}

/* ---------- Previews ---------- */

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun NoteDetailTextScreenPreview_Text() {
    NoteDetailTextScreen(
        title = "Ghi chú cuộc họp",
        date = "15 Nov 2024",
        content = ("Chào mọi người, hôm nay chúng ta sẽ thảo luận về kế hoạch dự án quý 4. ").repeat(6),
        onBack = {},
        onEdit = {},
        onNormalize = {},
        onSummarize = {},
        onMindMap = {},
        initialTab = 0
    )
}
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun NoteDetailTextScreenPreview_Audio() {
    NoteDetailTextScreen(
        title = "Ghi chú cuộc họp",
        date = "15 Nov 2024",
        content = ("Chào mọi người, hôm nay chúng ta sẽ thảo luận về kế hoạch dự án quý 4. ").repeat(1),
        onBack = {},
        onEdit = {},
        onNormalize = {},
        onSummarize = {},
        onMindMap = {},
        initialTab = 1
    )
}
@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun NoteDetail_Preview_Summary() {
    NoteDetailTextScreen(
        title = "Ghi chú cuộc họp",
        date = "15 Nov 2024",
        content = ("Nội dung dài... ").repeat(6),
        onBack = {},
        onEdit = {},
        onNormalize = {},
        onSummarize = {},
        onMindMap = {},
        initialTab = 0,
        showResultCardPreview = true,
        previewResultTitle = "Bản tóm tắt",
        previewResultContent = "Cuộc họp bàn về kế hoạch Q4: tiến độ, marketing 85%, dev đang testing, tập trung fix bug, tối ưu performance."
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun NoteDetail_Preview_Mindmap() {
    NoteDetailTextScreen(
        title = "Ghi chú cuộc họp",
        date = "15 Nov 2024",
        content = ("Nội dung ngắn... ").repeat(2),
        onBack = {},
        onEdit = {},
        onNormalize = {},
        onSummarize = {},
        onMindMap = {},
        initialTab = 0,
        showResultCardPreview = true,
        previewResultTitle = "Mind map",
        previewResultContent = "Mind map preview:\n• Mục tiêu\n  – Marketing\n  – Development\n• Deadline\n• Các bước hành động"
    )
}