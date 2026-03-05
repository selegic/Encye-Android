package com.selegic.encye.data.remote

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.VideoDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface VideoApiService {

    @Multipart
    @POST("/api/v1/video/upload")
    suspend fun uploadVideoChunk(
        @Part file: MultipartBody.Part,
        @Part("originalname") originalName: RequestBody,
        @Part("category") category: RequestBody,
        @Part("chunkNumber") chunkNumber: RequestBody,
        @Part("totalChunks") totalChunks: RequestBody,
        @Part("title") title: RequestBody? = null,
        @Part("description") description: RequestBody? = null
    ): ApiResponse<VideoDto>

    @GET("/api/v1/video/all")
    suspend fun getAllVideos(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 8
    ): ApiResponse<List<VideoDto>>

    @GET("/api/v1/video/{id}")
    suspend fun getVideoById(@Path("id") id: String): ApiResponse<VideoDto>

    @DELETE("/api/v1/video/{id}")
    suspend fun deleteVideo(@Path("id") id: String): ApiResponse<Unit>
}
