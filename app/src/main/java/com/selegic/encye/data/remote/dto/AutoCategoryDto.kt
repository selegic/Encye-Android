package com.selegic.encye.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AutoCategoryDto(
    val primary: CategoryDto
)
