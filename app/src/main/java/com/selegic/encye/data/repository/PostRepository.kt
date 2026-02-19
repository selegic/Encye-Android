package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.dto.PostDto

interface PostRepository {
    suspend fun getPosts(page: Int, limit: Int): List<PostDto>
}
