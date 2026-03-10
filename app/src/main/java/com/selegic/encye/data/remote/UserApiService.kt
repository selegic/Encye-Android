package com.selegic.encye.data.remote

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.GoogleAuthResponseDto
import com.selegic.encye.data.remote.dto.MobileAuthRequestDto
import com.selegic.encye.data.remote.dto.UserProfileResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApiService {

    @GET("/api/v1/user/auth/google/callback")
    suspend fun googleAuthCallback(@Query("code") code: String): GoogleAuthResponseDto

    @POST("/api/v1/user/mobile-auth")
    suspend fun mobileAuth(@Body request: MobileAuthRequestDto): GoogleAuthResponseDto

    @GET("/api/v1/user/profile/{id}")
    suspend fun getProfileById(@Path("id") id: String): UserProfileResponseDto
}
