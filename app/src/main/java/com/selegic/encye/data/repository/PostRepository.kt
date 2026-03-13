package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.PostDto
import java.io.File

interface PostRepository {
    suspend fun createPost(
        title: String? = null,
        content: String? = null,
        communityId: String? = null,
        mentions: List<String>? = null,
        images: List<File> = emptyList()
    ): ApiResponse<PostDto>

    suspend fun sharePost(id: String, content: String? = null): ApiResponse<PostDto>

    suspend fun deletePost(id: String): ApiResponse<Unit>

    suspend fun updatePost(
        id: String,
        title: String? = null,
        content: String? = null,
        mentions: List<String>? = null,
        images: List<File> = emptyList()
    ): ApiResponse<PostDto>

    suspend fun getPostById(id: String): ApiResponse<PostDto>

    suspend fun getAllPosts(page: Int = 1, limit: Int = 10): ApiResponse<List<PostDto>>

    suspend fun getOrganizationPosts(page: Int = 1, limit: Int = 10): ApiResponse<List<PostDto>>

    suspend fun getPosts(page: Int, limit: Int): List<PostDto>
}
