package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.dto.GoogleAuthResponseDto
import com.selegic.encye.data.remote.dto.MobileAuthRequestDto

interface UserRepository {

    suspend fun googleAuthCallback(code: String): GoogleAuthResponseDto

    suspend fun mobileAuth(request: MobileAuthRequestDto): GoogleAuthResponseDto
}
