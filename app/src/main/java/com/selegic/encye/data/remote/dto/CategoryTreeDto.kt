package com.selegic.encye.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryTreeDto(
    @SerialName("_id")
    val id: String,
    val name: String,
    val slug: String,
    val subcategories: List<CategoryTreeDto> = emptyList()
)
