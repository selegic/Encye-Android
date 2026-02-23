package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.CommentDto
import com.selegic.encye.data.remote.dto.CommentRequest

interface CommentRepository {
    
    suspend fun createComment(
        onModel: String,
        commentedOnId: String,
        text: String
    ): ApiResponse<CommentDto>

    suspend fun replyToComment(
        commentId: String,
        text: String
    ): ApiResponse<CommentDto>

    suspend fun getReplies(
        commentId: String,
        page: Int = 1,
        limit: Int = 5
    ): ApiResponse<List<CommentDto>>

    suspend fun getComments(
        onModel: String,
        itemId: String,
        page: Int = 1,
        limit: Int = 10
    ): ApiResponse<List<CommentDto>>

    suspend fun deleteComment(
        commentId: String
    ): ApiResponse<Unit>

    suspend fun updateComment(
        commentId: String,
        text: String
    ): ApiResponse<CommentDto>
}
