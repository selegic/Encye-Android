package com.selegic.encye.data.remote

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.PostDto
import retrofit2.http.GET
import retrofit2.http.Query

interface PostApiService {

    @GET("api/v1/post/all")
    suspend fun getPosts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<PostDto>>

}
