package com.example.notecast.domain.usecase

// This use case is no longer used now that VOICE note saving is handled in NoteEditViewModel.
// Keeping file for reference; can be safely deleted once confirmed unused in the project.


//import com.example.notecast.domain.model.Note
//import com.example.notecast.domain.repository.NoteRepository
//import java.util.UUID
//import javax.inject.Inject
//
///**
// * SaveVoiceNoteFromASRUseCase
// *
// * Use case tạo và lưu một VOICE Note từ kết quả ASR + metadata audio.
// * Trả về id của Note đã lưu để UI có thể điều hướng sang màn hình Edit.
// */
//class SaveVoiceNoteFromASRUseCase @Inject constructor(
//    private val noteRepository: NoteRepository,
//) {
//
//    /**
//     * @param finalText  transcript cuối cùng từ ASR
//     * @param audioFilePath  đường dẫn file audio đã lưu (đã nén)
//     * @param durationMs  tổng thời lượng audio (ms)
//     * @param sampleRate  sample rate audio (Hz)
//     * @param channels    số kênh audio (thường là 1 cho mono)
//     * @return id của Note vừa được lưu
//     */
//    suspend operator fun invoke(
//        finalText: String,
//        audioFilePath: String,
//        durationMs: Long,
//        sampleRate: Int,
//        channels: Int,
//    ): String {
//        val now = System.currentTimeMillis()
//        val noteId = UUID.randomUUID().toString()
//
//        val note = Note(
//            id = noteId,
//            noteType = "VOICE",
//            title = "", // UI/màn Edit có thể cho phép người dùng đặt lại tiêu đề
//            content = null,
//            tags = emptyList(),
//            mindMapJson = null,
//            isFavorite = false,
//            pinTimestamp = null,
//            folderId = null,
//            colorHex = null,
//            updatedAt = now,
//            createdAt = now,
//            // Audio info
//            filePath = audioFilePath,
//            cloudUrl = null,
//            durationMs = durationMs,
//            // Transcript gốc từ ASR
//            rawText = finalText,
//            timestampsJson = null,
//            // Processed text (chưa có)
//            punctuatedText = null,
//            summary = null,
//            sentiment = null,
//            // Metadata đồng bộ
//            isSynced = false,
//            isDeleted = false,
//        )
//
//        noteRepository.saveNote(note)
//        return noteId
//    }
//}

