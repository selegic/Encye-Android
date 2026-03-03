package com.selegic.encye.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.selegic.encye.data.local.AppDatabase
import com.selegic.encye.data.paging.ArticleRemoteMediator
import com.selegic.encye.data.remote.ArticleApiService
import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.ArticleDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class ArticleRepositoryImpl @Inject constructor(
    private val apiService: ArticleApiService,
    private val database: AppDatabase
) : ArticleRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getArticles(): Flow<PagingData<ArticleDto>> {
        val pagingSourceFactory = { database.articleDao.pagingSource() }

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            remoteMediator = ArticleRemoteMediator(
                apiService = apiService,
                database = database
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                ArticleDto(
                    id = entity.id,
                    _id = entity.monogoId,
                    title = entity.title,
                    description = entity.description,
                    image = entity.image,
                    autoCategory = entity.autoCategory,
                    createdBy = entity.createdBy,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override suspend fun createArticle(
        title: String,
        description: String,
        image: File?,
        subtitle: String?,
        category: String?,
        section: String?,
        tags: List<String>?,
        isPublished: Boolean?
    ): ApiResponse<ArticleDto> {
        val imagePart = image?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", it.name, requestFile)
        }

        return apiService.createArticle(
            title = title.toRequestBody(),
            description = description.toRequestBody(),
            image = imagePart,
            subtitle = subtitle?.toRequestBody(),
            category = category?.toRequestBody(),
            section = section?.toRequestBody(),
            tags = tags?.joinToString(",")?.toRequestBody(),
            isPublished = isPublished?.toString()?.toRequestBody()
        )
    }

    override suspend fun searchArticles(
        query: String?,
        category: String?,
        section: String?,
        page: Int,
        limit: Int
    ): ApiResponse<List<ArticleDto>> {
        return apiService.searchArticles(query, category, section, page, limit)
    }

    override suspend fun getAllArticles(page: Int, limit: Int): ApiResponse<List<ArticleDto>> {
        return apiService.getAllArticles(page, limit)
    }

    override suspend fun getOrganizationArticles(page: Int, limit: Int): ApiResponse<List<ArticleDto>> {
        return apiService.getOrganizationArticles(page, limit)
    }

    override suspend fun getArticleById(id: String): ApiResponse<ArticleDto> {
        return apiService.getArticleById(id)
    }

    override suspend fun updateArticle(
        id: String,
        title: String?,
        description: String?,
        image: File?,
        subtitle: String?,
        category: String?,
        section: String?,
        tags: List<String>?,
        isPublished: Boolean?
    ): ApiResponse<ArticleDto> {
        val imagePart = image?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", it.name, requestFile)
        }

        return apiService.updateArticle(
            id = id,
            title = title?.toRequestBody(),
            description = description?.toRequestBody(),
            image = imagePart,
            subtitle = subtitle?.toRequestBody(),
            category = category?.toRequestBody(),
            section = section?.toRequestBody(),
            tags = tags?.joinToString(",")?.toRequestBody(),
            isPublished = isPublished?.toString()?.toRequestBody()
        )
    }

    override suspend fun deleteArticle(id: String): ApiResponse<Unit> {
        return apiService.deleteArticle(id)
    }

    override suspend fun migrateArticles(): ApiResponse<Unit> {
        return apiService.migrateArticles()
    }

    private fun String.toRequestBody(): RequestBody {
        return this.toRequestBody("text/plain".toMediaTypeOrNull())
    }
}
