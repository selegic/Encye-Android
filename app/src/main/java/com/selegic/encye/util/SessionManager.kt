package com.selegic.encye.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit {
            it[AUTH_TOKEN] = token
        }
    }

    suspend fun getAuthToken(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[AUTH_TOKEN]
    }

    suspend fun clearAuthToken() {
        context.dataStore.edit {
            it.clear()
        }
    }

}