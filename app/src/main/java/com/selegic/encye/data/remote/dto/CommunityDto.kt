package com.selegic.encye.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityDto(
    @SerialName("_id")
    val mongoId: String,
    val name: String,
    val id: String,
    val admin: String? = null,
    val moderator: List<String> = emptyList(),
    val member: List<String> = emptyList(),
    val profileImage: ImageDto? = null,
    val coverImage: ImageDto? = null,
    val category: List<String> = emptyList(),
    val about: String? = null,
    val organization: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerialName("__v")
    val version: Int? = null,
    val isAdmin: Boolean = false,
    val isModerator: Boolean = false
)
