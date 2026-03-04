package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.TrainingApiService
import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.TrainingDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class TrainingRepositoryImpl @Inject constructor(
    private val trainingApiService: TrainingApiService
) : TrainingRepository {

    override suspend fun createTraining(
        title: String,
        coverImage: File?,
        summary: String?,
        moduleIds: List<String>?,
        organizationId: String?,
        similarTrainingIds: List<String>?
    ): ApiResponse<TrainingDto> {
        return trainingApiService.createTraining(
            title = title.toPlainTextRequestBody(),
            coverImage = coverImage?.toImagePart("coverImage"),
            summary = summary?.toPlainTextRequestBody(),
            module = moduleIds?.joinToString(",")?.toPlainTextRequestBody(),
            organization = organizationId?.toPlainTextRequestBody(),
            similar = similarTrainingIds?.joinToString(",")?.toPlainTextRequestBody()
        )
    }

    override suspend fun getAllTrainings(page: Int, limit: Int): ApiResponse<List<TrainingDto>> {
        return trainingApiService.getAllTrainings(page, limit)
    }

    override suspend fun getOrganizationTrainings(
        page: Int,
        limit: Int
    ): ApiResponse<List<TrainingDto>> {
        return trainingApiService.getOrganizationTrainings(page, limit)
    }

    override suspend fun getTrainingById(id: String): ApiResponse<TrainingDto> {
        return trainingApiService.getTrainingById(id)
    }

    override suspend fun updateTraining(
        id: String,
        title: String?,
        coverImage: File?,
        summary: String?,
        moduleIds: List<String>?,
        similarTrainingIds: List<String>?
    ): ApiResponse<TrainingDto> {
        return trainingApiService.updateTraining(
            id = id,
            title = title?.toPlainTextRequestBody(),
            summary = summary?.toPlainTextRequestBody(),
            coverImage = coverImage?.toImagePart("coverImage"),
            module = moduleIds?.joinToString(",")?.toPlainTextRequestBody(),
            similar = similarTrainingIds?.joinToString(",")?.toPlainTextRequestBody()
        )
    }

    override suspend fun deleteTraining(id: String): ApiResponse<Unit> {
        return trainingApiService.deleteTraining(id)
    }

    private fun String.toPlainTextRequestBody(): RequestBody {
        return toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun File.toImagePart(partName: String): MultipartBody.Part {
        val requestFile = asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, name, requestFile)
    }
}
