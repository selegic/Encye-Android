package com.selegic.encye.data.remote

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.CategoryTreeDto
import retrofit2.http.GET

interface CategoryApiService {

    @GET("/api/v1/category/all")
    suspend fun getAllCategories(): ApiResponse<List<CategoryTreeDto>>
}
