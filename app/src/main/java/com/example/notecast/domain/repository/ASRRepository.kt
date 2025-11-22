package com.example.notecast.domain.repository

import com.example.notecast.data.local.AudioData
import com.example.notecast.data.local.Transcript


interface ASRRepository {
    suspend fun transcribe(audioData: AudioData): Transcript
}
