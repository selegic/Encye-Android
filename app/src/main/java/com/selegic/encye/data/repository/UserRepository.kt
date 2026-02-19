package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.GoogleAuthDataDto

interface UserRepository {

    suspend fun googleAuthCallback(code: String): ApiResponse<GoogleAuthDataDto>
}
