package com.example.notecast.domain.repository

import com.example.notecast.domain.model.Audio
import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    fun allAudio(): Flow<List<Audio>>
    suspend fun getAudioById(id: String): Audio?
    suspend fun insertAudio(audio: Audio)
    suspend fun updateAudio(audio: Audio)
    suspend fun deleteAudio(id: String)
    suspend fun uploadPendingAudio() // orchestration entrypoint if repo handles upload
}