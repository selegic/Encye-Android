package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.CategoryTreeDto

interface CategoryRepository {
    suspend fun getAllCategories(): ApiResponse<List<CategoryTreeDto>>
}
