package com.selegic.encye.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class TrainingDto(
    @SerialName("_id")
    val id: String,
    val title: String,
    val summary: String? = null,
    val coverImage: ImageDto? = null,
    @SerialName("module")
    val modules: List<TrainingModuleDto> = emptyList(),
    val organization: String? = null,
    val similar: List<String> = emptyList(),
    val createdBy: UserDto? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerialName("__v")
    val version: Int? = null
)

@Serializable
data class TrainingModuleDto(
    @SerialName("_id")
    val id: String,
    val name: String,
    val moduleNumber: Int? = null,
    val content: String? = null,
    val documents: List<JsonElement> = emptyList(),
    val images: List<JsonElement> = emptyList(),
    val videos: List<JsonElement> = emptyList(),
    val quiz: JsonElement? = null,
    val organization: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerialName("__v")
    val version: Int? = null
)
