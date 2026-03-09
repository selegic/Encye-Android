package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.dto.LikeToggleResponse

interface LikeRepository {
    suspend fun toggleLike(onModel: String, itemId: String): LikeToggleResponse
}
