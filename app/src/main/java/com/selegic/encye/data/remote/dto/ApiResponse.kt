package com.selegic.encye.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    @SerialName("msg")
    @JsonNames("message")
    val msg: String = "",
    val data: T? = null,
    val totalCount: Int? = null,
    val totalTraining: Int? = null,
    val totalVideos: Int? = null,
    val currentPage: Int? = null,
    val hasMore: Boolean? = null
)
