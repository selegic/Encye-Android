package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.PostApiService
import com.selegic.encye.data.remote.dto.PostDto
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val postApiService: PostApiService
) : PostRepository {

    override suspend fun getPosts(page: Int, limit: Int): List<PostDto> {
        return postApiService.getPosts(page, limit).data ?: listOf()
    }

}
