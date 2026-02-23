package com.selegic.encye.data.remote.dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    @SerialName("_id") val id: String,
    val content: String,
    val createdBy: UserDto,
    val createdAt: String,
    val likeCount: Int = 0
)