package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.TrainingDto
import java.io.File

interface TrainingRepository {

    suspend fun createTraining(
        title: String,
        coverImage: File? = null,
        summary: String? = null,
        moduleIds: List<String>? = null,
        organizationId: String? = null,
        similarTrainingIds: List<String>? = null
    ): ApiResponse<TrainingDto>

    suspend fun getAllTrainings(page: Int = 1, limit: Int = 8): ApiResponse<List<TrainingDto>>

    suspend fun getOrganizationTrainings(
        page: Int = 1,
        limit: Int = 8
    ): ApiResponse<List<TrainingDto>>

    suspend fun getTrainingById(id: String): ApiResponse<TrainingDto>

    suspend fun updateTraining(
        id: String,
        title: String? = null,
        coverImage: File? = null,
        summary: String? = null,
        moduleIds: List<String>? = null,
        similarTrainingIds: List<String>? = null
    ): ApiResponse<TrainingDto>

    suspend fun deleteTraining(id: String): ApiResponse<Unit>
}
