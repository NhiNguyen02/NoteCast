package com.example.notecast.core.asr

/**
 * Tokenizer abstraction cho PhoWhisper.
 * - Thực tế nên là SentencePiece hoặc BPE, ở đây chỉ skeleton.
 */
interface Tokenizer {
    val bosTokenId: Int
    val eosTokenId: Int

    fun encode(text: String): LongArray
    fun decode(ids: LongArray): String
}

