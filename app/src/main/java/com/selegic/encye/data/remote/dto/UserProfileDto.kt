package com.selegic.encye.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class UserProfileResponseDto(
    val success: Boolean,
    val message: String,
    val data: UserProfileDataDto
)

@Serializable
data class UserProfileDataDto(
    val userDetails: UserDto,
    val isFollowing: Boolean = false,
    val articles: List<UserProfileArticleDto> = emptyList(),
    val posts: List<UserProfilePostDto> = emptyList()
)

@Serializable
data class UserProfileArticleDto(
    @SerialName("_id")
    val articleId: String,
    val id: String,
    val title: String,
    val description: String,
    val createdBy: String? = null,
    val createdAt: String,
    val image: JsonElement? = null
)

@Serializable
data class UserProfilePostDto(
    @SerialName("_id")
    val id: String,
    val content: String,
    val createdBy: String? = null,
    val createdAt: String,
    val image: List<JsonElement?> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val autoTags: List<String> = emptyList()
)
