package com.selegic.encye.data.remote

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.EnrollUserRequest
import com.selegic.encye.data.remote.dto.UpdateProgressRequest
import com.selegic.encye.data.remote.dto.UserEnrollmentDto
import com.selegic.encye.data.remote.dto.UserProgressDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface EnrollProgressApiService {

    @POST("/api/v1/enroll-progress/enroll")
    suspend fun enrollUser(
        @Body request: EnrollUserRequest
    ): ApiResponse<UserEnrollmentDto>

    @GET("/api/v1/enroll-progress/enrollments")
    suspend fun getEnrollments(): ApiResponse<List<UserProgressDto>>

    @GET("/api/v1/enroll-progress/allenrollments")
    suspend fun getAllEnrollments(): ApiResponse<List<UserProgressDto>>

    @PATCH("/api/v1/enroll-progress/updateProgress")
    suspend fun updateProgress(
        @Body request: UpdateProgressRequest
    ): ApiResponse<UserProgressDto>
}
