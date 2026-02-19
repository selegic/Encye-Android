package com.selegic.encye.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val name: String,
    val slug: String
)
