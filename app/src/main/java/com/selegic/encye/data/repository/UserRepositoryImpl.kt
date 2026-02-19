package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.UserApiService
import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.GoogleAuthDataDto
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: UserApiService
) : UserRepository {

    override suspend fun googleAuthCallback(code: String): ApiResponse<GoogleAuthDataDto> {
        return apiService.googleAuthCallback(code)
    }
}
