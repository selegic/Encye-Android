package com.selegic.encye.data.remote

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.TrainingDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface TrainingApiService {

    @Multipart
    @POST("/api/v1/training/create")
    suspend fun createTraining(
        @Part("title") title: RequestBody,
        @Part coverImage: MultipartBody.Part? = null,
        @Part("summary") summary: RequestBody? = null,
        @Part("module") module: RequestBody? = null,
        @Part("organization") organization: RequestBody? = null,
        @Part("similar") similar: RequestBody? = null
    ): ApiResponse<TrainingDto>

    @GET("/api/v1/training/all")
    suspend fun getAllTrainings(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 8
    ): ApiResponse<List<TrainingDto>>

    @GET("/api/v1/training/org/all")
    suspend fun getOrganizationTrainings(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 8
    ): ApiResponse<List<TrainingDto>>

    @GET("/api/v1/training/{id}")
    suspend fun getTrainingById(@Path("id") id: String): ApiResponse<TrainingDto>

    @Multipart
    @PUT("/api/v1/training/{id}")
    suspend fun updateTraining(
        @Path("id") id: String,
        @Part("title") title: RequestBody? = null,
        @Part("summary") summary: RequestBody? = null,
        @Part coverImage: MultipartBody.Part? = null,
        @Part("module") module: RequestBody? = null,
        @Part("similar") similar: RequestBody? = null
    ): ApiResponse<TrainingDto>

    @DELETE("/api/v1/training/{id}")
    suspend fun deleteTraining(@Path("id") id: String): ApiResponse<Unit>
}
