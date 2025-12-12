package com.example.notecast.presentation.ui.mindmap

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.example.notecast.domain.model.MindMapNode
import com.example.notecast.presentation.theme.Background
import com.example.notecast.presentation.theme.TitleBrush
import kotlin.math.max

@Composable
fun MindMapDialog(
    rootNode: MindMapNode,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().background(Background)
        ) {
            Column(Modifier.background(Background)) {
                Box(
                    modifier = Modifier
                        .zIndex(10f)
                        .background(Color.Transparent) // Nền đặc để che nội dung
                ) {
                    MindMapHeader(onDismiss)
                }
                Divider(color = Color(0xffE5E7EB), thickness = 1.dp, modifier = Modifier.fillMaxWidth().zIndex(10f))
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().background(Color.Transparent),
                ) {
                    ZoomablePannableContent {
                        Box(
                            modifier = Modifier
                                .wrapContentSize(unbounded = true) // Cho phép tràn màn hình
                                .padding(50.dp) // Padding đệm xung quanh
                        ) {
                            RecursiveTree(node = rootNode, isRoot = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecursiveTree(node: MindMapNode, isRoot: Boolean) {
    val nodeColor = try {
        Color(AndroidColor.parseColor(node.colorHex ?: "#6200EE"))
    } catch (e: Exception) { Color(0xFF6200EE) }
    TreeLayout(
        lineColor = nodeColor,
        nodeContent = {
            MindMapNodeCard(node.label, isRoot, nodeColor)
        },
        childrenContent = {
            node.children.forEach { child ->
                RecursiveTree(node = child, isRoot = false)
            }
        }
    )
}

/**
 * CUSTOM LAYOUT: FIX LỖI MẤT ROOT NODE & CHỒNG ĐÈ
 */
@Composable
fun TreeLayout(
    lineColor: Color,
    nodeContent: @Composable () -> Unit,
    childrenContent: @Composable () -> Unit
) {
    var lineCoordinates by remember { mutableStateOf<List<Pair<Offset, Offset>>>(emptyList()) }

    Layout(
        contents = listOf(nodeContent, childrenContent),
        modifier = Modifier.drawBehind {
            lineCoordinates.forEach { (start, end) ->
                val path = Path()
                path.moveTo(start.x, start.y)

                // CUBIC BEZIER (Cong chữ S mềm mại)
                val controlPoint1 = Offset(start.x + (end.x - start.x) * 0.6f, start.y)
                val controlPoint2 = Offset(start.x + (end.x - start.x) * 0.4f, end.y)

                path.cubicTo(
                    controlPoint1.x, controlPoint1.y,
                    controlPoint2.x, controlPoint2.y,
                    end.x, end.y
                )

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    ) { measurables, constraints ->

        // --- FIX QUAN TRỌNG: THÁO BỎ RÀNG BUỘC CHO CẢ CHA VÀ CON ---
        // Cho phép đo đạc tự do (Infinite), không bị giới hạn bởi kích thước màn hình
        val unconstrained = constraints.copy(
            minWidth = 0,
            maxWidth = Constraints.Infinity,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )

        // 1. Đo Node Cha với unconstrained (Sửa lỗi mất nội dung Root)
        val parentPlaceable = measurables[0].first().measure(unconstrained)

        // 2. Đo các Node Con với unconstrained (Sửa lỗi chồng đè)
        val childPlaceables = measurables[1].map { it.measure(unconstrained) }

        // --- CẤU HÌNH KHOẢNG CÁCH ---
        val horizontalGap = 100.dp.roundToPx() // Ngang 100dp
        val verticalGap = 30.dp.roundToPx()    // Dọc 30dp

        // 3. Tính chiều cao tổng khối con
        val childrenBlockHeight = if (childPlaceables.isEmpty()) 0 else {
            childPlaceables.sumOf { it.height } + (childPlaceables.size - 1) * verticalGap
        }

        // 4. Kích thước Layout
        val layoutHeight = max(parentPlaceable.height, childrenBlockHeight)

        val maxChildWidth = childPlaceables.maxOfOrNull { it.width } ?: 0
        val layoutWidth = parentPlaceable.width +
                if (childPlaceables.isEmpty()) 0 else (horizontalGap + maxChildWidth)

        layout(layoutWidth, layoutHeight) {

            // --- A. ĐẶT NODE CHA ---
            val parentY = (layoutHeight - parentPlaceable.height) / 2
            parentPlaceable.place(0, parentY)

            val startPoint = Offset(
                x = parentPlaceable.width.toFloat(),
                y = (parentY + parentPlaceable.height / 2).toFloat()
            )

            // --- B. ĐẶT CÁC NODE CON ---
            val childX = parentPlaceable.width + horizontalGap

            var currentChildY = (layoutHeight - childrenBlockHeight) / 2
            if (currentChildY < 0) currentChildY = 0

            val newLines = mutableListOf<Pair<Offset, Offset>>()

            childPlaceables.forEach { child ->
                child.place(childX, currentChildY)

                // Tính điểm kết thúc dây (Lấn vào 20px để không hở)
                val endPoint = Offset(
                    x = childX.toFloat() + 20f,
                    y = (currentChildY + child.height / 2).toFloat()
                )

                newLines.add(startPoint to endPoint)
                currentChildY += child.height + verticalGap
            }

            if (lineCoordinates != newLines) {
                lineCoordinates = newLines
            }
        }
    }
}

@Composable
fun MindMapNodeCard(text: String, isRoot: Boolean, color: Color) {
    val backgroundColor = if (isRoot) color else Color.White
    val contentColor = if (isRoot) Color.White else Color.Black
    val borderColor = if (isRoot) Color.Transparent else color

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .widthIn(min = 80.dp, max = 220.dp)
            .wrapContentHeight()
            .border(if (isRoot) 0.dp else 2.dp, borderColor, RoundedCornerShape(8.dp))
            .zIndex(2f) // Đè lên dây nối
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isRoot) FontWeight.Bold else FontWeight.Medium,
                    fontSize = if (isRoot) 16.sp else 14.sp
                ),
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MindMapHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Mind Map", fontWeight = FontWeight.Bold, fontSize = 20.sp, style = TextStyle(brush = TitleBrush))
            Text("Được tạo từ ghi chú", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
        }
    }
}

@Composable
fun ZoomablePannableContent(content: @Composable () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 4f)
                    offset += pan
                }
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        content()
    }
}