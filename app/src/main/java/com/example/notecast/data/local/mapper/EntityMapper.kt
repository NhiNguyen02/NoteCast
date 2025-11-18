package com.example.notecast.data.local.mapper

import com.example.notecast.data.local.entities.AudioEntity
import com.example.notecast.data.local.entities.FolderEntity
import com.example.notecast.data.local.entities.NoteEntity
import com.example.notecast.data.local.entities.NoteWithDetails
import com.example.notecast.data.local.entities.ProcessedTextEntity
import com.example.notecast.data.local.entities.TranscriptEntity
import com.example.notecast.domain.model.Folder
import com.example.notecast.domain.model.Note
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Mapper giữa Room Entities (Data Layer) và Domain Models (Domain Layer).
 * Được viết lại để hỗ trợ cấu trúc "hub-and-spoke" (NoteWithDetails).
 */
object EntityMapper {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    // --- Folder Mapping (1-1) ---

    fun folderEntityToDomain(e: FolderEntity): Folder = Folder(
        id = e.id,
        name = e.name,
        colorHex = e.colorHex,
        createdAt = e.createdAt,
        updatedAt = e.updatedAt,
        isSynced = e.isSynced,
        isDeleted = e.isDeleted
    )

    fun domainToFolderEntity(d: Folder): FolderEntity = FolderEntity(
        id = d.id,
        name = d.name,
        colorHex = d.colorHex,
        createdAt = d.createdAt,
        updatedAt = d.updatedAt,
        isSynced = d.isSynced,
        isDeleted = d.isDeleted
    )

    // --- Note Mapping (Phức tạp) ---

    /**
     * HÀM QUAN TRỌNG NHẤT (ĐỌC): Gộp NoteWithDetails -> Note (Domain)
     * Chuyển đổi POJO NoteWithDetails (gom 4 bảng) thành 1 Domain Model Note duy nhất.
     */
    fun noteWithDetailsToDomain(nwd: NoteWithDetails): Note {
        val noteEntity = nwd.note
        val audioEntity = nwd.audio
        val transcriptEntity = nwd.transcript
        val processedEntity = nwd.processedText

        // Logic kiểm tra isSynced: Chỉ coi là synced nếu TẤT CẢ các phần đã synced
        val isGloballySynced = noteEntity.isSynced &&
                (audioEntity?.isSynced ?: true) &&
                (transcriptEntity?.isSynced ?: true) &&
                (processedEntity?.isSynced ?: true)

        return Note(
            // Thông tin cơ bản (Từ NoteEntity)
            id = noteEntity.id,
            noteType = noteEntity.noteType,
            title = noteEntity.title,
            content = noteEntity.content,
            tags = try {
                if (noteEntity.tags.isBlank()) emptyList() else json.decodeFromString(noteEntity.tags)
            } catch (e: Exception) {
                emptyList()
            },
            mindMapJson = noteEntity.mindMapJson,
            isFavorite = noteEntity.isFavorite,
            pinTimestamp = noteEntity.pinTimestamp,
            folderId = noteEntity.folderId,
            colorHex = noteEntity.colorHex,
            updatedAt = noteEntity.updatedAt,
            isDeleted = noteEntity.isDeleted, // Lấy từ NoteEntity chính
            isSynced = isGloballySynced, // Dùng logic đã tính

            // Thông tin Audio (Từ AudioEntity)
            filePath = audioEntity?.filePath,
            cloudUrl = audioEntity?.cloudUrl,
            durationMs = audioEntity?.durationMs,

            // Thông tin Transcript (Từ TranscriptEntity)
            rawText = transcriptEntity?.rawText,
            timestampsJson = transcriptEntity?.timestampsJson,

            // Thông tin Đã xử lý (Từ ProcessedTextEntity)
            punctuatedText = processedEntity?.punctuatedText,
            summary = processedEntity?.summary,
            sentiment = processedEntity?.sentiment
        )
    }

    /**
     * HÀM "TÁCH" (GHI): Tách 1 Note (Domain) -> NoteEntity
     * Dùng khi Repository muốn GHI (upsert) xuống CSDL.
     */
    fun domainToNoteEntity(d: Note): NoteEntity = NoteEntity(
        id = d.id,
        noteType = d.noteType,
        title = d.title,
        content = d.content,
        tags = json.encodeToString(d.tags), // Chuyển List<String> về String JSON
        mindMapJson = d.mindMapJson,
        isFavorite = d.isFavorite,
        pinTimestamp = d.pinTimestamp,
        folderId = d.folderId,
        colorHex = d.colorHex,
        updatedAt = d.updatedAt,
        isSynced = d.isSynced,
        isDeleted = d.isDeleted
    )

    /**
     * HÀM "TÁCH" (GHI): Tách 1 Note (Domain) -> AudioEntity?
     * Trả về null nếu đây là Text Note.
     */
    fun domainToAudioEntity(d: Note): AudioEntity? {
        // Chỉ tạo AudioEntity nếu là VOICE note VÀ có thông tin audio
        if (d.noteType != "VOICE" || d.filePath == null || d.durationMs == null) {
            return null
        }
        return AudioEntity(
            id = d.id + "_audio", // Tạo ID riêng (hoặc dùng 1 ID mới)
            noteId = d.id, // Liên kết với Note
            filePath = d.filePath,
            cloudUrl = d.cloudUrl,
            durationMs = d.durationMs,
            // (sampleRate và channels - Domain model của bạn chưa có,
            // chúng ta sẽ cần thêm vào Note (domain) nếu bạn cần)
            sampleRate = 44100, // Giá trị mặc định
            channels = 1, // Giá trị mặc định
            createdAt = d.updatedAt, // (Nên là createdAt riêng)
            isSynced = d.isSynced,
            isDeleted = d.isDeleted
        )
    }

    /**
     * HÀM "TÁCH" (GHI): Tách 1 Note (Domain) -> TranscriptEntity?
     * Trả về null nếu không có rawText.
     */
    fun domainToTranscriptEntity(d: Note): TranscriptEntity? {
        if (d.noteType != "VOICE" || d.rawText == null) {
            return null
        }
        return TranscriptEntity(
            id = d.id + "_transcript", // Tạo ID riêng
            noteId = d.id, // Liên kết với Note
            rawText = d.rawText,
            timestampsJson = d.timestampsJson,
            language = "vi", // (Nên thêm vào Domain model)
            confidence = 0.9f, // (Nên thêm vào Domain model)
            createdAt = d.updatedAt,
            isSynced = d.isSynced,
            isDeleted = d.isDeleted
        )
    }

    /**
     * HÀM "TÁCH" (GHI): Tách 1 Note (Domain) -> ProcessedTextEntity?
     * Trả về null nếu không có dữ liệu xử lý.
     * Dùng cho cả TEXT và VOICE notes.
     */
    fun domainToProcessedTextEntity(d: Note): ProcessedTextEntity? {
        if (d.punctuatedText == null && d.summary == null && d.sentiment == null) {
            return null
        }
        return ProcessedTextEntity(
            id = d.id + "_processed", // Tạo ID riêng
            noteId = d.id, // Liên kết với Note
            punctuatedText = d.punctuatedText,
            summary = d.summary,
            sentiment = d.sentiment,
            createdAt = d.updatedAt,
            isSynced = d.isSynced,
            isDeleted = d.isDeleted
        )
    }
}