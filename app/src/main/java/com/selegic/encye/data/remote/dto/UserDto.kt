package com.selegic.encye.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class UserDto(
    @SerialName("_id")
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val profilePicture: String? = null,
    val password: String? = null,
    val role: String? = null,
    val organization: String? = null,
    val isActivated: Boolean? = null,
    val lastLoginTime: String? = null,
    val community: List<String> = emptyList(),
    val followersCount: Int? = null,
    val followingCount: Int? = null,
    val preferences: List<JsonElement> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerialName("__v")
    val version: Int? = null
)
