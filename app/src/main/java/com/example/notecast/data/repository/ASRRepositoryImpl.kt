package com.example.notecast.data.repository

import android.util.Log
import com.example.notecast.data.local.AudioData
import com.example.notecast.data.local.Transcript
import com.example.notecast.core.asr.ASRCore
import com.example.notecast.domain.repository.ASRRepository
import javax.inject.Inject

class ASRRepositoryImpl @Inject constructor(
    private val asrCore: ASRCore
) : ASRRepository {
    private val TAG = "ASRRepository"
    override suspend fun transcribe(audioData: AudioData): Transcript {
        Log.d(TAG, "transcribe: repository received audioData.pcm.size=${audioData.pcm.size}")
        val result = asrCore.transcribe(audioData)
        Log.d(TAG, "transcribe: repository returning transcript length=${result.text.length}")
        return result
    }
}
