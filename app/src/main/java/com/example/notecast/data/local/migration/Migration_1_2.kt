package com.example.notecast.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration example from DB version 1 -> 2
 * Adds isSynced and isDeleted columns with default 0.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add columns to audio
        database.execSQL("ALTER TABLE audio ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE audio ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        // Add columns to transcript
        database.execSQL("ALTER TABLE transcript ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE transcript ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        // Add columns to processed_text
        database.execSQL("ALTER TABLE processed_text ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE processed_text ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        // Add columns to note
        database.execSQL("ALTER TABLE note ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE note ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        // Add columns to folder (if folder exists)
        try {
            database.execSQL("ALTER TABLE folder ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE folder ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        } catch (t: Throwable) {
            // if folder table doesn't exist in v1, ignore
        }
    }
}