package com.example.notecast.presentation.ui.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.notecast.domain.usecase.SummarizeNoteUseCase
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@Composable
fun SummarizeDialog(
    noteContent: String,
    onDismiss: () -> Unit,
    summarizeUseCase: SummarizeNoteUseCase
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8F9FA)
        ) {
            SummarizeScreenUI(
                noteContent = noteContent,
                onDismiss = onDismiss,
                summarizeUseCase = summarizeUseCase
            )
        }
    }
}

@Composable
private fun SummarizeScreenUI(
    noteContent: String,
    onDismiss: () -> Unit,
    summarizeUseCase: SummarizeNoteUseCase
) {
    var summary by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {

        SummarizeHeader(onDismiss)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            when {
                loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Đang tạo tóm tắt...")
                    }
                }

                summary != null -> {
                    SummaryResultView(summary!!)
                }

                else -> {
                    EmptySummaryView(
                        onGenerate = {
                            scope.launch {
                                loading = true
                                summary = summarizeUseCase(noteContent)
                                loading = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SummarizeHeader(onDismiss: () -> Unit) {
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
            Text("Tóm tắt ghi chú", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("AI Summary • Notecast", color = Color.Gray, fontSize = 12.sp)
        }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
        }
    }
}

@Composable
fun EmptySummaryView(onGenerate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Nhấn nút để tạo tóm tắt tự động", fontSize = 15.sp, color = Color.DarkGray)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onGenerate) {
            Text("Tạo tóm tắt")
        }
    }
}

@Composable
fun SummaryResultView(summary: String) {
    val scroll = rememberScrollState()
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = {
                clipboard.setText(AnnotatedString(summary))
            }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
            }
        }

        Text(
            text = summary,
            fontSize = 15.sp,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scroll)
                .padding(top = 10.dp)
        )
    }
}
