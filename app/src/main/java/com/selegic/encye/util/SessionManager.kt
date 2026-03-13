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

data class CachedUserProfile(
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePicture: String? = null
) {
    val displayName: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .ifBlank { "You" }
}

class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val FIRST_NAME = stringPreferencesKey("first_name")
        private val LAST_NAME = stringPreferencesKey("last_name")
        private val PROFILE_PICTURE = stringPreferencesKey("profile_picture")
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit {
            it[AUTH_TOKEN] = token
        }
    }

    suspend fun saveCachedUserProfile(
        firstName: String?,
        lastName: String?,
        profilePicture: String?
    ) {
        context.dataStore.edit {
            if (firstName.isNullOrBlank()) {
                it.remove(FIRST_NAME)
            } else {
                it[FIRST_NAME] = firstName
            }

            if (lastName.isNullOrBlank()) {
                it.remove(LAST_NAME)
            } else {
                it[LAST_NAME] = lastName
            }

            if (profilePicture.isNullOrBlank()) {
                it.remove(PROFILE_PICTURE)
            } else {
                it[PROFILE_PICTURE] = profilePicture
            }
        }
    }

    suspend fun getCachedUserProfile(): CachedUserProfile {
        val preferences = context.dataStore.data.first()
        return CachedUserProfile(
            firstName = preferences[FIRST_NAME],
            lastName = preferences[LAST_NAME],
            profilePicture = preferences[PROFILE_PICTURE]
        )
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
