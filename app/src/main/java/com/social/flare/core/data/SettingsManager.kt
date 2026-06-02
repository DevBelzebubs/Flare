package com.social.flare.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

class SettingsManager(private val context: Context) {
    companion object {
        private val PUSH_NOTIFICATIONS_ENABLED = booleanPreferencesKey("push_notifications_enabled")
        private val EMAIL_NOTIFICATIONS_ENABLED = booleanPreferencesKey("email_notifications_enabled")
        private val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        private val TEXT_SIZE_SCALE = floatPreferencesKey("text_size_scale")
    }

    val pushNotificationsEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[PUSH_NOTIFICATIONS_ENABLED] ?: false
    }

    val emailNotificationsEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[EMAIL_NOTIFICATIONS_ENABLED] ?: false
    }

    val darkModeEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[DARK_MODE_ENABLED] ?: true
    }

    val textSizeScaleFlow: Flow<Float> = context.settingsDataStore.data.map { preferences ->
        preferences[TEXT_SIZE_SCALE] ?: 0.5f
    }

    suspend fun setPushNotificationsEnabled(value: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[PUSH_NOTIFICATIONS_ENABLED] = value
        }
    }

    suspend fun setEmailNotificationsEnabled(value: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[EMAIL_NOTIFICATIONS_ENABLED] = value
        }
    }

    suspend fun setDarkModeEnabled(value: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[DARK_MODE_ENABLED] = value
        }
    }

    suspend fun setTextSizeScale(value: Float) {
        context.settingsDataStore.edit { preferences ->
            preferences[TEXT_SIZE_SCALE] = value.coerceIn(0f, 1f)
        }
    }
}
