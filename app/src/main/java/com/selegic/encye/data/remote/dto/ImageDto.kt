package com.selegic.encye.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImageDto(
    @SerialName("_id")
    val id: String,
    val url: String? = null,
    val name: String? = null,
    val filePath: String? = null
)
