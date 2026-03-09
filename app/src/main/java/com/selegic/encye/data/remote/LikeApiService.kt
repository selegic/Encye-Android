package com.selegic.encye.data.remote

import com.selegic.encye.data.remote.dto.LikeToggleResponse
import retrofit2.http.POST
import retrofit2.http.Path

interface LikeApiService {

    @POST("/api/v1/like/{onModel}/{itemId}")
    suspend fun toggleLike(
        @Path("onModel") onModel: String,
        @Path("itemId") itemId: String
    ): LikeToggleResponse
}
