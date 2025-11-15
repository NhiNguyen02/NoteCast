package com.example.notecast.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room TypeConverters using kotlinx.serialization
 * - Use Json.decodeFromString / encodeToString (no reflection)
 * - Works well with Kotlin + Jetpack Compose projects
 *
 * Make sure to add kotlinx-serialization-json dependency and the kotlin serialization plugin.
 */
object Converters {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    @TypeConverter
    @JvmStatic
    fun fromStringList(value: String?): List<String> {
        return if (value.isNullOrEmpty()) emptyList()
        else json.decodeFromString(value)
    }

    @TypeConverter
    @JvmStatic
    fun toStringList(list: List<String>?): String {
        return json.encodeToString(list ?: emptyList())
    }
}