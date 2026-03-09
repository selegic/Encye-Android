package com.selegic.encye.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class VideoDto(
    @SerialName("_id")
    val mongoId: String,
    val id: String,
    val name: String? = null,
    val title: String? = null,
    val description: String? = null,
    val idWithExt: String? = null,
    val filePath: String? = null,
    val url: String? = null,
    val category: String? = null,
    val duration: Double? = null,
    val organization: String? = null,
    val uploadedBy: String? = null,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val likeCount: Int = 0,
    @JsonNames("isliked", "liked")
    val isLiked: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val order: Int? = null,
    @SerialName("__v")
    val version: Int? = null
)
