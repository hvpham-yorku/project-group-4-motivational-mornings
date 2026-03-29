package com.example.motivationalmornings.Persistence

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPrefs {
    private val IMAGE_URI_KEY = stringPreferencesKey("image_of_day_uri")
    private val NOTIF_HOUR_KEY = intPreferencesKey("notif_hour")
    private val NOTIF_MINUTE_KEY = intPreferencesKey("notif_minute")
    private val NOTIF_ENABLED_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("notif_enabled")

    fun imageUriFlow(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs: Preferences ->
            prefs[IMAGE_URI_KEY]
        }
    }

    suspend fun saveImageUri(context: Context, uri: String) {
        context.dataStore.edit { prefs ->
            prefs[IMAGE_URI_KEY] = uri
        }
    }

    suspend fun clearImageUri(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(IMAGE_URI_KEY)
        }
    }

    // Notification Settings
    fun notificationSettingsFlow(context: Context): Flow<Triple<Boolean, Int, Int>> {
        return context.dataStore.data.map { prefs ->
            Triple(
                prefs[NOTIF_ENABLED_KEY] ?: false,
                prefs[NOTIF_HOUR_KEY] ?: 8,
                prefs[NOTIF_MINUTE_KEY] ?: 0
            )
        }
    }

    suspend fun saveNotificationSettings(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[NOTIF_ENABLED_KEY] = enabled
            prefs[NOTIF_HOUR_KEY] = hour
            prefs[NOTIF_MINUTE_KEY] = minute
        }
    }
}
