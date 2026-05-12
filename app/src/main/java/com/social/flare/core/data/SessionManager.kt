package com.social.flare.core.data

import android.R
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_session")
class SessionManager(private val context: Context){
    companion object{
        private val ACTIVE_CITIZEN_ID = stringPreferencesKey("active_citizen_id")
    }
    val activeCitizenIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACTIVE_CITIZEN_ID]
    }
    suspend fun saveSession(citizenId: String){
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_CITIZEN_ID] = citizenId;
        }
    }
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACTIVE_CITIZEN_ID)
        }
    }
}