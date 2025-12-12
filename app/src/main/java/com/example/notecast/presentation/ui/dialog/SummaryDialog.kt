package com.example.notecast.presentation.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.notecast.utils.extractSummaryFromContent
import com.example.notecast.utils.previewText

// Summary dialog composable (in-file). Displays initial preview, runs summarization via onStart,
// shows loading state, and shows extracted summary from contentAfter when available.
@Composable
fun SummaryDialog(
    noteContent: String,
    isProcessing: Boolean,
    error: String?,
    onStart: () -> Unit,
    onDismiss: () -> Unit,
    contentAfter: String // observe ViewModel content to detect appended summary
) {
    val clipboard = LocalClipboardManager.current
    var showResult by remember { mutableStateOf(false) }
    // Explicit type so compiler can infer generics
//    val extracted: String? by remember(contentAfter) {
//        mutableStateOf(extractSummaryFromContent(contentAfter))
//    }
    val extracted by remember(contentAfter) {
        mutableStateOf(extractSummaryFromContent(contentAfter))
    }

    LaunchedEffect(isProcessing, extracted) {
        // when processing finished and extracted text is present, show result
        if (!isProcessing && !extracted.isNullOrBlank()) {
            showResult = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (showResult) {
                TextButton(onClick = {
                    // copy and close
                    extracted?.let { clipboard.setText(AnnotatedString(it)) }
                    onDismiss()
                }) {
                    Text("Copy & Close")
                }
            } else {
                TextButton(onClick = onStart) {
                    Text("Tạo tóm tắt")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        },
        title = {
            Text("Tóm tắt ghi chú")
        },
        text = {
            Column {
                if (isProcessing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Đang tạo tóm tắt...")
                    }
                } else if (!showResult) {
                    if (!error.isNullOrBlank()) {
                        Text("Lỗi: $error", color = Color.Red)
                        Spacer(Modifier.height(8.dp))
                    }
                    Text("Nội dung (xem trước):", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(previewText(noteContent), style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("Kết quả tóm tắt:", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(extracted ?: "Không tìm thấy tóm tắt", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    )
}