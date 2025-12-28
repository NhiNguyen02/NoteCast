package com.example.notecast.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from DB version 1 -> 2.
 *
 * - Chuẩn hoá schema bảng note theo NoteEntity mới (type, rawText, normalizedText, summary, keywordsJson, mindmapJson, status,...).
 * - Chuẩn hoá bảng audio theo AudioEntity mới (noteId PK 1-1, durationSec, sampleRate, chunksJson,...).
 * - Thêm cột isSynced / isDeleted cho note, folder.
 * - Xoá các bảng transcript / processed_text (đã gộp vào Note/Audio).
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. TẠO BẢNG NOTE MỚI
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS note_new (
                id TEXT NOT NULL PRIMARY KEY,
                type TEXT NOT NULL,
                title TEXT,
                rawText TEXT,
                normalizedText TEXT,
                summary TEXT,
                keywordsJson TEXT,
                mindmapJson TEXT,
                status TEXT NOT NULL,
                folderId TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                isSynced INTEGER NOT NULL DEFAULT 0,
                isDeleted INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        // 2. COPY DỮ LIỆU CƠ BẢN TỪ BẢNG NOTE CŨ (NẾU TỒN TẠI CÁC CỘT)
        // Giả định v1 có các cột: id, noteType, title, content, mindMapJson, folderId, createdAt, updatedAt
        // Các cột mới khác sẽ được default/null.
        db.execSQL(
            """
            INSERT INTO note_new (id, type, title, rawText, normalizedText, summary, keywordsJson, mindmapJson, status, folderId, createdAt, updatedAt, isSynced, isDeleted)
            SELECT id,
                   CASE noteType WHEN 'VOICE' THEN 'audio' ELSE 'text' END AS type,
                   title,
                   NULL AS rawText,
                   NULL AS normalizedText,
                   NULL AS summary,
                   NULL AS keywordsJson,
                   mindMapJson AS mindmapJson,
                   'created' AS status,
                   folderId,
                   createdAt,
                   updatedAt,
                   0 AS isSynced,
                   0 AS isDeleted
            FROM note
            """.trimIndent()
        )

        // 3. XOÁ BẢNG NOTE CŨ VÀ ĐỔI TÊN note_new -> note
        db.execSQL("DROP TABLE note")
        db.execSQL("ALTER TABLE note_new RENAME TO note")

        // 4. TẠO BẢNG AUDIO MỚI (NẾU CẦN)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS audio_new (
                noteId TEXT NOT NULL PRIMARY KEY,
                durationSec REAL NOT NULL,
                sampleRate INTEGER NOT NULL,
                chunksJson TEXT,
                localFilePath TEXT,
                cloudUrl TEXT,
                createdAt INTEGER NOT NULL,
                asrModel TEXT
            )
            """.trimIndent()
        )

        // 5. CỐ GẮNG COPY DỮ LIỆU TỪ BẢNG AUDIO CŨ (NẾU TỒN TẠI CÁC CỘT)
        try {
            db.execSQL(
                """
                INSERT INTO audio_new (noteId, durationSec, sampleRate, chunksJson, localFilePath, cloudUrl, createdAt, asrModel)
                SELECT noteId,
                       CAST(durationMs AS REAL) / 1000.0 AS durationSec,
                       sampleRate,
                       NULL AS chunksJson,
                       filePath AS localFilePath,
                       cloudUrl,
                       createdAt,
                       NULL AS asrModel
                FROM audio
                """.trimIndent()
            )
            db.execSQL("DROP TABLE audio")
            db.execSQL("ALTER TABLE audio_new RENAME TO audio")
        } catch (_: Throwable) {
            // nếu bảng audio cũ không có hoặc schema khác, bỏ qua
            db.execSQL("DROP TABLE IF EXISTS audio_new")
        }

        // 6. XOÁ BẢNG transcript / processed_text (nếu tồn tại)
        db.execSQL("DROP TABLE IF EXISTS transcript")
        db.execSQL("DROP TABLE IF EXISTS processed_text")

        // 7. THÊM CỘT isSynced / isDeleted CHO FOLDER (NẾU CHƯA CÓ)
        try {
            db.execSQL("ALTER TABLE folder ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        } catch (_: Throwable) { }
        try {
            db.execSQL("ALTER TABLE folder ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        } catch (_: Throwable) { }
    }
}