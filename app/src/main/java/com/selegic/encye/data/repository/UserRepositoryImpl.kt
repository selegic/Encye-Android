package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.UserApiService
import com.selegic.encye.data.remote.dto.GoogleAuthResponseDto
import com.selegic.encye.data.remote.dto.MobileAuthRequestDto
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: UserApiService
) : UserRepository {

    override suspend fun googleAuthCallback(code: String): GoogleAuthResponseDto {
        return apiService.googleAuthCallback(code)
    }

    override suspend fun mobileAuth(request: MobileAuthRequestDto): GoogleAuthResponseDto {
        return apiService.mobileAuth(request)
    }
}
