package com.example.motivationalmornings.Persistence

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPrefs {
    private val IMAGE_URI_KEY = stringPreferencesKey("image_of_day_uri")

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
}