package com.example.notecast.data.repository

import com.example.notecast.data.local.dao.AudioDao
import com.example.notecast.data.local.mapper.EntityMapper
import com.example.notecast.domain.model.Audio
import com.example.notecast.domain.repository.AudioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation của AudioRepository (Chưa dùng Firebase).
 */
class AudioRepositoryImpl @Inject constructor(
    private val audioDao: AudioDao
) : AudioRepository {

    override fun allAudio(): Flow<List<Audio>> {
        return audioDao.allAudio().map { entityList ->
            entityList.map { EntityMapper.audioEntityToDomain(it) }
        }
    }

    override suspend fun getAudioById(id: String): Audio? {
        return audioDao.getById(id)?.let { EntityMapper.audioEntityToDomain(it) }
    }

    override suspend fun insertAudio(audio: Audio) {
        val entity = EntityMapper.domainToAudioEntity(audio).copy(
            id = UUID.randomUUID().toString(),
            isSynced = false,
            isDeleted = false,
            createdAt = Date().time
        )
        audioDao.insert(entity)
    }

    override suspend fun updateAudio(audio: Audio) {
        val entity = EntityMapper.domainToAudioEntity(audio).copy(isSynced = false)
        audioDao.update(entity)
    }

    override suspend fun deleteAudio(id: String) {
        val entity = audioDao.getById(id)
        if (entity != null) {
            val deletedEntity = entity.copy(
                isDeleted = true,
                isSynced = false,
                cloudUrl = null // Xóa URL cloud nếu xóa
            )
            audioDao.update(deletedEntity)
        }
    }

    override suspend fun uploadPendingAudio() {
        // Người dùng yêu cầu "chưa dùng firebase"
        println("Sync logic for Audio is skipped (Firebase not included).")
    }
}