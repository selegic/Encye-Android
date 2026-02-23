package com.selegic.encye.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthResponseDto(
    val message: String? = null,
    val data: GoogleAuthDataDto? = null
)
