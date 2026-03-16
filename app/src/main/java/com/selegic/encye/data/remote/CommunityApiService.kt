package com.selegic.encye.data.remote

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.CommunityDto
import retrofit2.http.GET

interface CommunityApiService {

    @GET("api/v1/community/all")
    suspend fun getAllCommunities(): ApiResponse<List<CommunityDto>>
}
