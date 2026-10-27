package com.explay.audio.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.explay.audio.data.model.AudioProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "explay_audio_prefs")

class AudioPreferencesRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }

    private object PrefKeys {
        val ENGINE_ENABLED = booleanPreferencesKey("engine_enabled")
        val CURRENT_PROFILE_ID = stringPreferencesKey("current_profile_id")
        val ACTIVE_PROFILE_DATA = stringPreferencesKey("active_profile_data")
        val CUSTOM_PROFILES_JSON = stringPreferencesKey("custom_profiles_json")
        val FORCE_SPEAKER = booleanPreferencesKey("force_speaker")
        val FORCE_BLUETOOTH = booleanPreferencesKey("force_bluetooth")
        val AUTO_MASTER_ENABLED = booleanPreferencesKey("auto_master_enabled")
    }

    val isAutoMasterEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PrefKeys.AUTO_MASTER_ENABLED] ?: false
    }

    val isEngineEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PrefKeys.ENGINE_ENABLED] ?: true
    }

    val forceSpeaker: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PrefKeys.FORCE_SPEAKER] ?: true
    }

    val forceBluetooth: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PrefKeys.FORCE_BLUETOOTH] ?: true
    }

    val currentProfile: Flow<AudioProfile> = context.dataStore.data.map { prefs ->
        val savedJson = prefs[PrefKeys.ACTIVE_PROFILE_DATA]
        if (!savedJson.isNullOrBlank()) {
            try {
                json.decodeFromString<AudioProfile>(savedJson)
            } catch (_: Exception) {
                AudioProfile.PRESETS.first()
            }
        } else {
            AudioProfile.PRESETS.first()
        }
    }

    val customProfiles: Flow<List<AudioProfile>> = context.dataStore.data.map { prefs ->
        val rawJson = prefs[PrefKeys.CUSTOM_PROFILES_JSON]
        if (!rawJson.isNullOrBlank()) {
            try {
                json.decodeFromString<List<AudioProfile>>(rawJson)
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    suspend fun setEngineEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PrefKeys.ENGINE_ENABLED] = enabled
        }
    }

    suspend fun setForceSpeaker(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PrefKeys.FORCE_SPEAKER] = enabled
        }
    }

    suspend fun setForceBluetooth(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PrefKeys.FORCE_BLUETOOTH] = enabled
        }
    }

    suspend fun setAutoMasterEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PrefKeys.AUTO_MASTER_ENABLED] = enabled
        }
    }

    suspend fun saveCurrentProfile(profile: AudioProfile) {
        context.dataStore.edit { prefs ->
            prefs[PrefKeys.CURRENT_PROFILE_ID] = profile.id
            prefs[PrefKeys.ACTIVE_PROFILE_DATA] = json.encodeToString(profile)
        }
    }

    suspend fun saveCustomProfile(profile: AudioProfile) {
        context.dataStore.edit { prefs ->
            val raw = prefs[PrefKeys.CUSTOM_PROFILES_JSON]
            val existing = if (!raw.isNullOrBlank()) {
                try {
                    json.decodeFromString<List<AudioProfile>>(raw).toMutableList()
                } catch (_: Exception) {
                    mutableListOf()
                }
            } else {
                mutableListOf()
            }

            val index = existing.indexOfFirst { it.id == profile.id }
            if (index >= 0) {
                existing[index] = profile
            } else {
                existing.add(profile)
            }
            prefs[PrefKeys.CUSTOM_PROFILES_JSON] = json.encodeToString(existing.toList())
            prefs[PrefKeys.CURRENT_PROFILE_ID] = profile.id
            prefs[PrefKeys.ACTIVE_PROFILE_DATA] = json.encodeToString(profile)
        }
    }

    suspend fun deleteCustomProfile(profileId: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[PrefKeys.CUSTOM_PROFILES_JSON]
            if (!raw.isNullOrBlank()) {
                try {
                    val existing = json.decodeFromString<List<AudioProfile>>(raw)
                        .filter { it.id != profileId }
                    prefs[PrefKeys.CUSTOM_PROFILES_JSON] = json.encodeToString(existing)
                } catch (_: Exception) {
                    // Ignore corrupted profile json
                }
            }
        }
    }
}
