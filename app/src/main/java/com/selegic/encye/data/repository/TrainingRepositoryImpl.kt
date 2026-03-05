package com.selegic.encye.data.repository

import com.selegic.encye.data.local.AppDatabase
import com.selegic.encye.data.local.entity.TrainingEntity
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
    private val trainingApiService: TrainingApiService,
    private val appDatabase: AppDatabase
) : TrainingRepository {

    private val trainingDao = appDatabase.trainingDao

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

    override suspend fun getCachedTrainings(): List<TrainingDto> {
        return trainingDao.getAll().map { it.toDto() }
    }

    override suspend fun getCachedTrainingById(id: String): TrainingDto? {
        return trainingDao.getById(id)?.toDto()
    }

    override suspend fun isTrainingCacheStale(ttlMillis: Long): Boolean {
        val latestCacheTimestamp = trainingDao.getLatestCacheTimestamp() ?: return true
        val ageMillis = System.currentTimeMillis() - latestCacheTimestamp
        return ageMillis > ttlMillis
    }

    override suspend fun refreshTrainingsIfStale(
        ttlMillis: Long,
        force: Boolean,
        page: Int,
        limit: Int
    ): ApiResponse<List<TrainingDto>> {
        if (!force && !isTrainingCacheStale(ttlMillis)) {
            val cached = getCachedTrainings()
            return ApiResponse(
                success = true,
                msg = "Loaded from cache",
                data = cached
            )
        }

        val response = trainingApiService.getAllTrainings(page, limit)
        if (response.success) {
            val now = System.currentTimeMillis()
            val entities = response.data.orEmpty().map { it.toEntity(now) }
            trainingDao.clearAll()
            if (entities.isNotEmpty()) {
                trainingDao.upsertAll(entities)
            }
        }
        return response
    }

    override suspend fun refreshTrainingById(id: String): ApiResponse<TrainingDto> {
        val response = trainingApiService.getTrainingById(id)
        if (response.success) {
            response.data?.let {
                trainingDao.upsert(it.toEntity(System.currentTimeMillis()))
            }
        }
        return response
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

    private fun TrainingEntity.toDto(): TrainingDto {
        return TrainingDto(
            id = id,
            title = title,
            summary = summary,
            coverImage = coverImage,
            modules = modules,
            organization = organization,
            similar = similar,
            createdBy = createdBy,
            createdAt = createdAt,
            updatedAt = updatedAt,
            version = version
        )
    }

    private fun TrainingDto.toEntity(cachedAtEpochMillis: Long): TrainingEntity {
        return TrainingEntity(
            id = id,
            title = title,
            summary = summary,
            coverImage = coverImage,
            modules = modules,
            organization = organization,
            similar = similar,
            createdBy = createdBy,
            createdAt = createdAt,
            updatedAt = updatedAt,
            version = version,
            cachedAtEpochMillis = cachedAtEpochMillis
        )
    }
}
