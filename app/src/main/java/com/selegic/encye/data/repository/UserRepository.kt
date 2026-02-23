package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.dto.GoogleAuthResponseDto

interface UserRepository {

    suspend fun googleAuthCallback(code: String): GoogleAuthResponseDto
}
