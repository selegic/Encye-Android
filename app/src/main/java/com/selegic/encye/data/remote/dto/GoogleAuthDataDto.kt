package com.selegic.encye.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthDataDto(
    val token: String,
    val userId: String,
    val firstName: String,
    val lastName: String,
    val new: Boolean
)
