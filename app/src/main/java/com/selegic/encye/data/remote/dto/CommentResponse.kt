package com.selegic.encye.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommentResponse(
    val success: Boolean,
    val message: String,
    val comments: List<CommentDto>,
    val hasMore: Boolean,
    val currentPage: Int,
    val totalComments: Int
)
