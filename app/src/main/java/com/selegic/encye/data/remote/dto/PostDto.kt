package com.selegic.encye.data.remote.dto

import com.selegic.encye.data.remote.dto.ImageDto
import com.selegic.encye.data.remote.dto.UserDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    @SerialName("_id")
    val id: String,
    val content: String,
    val image: List<ImageDto>? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val createdBy: UserDto,
    val createdAt: String,
    val updatedAt: String
)
