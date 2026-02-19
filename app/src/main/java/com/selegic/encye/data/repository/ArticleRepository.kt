package com.selegic.encye.data.repository

import androidx.paging.PagingData
import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.ArticleDto
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ArticleRepository {

    fun getArticles(): Flow<PagingData<ArticleDto>>

    suspend fun createArticle(
        title: String,
        description: String,
        image: File? = null,
        subtitle: String? = null,
        category: String? = null,
        section: String? = null,
        tags: List<String>? = null,
        isPublished: Boolean? = null
    ): ApiResponse<ArticleDto>

    suspend fun searchArticles(
        query: String? = null,
        category: String? = null,
        section: String? = null,
        page: Int = 1,
        limit: Int = 10
    ): ApiResponse<List<ArticleDto>>

    suspend fun getAllArticles(page: Int = 1, limit: Int = 10): ApiResponse<List<ArticleDto>>

    suspend fun getOrganizationArticles(page: Int = 1, limit: Int = 10): ApiResponse<List<ArticleDto>>

    suspend fun getArticleById(id: String): ApiResponse<ArticleDto>

    suspend fun updateArticle(
        id: String,
        title: String? = null,
        description: String? = null,
        image: File? = null,
        subtitle: String? = null,
        category: String? = null,
        section: String? = null,
        tags: List<String>? = null,
        isPublished: Boolean? = null
    ): ApiResponse<ArticleDto>

    suspend fun deleteArticle(id: String): ApiResponse<Unit>

    suspend fun migrateArticles(): ApiResponse<Unit>
}
