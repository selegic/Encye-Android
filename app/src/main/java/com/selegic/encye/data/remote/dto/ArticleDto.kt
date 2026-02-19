package com.selegic.encye.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArticleDto(
    val _id: String,
    val id: String,
    val title: String,
    val description: String,
    val image: ImageDto? = null,
    val autoCategory: AutoCategoryDto? = null,
    val createdBy: UserDto? = null,
    val createdAt: String,
)
