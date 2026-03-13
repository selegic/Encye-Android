package com.selegic.encye.data.remote

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.PostDto
import com.selegic.encye.data.remote.dto.SharePostRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PostApiService {

    @Multipart
    @POST("api/v1/post/create")
    suspend fun createPost(
        @Part("title") title: RequestBody? = null,
        @Part("content") content: RequestBody? = null,
        @Part("community") community: RequestBody? = null,
        @Part("mentions") mentions: List<@JvmSuppressWildcards RequestBody>? = null,
        @Part image: List<@JvmSuppressWildcards MultipartBody.Part> = emptyList()
    ): ApiResponse<PostDto>

    @POST("api/v1/post/share/{id}")
    suspend fun sharePost(
        @Path("id") id: String,
        @Body request: SharePostRequest
    ): ApiResponse<PostDto>

    @DELETE("api/v1/post/{id}")
    suspend fun deletePost(@Path("id") id: String): ApiResponse<Unit>

    @Multipart
    @PATCH("api/v1/post/{id}")
    suspend fun updatePost(
        @Path("id") id: String,
        @Part("title") title: RequestBody? = null,
        @Part("content") content: RequestBody? = null,
        @Part("mentions") mentions: List<@JvmSuppressWildcards RequestBody>? = null,
        @Part image: List<@JvmSuppressWildcards MultipartBody.Part> = emptyList()
    ): ApiResponse<PostDto>

    @GET("api/v1/post/{id}")
    suspend fun getPostById(@Path("id") id: String): ApiResponse<PostDto>

    @GET("api/v1/post/all")
    suspend fun getAllPosts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<PostDto>>

    @GET("api/v1/post/org/all")
    suspend fun getOrganizationPosts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<PostDto>>
}
