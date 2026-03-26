package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.EnrollProgressApiService
import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.EnrollUserRequest
import com.selegic.encye.data.remote.dto.UpdateProgressRequest
import com.selegic.encye.data.remote.dto.UserEnrollmentDto
import com.selegic.encye.data.remote.dto.UserProgressDto
import javax.inject.Inject

class EnrollProgressRepositoryImpl @Inject constructor(
    private val enrollProgressApiService: EnrollProgressApiService
) : EnrollProgressRepository {

    override suspend fun enrollUser(trainingId: String): ApiResponse<UserEnrollmentDto> {
        return enrollProgressApiService.enrollUser(
            EnrollUserRequest(trainingId = trainingId)
        )
    }

    override suspend fun getEnrollments(): ApiResponse<List<UserProgressDto>> {
        return enrollProgressApiService.getEnrollments()
    }

    override suspend fun getAllEnrollments(): ApiResponse<List<UserProgressDto>> {
        return enrollProgressApiService.getAllEnrollments()
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
        )
    }
}
