package com.example.notecast.domain.vad

/**
 * Domain-level interface: input = PCM 16kHz mono ShortArray, output boolean speech/no-speech.
 */
interface VADDetector {
    /**
     * @param pcm frame of audio in PCM16 (mono, 16kHz), expected frame size according to impl
     * @return true if frame contains speech (detected)
     */
    fun isSpeech(pcm: ShortArray): Boolean
}