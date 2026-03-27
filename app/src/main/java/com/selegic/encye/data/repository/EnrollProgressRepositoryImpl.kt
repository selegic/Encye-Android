package com.selegic.encye.data.repository
import android.util.Log
import com.selegic.encye.data.local.EnrollmentProgressCacheStore
import com.selegic.encye.data.remote.EnrollProgressApiService
import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.EnrollUserRequest
import com.selegic.encye.data.remote.dto.UpdateProgressRequest
import com.selegic.encye.data.remote.dto.UserEnrollmentDto
import com.selegic.encye.data.remote.dto.UserProgressDto
import kotlinx.coroutines.coroutineScope
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "EnrollProgressRepository"
@Singleton
class EnrollProgressRepositoryImpl @Inject constructor(
    private val enrollProgressApiService: EnrollProgressApiService,
    private val enrollmentProgressCacheStore: EnrollmentProgressCacheStore
) : EnrollProgressRepository {
    override suspend fun enrollUser(trainingId: String): ApiResponse<UserEnrollmentDto> {
        return enrollProgressApiService.enrollUser(
            EnrollUserRequest(trainingId = trainingId)
        ).also { response ->
            if (response.success) {
                coroutineScope { launch { runCatching { refreshAllEnrollments() } } }
            }
        }
    }

    override suspend fun getEnrollments(): ApiResponse<List<UserProgressDto>> {
        return enrollProgressApiService.getEnrollments()
    }

    override fun getAllEnrollments(): Flow<List<UserProgressDto>> = channelFlow {
        launch {
            runCatching { refreshAllEnrollments() }
                .onFailure { it.printStackTrace() }
        }
        enrollmentProgressCacheStore.observeAllEnrollments().collect {
            send(it)
        }
    }

    override suspend fun refreshAllEnrollments() {
        val response = enrollProgressApiService.getEnrollments()
        if (response.success) {
            enrollmentProgressCacheStore.saveAllEnrollments(response.data.orEmpty())
        }
        Log.i(TAG, "refreshAllEnrollments: ${response.data}")
    }

    override suspend fun updateProgress(
        trainingId: String,
        moduleId: String,
        moduleStatus: String
    ): ApiResponse<UserProgressDto> {
        return enrollProgressApiService.updateProgress(
            UpdateProgressRequest(
                trainingId = trainingId,
                moduleId = moduleId,
                moduleStatus = moduleStatus
            )
        ).also { response ->
            if (response.success) {
                runCatching { refreshAllEnrollments() }
            }
        }
    }
}
