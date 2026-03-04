package com.selegic.encye.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImageDto(
    @SerialName("_id")
    val id: String,
    val publicId: String? = null,
    @SerialName("id")
    val externalId: String? = null,
    val idWithExt: String? = null,
    val url: String? = null,
    val name: String? = null,
    val filePath: String? = null,
    val type: String? = null,
    val category: String? = null,
    val organization: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val order: Int? = null,
    @SerialName("__v")
    val version: Int? = null
)
