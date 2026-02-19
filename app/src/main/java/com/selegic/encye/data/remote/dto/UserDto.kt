package com.selegic.encye.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("_id")
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePicture: String? = null
)
