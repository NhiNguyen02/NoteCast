package com.example.notecast.presentation.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* ---------- Host demo (optional) ---------- */
@Composable
fun SummaryProcessingHost(onDone: (resultText: String) -> Unit = {}) {
    var showProcessing by remember { mutableStateOf(false) }
    var percent by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            showProcessing = true
            percent = 0
            showResult = false
            scope.launch {
                while (percent < 100 && showProcessing) {
                    delay(220L)
                    val step = if (percent < 30) 6 else if (percent < 80) 8 else 4
                    percent = (percent + step).coerceAtMost(100)
                }
                if (percent >= 100) {
                    delay(220)
                    showProcessing = false
                    showResult = true
                    onDone("Tóm tắt hoàn tất")
                }
            }
        }) {
            Text("Bắt đầu tóm tắt")
        }
    }

    if (showProcessing) {
        SummaryProcessingDialog(
            percent = percent,
            onCancel = {
                showProcessing = false
                percent = 0
            }
        )
    }

    if (showResult) {
        SummaryCompleteDialog(
            onOk = { showResult = false }
        )
    }
}