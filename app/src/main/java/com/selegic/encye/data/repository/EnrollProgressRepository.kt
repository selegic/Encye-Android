package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.UserEnrollmentDto
import com.selegic.encye.data.remote.dto.UserProgressDto
import kotlinx.coroutines.flow.Flow

interface EnrollProgressRepository {

    suspend fun enrollUser(trainingId: String): ApiResponse<UserEnrollmentDto>

    suspend fun getEnrollments(): ApiResponse<List<UserProgressDto>>

    fun getAllEnrollments(): Flow<List<UserProgressDto>>

    suspend fun refreshAllEnrollments()

    suspend fun updateProgress(
        trainingId: String,
        moduleId: String,
        moduleStatus: String
    ): ApiResponse<UserProgressDto>
}
