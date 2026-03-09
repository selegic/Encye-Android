package com.selegic.encye.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LikeToggleResponse(
    val success: Boolean,
    val liked: Boolean,
    val message: String
)
