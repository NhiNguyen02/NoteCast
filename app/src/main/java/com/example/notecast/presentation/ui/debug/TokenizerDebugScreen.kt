package com.example.notecast.presentation.ui.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notecast.presentation.viewmodel.TokenizerDebugViewModel

/**
 * Màn hình đơn giản chỉ để debug Tokenizer.
 * Nhấn nút "Test Tokenizer" sẽ gọi TokenizerDebugViewModel.debugTokenizer()
 * và log kết quả ra Logcat (tag: "TokenizerDebug").
 */
@Composable
fun TokenizerDebugScreen(
    viewModel: TokenizerDebugViewModel = hiltViewModel(),
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Tokenizer Debug")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.debugTokenizer() }) {
                Text(text = "Test Tokenizer")
            }
        }
    }
}

