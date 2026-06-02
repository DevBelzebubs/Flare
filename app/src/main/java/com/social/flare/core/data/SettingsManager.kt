package com.social.flare.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

class SettingsManager(private val context: Context) {
    companion object {
        private val PUSH_NOTIFICATIONS_ENABLED = booleanPreferencesKey("push_notifications_enabled")
    }

    val pushNotificationsEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[PUSH_NOTIFICATIONS_ENABLED] ?: false
    }

    suspend fun setPushNotificationsEnabled(value: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[PUSH_NOTIFICATIONS_ENABLED] = value
        }
    }
}
