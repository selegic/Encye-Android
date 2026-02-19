package com.selegic.encye.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val msg: String,
    val data: T? = null,
    val totalCount: Int? = null,
    val currentPage: Int? = null,
    val hasMore: Boolean? = null
)
