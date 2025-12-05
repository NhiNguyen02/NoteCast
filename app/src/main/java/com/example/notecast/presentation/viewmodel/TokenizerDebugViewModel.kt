package com.example.notecast.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.notecast.core.asr.Tokenizer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel dùng riêng cho mục đích debug Tokenizer/BPE.
 * Có thể gắn tạm vào một màn debug hoặc gọi từ Activity để kiểm tra encode/decode.
 */
@HiltViewModel
class TokenizerDebugViewModel @Inject constructor(
    private val tokenizer: Tokenizer,
) : ViewModel() {

    fun debugTokenizer() {
        val samples = listOf(
            "xin chào",
            "hôm nay trời đẹp không?",
            "đây là bài test tokenizer PhoWhisper",
        )

        samples.forEach { text ->
            val encoded = tokenizer.encode(text)
            val decoded = tokenizer.decode(encoded)
            Log.d(
                "TokenizerDebug",
                "text='$text' -> ids=${encoded.joinToString()} -> decoded='$decoded'",
            )
        }
    }
}

