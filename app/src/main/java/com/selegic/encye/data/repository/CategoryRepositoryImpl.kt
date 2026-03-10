package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.CategoryApiService
import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.CategoryTreeDto
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryApiService: CategoryApiService
) : CategoryRepository {

    override suspend fun getAllCategories(): ApiResponse<List<CategoryTreeDto>> {
        return categoryApiService.getAllCategories()
    }
}
