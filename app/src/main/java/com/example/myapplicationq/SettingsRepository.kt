package com.example.myapplicationq

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "wallpaper_prefs")

class SettingsRepository private constructor(private val context: Context) {
    companion object {
        @Volatile private var INSTANCE: SettingsRepository? = null
        fun getInstance(context: Context): SettingsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }

        // Preference keys
        private val MORNING_START = intPreferencesKey("morning_start")
        private val MORNING_END = intPreferencesKey("morning_end")
        private val NIGHT_START = intPreferencesKey("night_start")
        private val INTERVAL_MIN = intPreferencesKey("wallpaper_interval_min")
        private val MORNING_QUOTES = stringPreferencesKey("morning_quotes_json")
        private val NIGHT_QUOTES = stringPreferencesKey("night_quotes_json")
        private val DEFAULT_QUOTES = stringPreferencesKey("default_quotes_json")
    }

    // ----- Primitive values -----
    fun getMorningStart(): Flow<Int?> = context.dataStore.data.map { it[MORNING_START] }
    suspend fun setMorningStart(value: Int) = context.dataStore.updateData { it.toMutablePreferences().apply { this[MORNING_START] = value } }

    fun getMorningEnd(): Flow<Int?> = context.dataStore.data.map { it[MORNING_END] }
    suspend fun setMorningEnd(value: Int) = context.dataStore.updateData { it.toMutablePreferences().apply { this[MORNING_END] = value } }

    fun getNightStart(): Flow<Int?> = context.dataStore.data.map { it[NIGHT_START] }
    suspend fun setNightStart(value: Int) = context.dataStore.updateData { it.toMutablePreferences().apply { this[NIGHT_START] = value } }

    fun getIntervalMinutes(): Flow<Int?> = context.dataStore.data.map { it[INTERVAL_MIN] }
    suspend fun setIntervalMinutes(value: Int) = context.dataStore.updateData { it.toMutablePreferences().apply { this[INTERVAL_MIN] = value } }

    // ----- Quote lists -----
    private val json = Json { ignoreUnknownKeys = true }

    fun getMorningQuotes(): Flow<Array<String>?> = context.dataStore.data.map { prefs ->
        prefs[MORNING_QUOTES]?.let { json.decodeFromString(it) }
    }
    suspend fun setMorningQuotes(list: List<String>) = context.dataStore.updateData {
        it.toMutablePreferences().apply { this[MORNING_QUOTES] = json.encodeToString(list) }
    }

    fun getNightQuotes(): Flow<Array<String>?> = context.dataStore.data.map { prefs ->
        prefs[NIGHT_QUOTES]?.let { json.decodeFromString(it) }
    }
    suspend fun setNightQuotes(list: List<String>) = context.dataStore.updateData {
        it.toMutablePreferences().apply { this[NIGHT_QUOTES] = json.encodeToString(list) }
    }

    fun getDefaultQuotes(): Flow<Array<String>?> = context.dataStore.data.map { prefs ->
        prefs[DEFAULT_QUOTES]?.let { json.decodeFromString(it) }
    }
    suspend fun setDefaultQuotes(list: List<String>) = context.dataStore.updateData {
        it.toMutablePreferences().apply { this[DEFAULT_QUOTES] = json.encodeToString(list) }
    }

    // ----- Reset helpers (manual only) -----
    suspend fun resetMorningQuotes() {
        context.dataStore.updateData {
            it.toMutablePreferences().apply { remove(MORNING_QUOTES) }
        }
    }
    suspend fun resetNightQuotes() {
        context.dataStore.updateData {
            it.toMutablePreferences().apply { remove(NIGHT_QUOTES) }
        }
    }
    suspend fun resetDefaultQuotes() {
        context.dataStore.updateData {
            it.toMutablePreferences().apply { remove(DEFAULT_QUOTES) }
        }
    }
}
