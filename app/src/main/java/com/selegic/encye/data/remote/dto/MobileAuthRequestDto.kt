package com.selegic.encye.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MobileAuthRequestDto(
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePicture: String? = null
)
