package com.selegic.encye.data.remote

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.CommentDto
import com.selegic.encye.data.remote.dto.CommentRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommentApiService {

    @POST("/api/v1/comment/create/{onModel}/{commentedOnId}")
    suspend fun createComment(
        @Path("onModel") onModel: String,
        @Path("commentedOnId") commentedOnId: String,
        @Body request: CommentRequest
    ): ApiResponse<CommentDto>

    @POST("/api/v1/comment/reply/{commentId}")
    suspend fun replyToComment(
        @Path("commentId") commentId: String,
        @Body request: CommentRequest
    ): ApiResponse<CommentDto>

    @GET("/api/v1/comment/all/replies/{commentId}")
    suspend fun getReplies(
        @Path("commentId") commentId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 5
    ): ApiResponse<List<CommentDto>>

    @GET("/api/v1/comment/all/{onModel}/{itemId}")
    suspend fun getComments(
        @Path("onModel") onModel: String,
        @Path("itemId") itemId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<CommentDto>>

    @DELETE("/api/v1/comment/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: String
    ): ApiResponse<Unit>

    @PATCH("/api/v1/comment/{commentId}")
    suspend fun updateComment(
        @Path("commentId") commentId: String,
        @Body request: CommentRequest
    ): ApiResponse<CommentDto>
}
