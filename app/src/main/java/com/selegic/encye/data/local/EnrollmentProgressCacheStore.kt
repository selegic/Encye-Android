package com.selegic.encye.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.selegic.encye.data.remote.dto.UserProgressDto
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.enrollmentCacheDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "enrollment_cache"
)

@Singleton
class EnrollmentProgressCacheStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private companion object {
        val ALL_ENROLLMENTS_JSON = stringPreferencesKey("all_enrollments_json")
        val ALL_ENROLLMENTS_CACHED_AT = longPreferencesKey("all_enrollments_cached_at")
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun observeAllEnrollments(): Flow<List<UserProgressDto>> {
        return context.enrollmentCacheDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[ALL_ENROLLMENTS_JSON]
                    ?.takeIf { it.isNotBlank() }
                    ?.let { encoded -> json.decodeFromString<List<UserProgressDto>>(encoded) }
                    .orEmpty()
            }
    }

    suspend fun saveAllEnrollments(enrollments: List<UserProgressDto>) {
        val cachedAt = System.currentTimeMillis()
        context.enrollmentCacheDataStore.edit { preferences ->
            preferences[ALL_ENROLLMENTS_JSON] = json.encodeToString(enrollments)
            preferences[ALL_ENROLLMENTS_CACHED_AT] = cachedAt
        }
    }

    suspend fun clearAllEnrollments() {
        context.enrollmentCacheDataStore.edit { preferences ->
            preferences.remove(ALL_ENROLLMENTS_JSON)
            preferences.remove(ALL_ENROLLMENTS_CACHED_AT)
        }
    }
}
