package com.selegic.encye.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    @SerialName("_id") val id: String,
    @SerialName("text") val content: String,
    @SerialName("user") val createdBy: UserDto,
    val createdAt: String,
    val likeCount: Int = 0,
    val isLiked: Boolean = false
)
