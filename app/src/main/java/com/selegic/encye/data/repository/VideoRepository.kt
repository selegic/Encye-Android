package com.selegic.encye.data.repository

import androidx.paging.PagingData
import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.VideoDto
import kotlinx.coroutines.flow.Flow
import java.io.File

interface VideoRepository {

    fun getVideoFeed(
        category: String? = "short_video",
        pageSize: Int = 8,
        prefetchDistance: Int = 2
    ): Flow<PagingData<VideoDto>>

    suspend fun getAllVideos(page: Int = 1, limit: Int = 8): ApiResponse<List<VideoDto>>

    suspend fun getVideoById(id: String): ApiResponse<VideoDto>

    suspend fun uploadVideoChunk(
        file: File,
        originalName: String,
        category: String,
        chunkNumber: Int,
        totalChunks: Int,
        title: String? = null,
        description: String? = null
    ): ApiResponse<VideoDto>

    suspend fun deleteVideo(id: String): ApiResponse<Unit>
}
