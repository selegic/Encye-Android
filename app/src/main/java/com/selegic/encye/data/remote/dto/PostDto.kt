package com.selegic.encye.data.remote.dto

import com.selegic.encye.data.remote.dto.ImageDto
import com.selegic.encye.data.remote.dto.UserDto
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class PostDto(
    @SerialName("_id")
    val id: String,
    val content: String,
    val image: List<ImageDto>? = null,
    val likeCount: Int = 0,
    @JsonNames("isliked", "liked")
    val isLiked: Boolean = false,
    val commentCount: Int = 0,
    val createdBy: UserDto,
    val createdAt: String,
    val autoCategory: AutoCategoryDto? = null,
    val autoTags: List<String> = emptyList(), // Added for the text-first design
    val updatedAt: String
)
