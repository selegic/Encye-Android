package com.selegic.encye.data.remote

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.ImageDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImageApiService {

    @Multipart
    @POST("/api/v1/image/upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("category") category: RequestBody
    ): ApiResponse<ImageDto>
}
