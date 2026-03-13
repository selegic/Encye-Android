package com.selegic.encye.data.repository.fake

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.PostDto
import com.selegic.encye.data.repository.PostRepository
import java.io.File
import javax.inject.Inject

class FakePostRepository @Inject constructor() : PostRepository {
    override suspend fun createPost(
        title: String?,
        content: String?,
        communityId: String?,
        mentions: List<String>?,
        images: List<File>
    ): ApiResponse<PostDto> {
        TODO("Not yet implemented")
    }

    override suspend fun sharePost(id: String, content: String?): ApiResponse<PostDto> {
        TODO("Not yet implemented")
    }

    override suspend fun deletePost(id: String): ApiResponse<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun updatePost(
        id: String,
        title: String?,
        content: String?,
        mentions: List<String>?,
        images: List<File>
    ): ApiResponse<PostDto> {
        TODO("Not yet implemented")
    }

    override suspend fun getPostById(id: String): ApiResponse<PostDto> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllPosts(page: Int, limit: Int): ApiResponse<List<PostDto>> {
        TODO("Not yet implemented")
    }

    override suspend fun getOrganizationPosts(page: Int, limit: Int): ApiResponse<List<PostDto>> {
        TODO("Not yet implemented")
    }

    override suspend fun getPosts(
        page: Int,
        limit: Int
    ): List<PostDto> {
        TODO("Not yet implemented")
    }
}
