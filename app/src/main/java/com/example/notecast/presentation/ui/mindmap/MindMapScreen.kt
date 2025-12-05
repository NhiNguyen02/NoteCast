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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.notecast.domain.model.MindMapNode
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.max

@Composable
fun MindMapDialog(
    rootNode: MindMapNode,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Full screen
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8F9FA)
        ) {
            Column {
                MindMapHeader(onDismiss)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                        .background(Color(0xFFF8F9FA))
                ) {
                    ZoomablePannableContent {
                        // Padding lớn để cây nằm giữa
                        Box(modifier = Modifier.padding(100.dp)) {
                            RecursiveTree(node = rootNode, isRoot = true)
                        }
                    }
                }
            }
        }
    }
}

// --- ĐỆ QUY VẼ CÂY ---
@Composable
fun RecursiveTree(node: MindMapNode, isRoot: Boolean) {
    // Parse màu
    val nodeColor = try {
        Color(AndroidColor.parseColor(node.colorHex ?: "#6200EE"))
    } catch (e: Exception) { Color(0xFF6200EE) }
    println("NODE = ${node.label}, children = ${node.children.size}")
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
 * CUSTOM LAYOUT: Sắp xếp Cha bên trái, Con bên phải và VẼ ĐƯỜNG NỐI
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
                path.cubicTo(
                    start.x + (end.x - start.x) / 2, start.y,
                    start.x + (end.x - start.x) / 2, end.y,
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

        val parentPlaceable = measurables[0].first().measure(constraints)

        val childMeasurables = measurables[1]
        val childPlaceables = childMeasurables.map { it.measure(constraints) }

        val horizontalGap = 80.dp.roundToPx()
        val verticalGap = 24.dp.roundToPx()

        // TÍNH CHIỀU CAO SUBTREE (giãn theo chiều dọc)
        val totalChildrenHeight =
            childPlaceables.sumOf { it.height } +
                    max(0, childPlaceables.size - 1) * verticalGap

        val layoutHeight = max(parentPlaceable.height, totalChildrenHeight)

        // TÍNH CHIỀU RỘNG SUBTREE (giãn theo chiều ngang)
        val maxChildWidth = childPlaceables.maxOfOrNull { it.width } ?: 0
        val layoutWidth = parentPlaceable.width +
                (if (childPlaceables.isEmpty()) 0 else horizontalGap + maxChildWidth)

        layout(layoutWidth, layoutHeight) {

            // Đặt Parent
            val parentY = (layoutHeight - parentPlaceable.height) / 2
            parentPlaceable.place(0, parentY)

            val startPoint = Offset(
                x = parentPlaceable.width.toFloat(),
                y = parentY + parentPlaceable.height / 2f
            )

            var childY = (layoutHeight - totalChildrenHeight) / 2
            val childX = parentPlaceable.width + horizontalGap

            val newLines = mutableListOf<Pair<Offset, Offset>>()

            childPlaceables.forEach { child ->
                child.place(childX, childY)

                val endPoint = Offset(
                    x = childX.toFloat(),
                    y = childY + child.height / 2f
                )
                newLines.add(startPoint to endPoint)

                childY += child.height + verticalGap
            }

            if (newLines != lineCoordinates)
                lineCoordinates = newLines
        }
    }
}


// --- COMPONENT: THẺ NODE ---
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
            .width(IntrinsicSize.Max)
            .widthIn(min = 80.dp, max = 220.dp)
            .border(if (isRoot) 0.dp else 2.dp, borderColor, RoundedCornerShape(8.dp))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
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

// --- HEADER ---
@Composable
fun MindMapHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Sơ đồ tư duy", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Được tạo từ ghi chú", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
        }
    }
}

// --- ZOOM & PAN ---
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
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}