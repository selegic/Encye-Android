package com.selegic.encye.data.remote

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.ArticleDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ArticleApiService {

    @Multipart
    @POST("/api/v1/article/create")
    suspend fun createArticle(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part? = null,
        @Part("subtitle") subtitle: RequestBody? = null,
        @Part("category") category: RequestBody? = null,
        @Part("section") section: RequestBody? = null,
        @Part("tags") tags: RequestBody? = null,
        @Part("isPublished") isPublished: RequestBody? = null,
    ): ApiResponse<ArticleDto>

    @GET("/api/v1/article/search")
    suspend fun searchArticles(
        @Query("query") query: String? = null,
        @Query("category") category: String? = null,
        @Query("section") section: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<ArticleDto>>

    @GET("/api/v1/article/all")
    suspend fun getAllArticles(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<ArticleDto>>

    @GET("/api/v1/article/org/all")
    suspend fun getOrganizationArticles(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<ArticleDto>>

    @GET("/api/v1/article/{id}")
    suspend fun getArticleById(@Path("id") id: String): ApiResponse<ArticleDto>

    @Multipart
    @PUT("/api/v1/article/{id}")
    suspend fun updateArticle(
        @Path("id") id: String,
        @Part("title") title: RequestBody? = null,
        @Part("description") description: RequestBody? = null,
        @Part image: MultipartBody.Part? = null,
        @Part("subtitle") subtitle: RequestBody? = null,
        @Part("category") category: RequestBody? = null,
        @Part("section") section: RequestBody? = null,
        @Part("tags") tags: RequestBody? = null,
        @Part("isPublished") isPublished: RequestBody? = null,
    ): ApiResponse<ArticleDto>

    @DELETE("/api/v1/article/{id}")
    suspend fun deleteArticle(@Path("id") id: String): ApiResponse<Unit>

    @GET("/api/v1/article/migrate")
    suspend fun migrateArticles(): ApiResponse<Unit>
}
