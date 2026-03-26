package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.UserEnrollmentDto
import com.selegic.encye.data.remote.dto.UserProgressDto

interface EnrollProgressRepository {

    suspend fun enrollUser(trainingId: String): ApiResponse<UserEnrollmentDto>

    suspend fun getEnrollments(): ApiResponse<List<UserProgressDto>>

    suspend fun getAllEnrollments(): ApiResponse<List<UserProgressDto>>

    suspend fun updateProgress(
        trainingId: String,
        moduleId: String,
        moduleStatus: String
    ): ApiResponse<UserProgressDto>
}
