package com.selegic.encye.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.selegic.encye.data.paging.VideoPagingSource
import com.selegic.encye.data.remote.VideoApiService
import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.VideoDto
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val videoApiService: VideoApiService
) : VideoRepository {

    override fun getVideoFeed(
        category: String?,
        pageSize: Int,
        prefetchDistance: Int
    ): Flow<PagingData<VideoDto>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize,
                prefetchDistance = prefetchDistance,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                VideoPagingSource(
                    apiService = videoApiService,
                    category = category,
                    limit = pageSize
                )
            }
        ).flow
    }

    override suspend fun getAllVideos(page: Int, limit: Int): ApiResponse<List<VideoDto>> {
        return videoApiService.getAllVideos(page = page, limit = limit)
    }

    override suspend fun getVideoById(id: String): ApiResponse<VideoDto> {
        return videoApiService.getVideoById(id)
    }

    override suspend fun uploadVideoChunk(
        file: File,
        originalName: String,
        category: String,
        chunkNumber: Int,
        totalChunks: Int,
        title: String?,
        description: String?
    ): ApiResponse<VideoDto> {
        return videoApiService.uploadVideoChunk(
            file = file.toVideoPart(),
            originalName = originalName.toPlainTextRequestBody(),
            category = category.toPlainTextRequestBody(),
            chunkNumber = chunkNumber.toString().toPlainTextRequestBody(),
            totalChunks = totalChunks.toString().toPlainTextRequestBody(),
            title = title?.toPlainTextRequestBody(),
            description = description?.toPlainTextRequestBody()
        )
    }

    override suspend fun deleteVideo(id: String): ApiResponse<Unit> {
        return videoApiService.deleteVideo(id)
    }

    private fun String.toPlainTextRequestBody(): RequestBody {
        return toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun File.toVideoPart(): MultipartBody.Part {
        val requestFile = asRequestBody("video/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", name, requestFile)
    }
}
