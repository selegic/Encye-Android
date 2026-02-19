package com.selegic.encye.data.remote

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.GoogleAuthDataDto
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApiService {

    @GET("/api/v1/user/auth/google/callback")
    suspend fun googleAuthCallback(@Query("code") code: String): ApiResponse<GoogleAuthDataDto>
}
