package com.selegic.encye.data.repository.fake

import com.selegic.encye.data.remote.dto.PostDto
import com.selegic.encye.data.repository.PostRepository
import javax.inject.Inject

class FakePostRepository @Inject constructor() : PostRepository {
    override suspend fun getPosts(
        page: Int,
        limit: Int
    ): List<PostDto> {
        TODO("Not yet implemented")
    }
}
