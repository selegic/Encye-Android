package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.LikeApiService
import com.selegic.encye.data.remote.dto.LikeToggleResponse
import javax.inject.Inject

class LikeRepositoryImpl @Inject constructor(
    private val likeApiService: LikeApiService
) : LikeRepository {

    override suspend fun toggleLike(onModel: String, itemId: String): LikeToggleResponse {
        return likeApiService.toggleLike(onModel, itemId)
    }
}
