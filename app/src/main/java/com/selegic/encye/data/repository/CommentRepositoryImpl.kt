package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.CommentApiService
import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.CommentDto
import com.selegic.encye.data.remote.dto.CommentRequest
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val commentApiService: CommentApiService
) : CommentRepository {

    override suspend fun createComment(
        onModel: String,
        commentedOnId: String,
        text: String
    ): ApiResponse<CommentDto> {
        return commentApiService.createComment(onModel, commentedOnId, CommentRequest(text))
    }

    override suspend fun replyToComment(
        commentId: String,
        text: String
    ): ApiResponse<CommentDto> {
        return commentApiService.replyToComment(commentId, CommentRequest(text))
    }

    override suspend fun getReplies(
        commentId: String,
        page: Int,
        limit: Int
    ): ApiResponse<List<CommentDto>> {
        return commentApiService.getReplies(commentId, page, limit)
    }

    override suspend fun getComments(
        onModel: String,
        itemId: String,
        page: Int,
        limit: Int
    ): ApiResponse<List<CommentDto>> {
        return commentApiService.getComments(onModel, itemId, page, limit)
    }

    override suspend fun deleteComment(commentId: String): ApiResponse<Unit> {
        return commentApiService.deleteComment(commentId)
    }

    override suspend fun updateComment(commentId: String, text: String): ApiResponse<CommentDto> {
        return commentApiService.updateComment(commentId, CommentRequest(text))
    }
}
