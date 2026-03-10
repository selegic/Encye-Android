package com.selegic.encye.util

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

    suspend fun getCurrentUserId(): String? {
        return getAuthToken()?.let(::extractUserIdFromJwt)
    }

    suspend fun clearAuthToken() {
        context.dataStore.edit {
            it.clear()
        }
    }

    private fun extractUserIdFromJwt(token: String): String? {
        return try {
            val payload = token.split(".").getOrNull(1) ?: return null
            val decoded = Base64.decode(
                payload,
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
            Json.parseToJsonElement(decoded.decodeToString())
                .jsonObject["id"]
                ?.jsonPrimitive
                ?.contentOrNull
        } catch (_: Exception) {
            null
        }
    }
}
