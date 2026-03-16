package com.selegic.encye.data.remote

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.CommunityDto
import com.selegic.encye.data.remote.dto.CommunityMemberRequest
import com.selegic.encye.data.remote.dto.CommunityModeratorRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface CommunityApiService {

    @Multipart
    @POST("api/v1/community/create")
    suspend fun createCommunity(
        @Part("name") name: RequestBody,
        @Part("id") id: RequestBody,
        @Part("organization") organization: RequestBody,
        @Part("about") about: RequestBody? = null,
        @Part profileImage: MultipartBody.Part? = null,
        @Part coverImage: MultipartBody.Part? = null
    ): ApiResponse<CommunityDto>

    @GET("api/v1/community/all")
    suspend fun getAllCommunities(): ApiResponse<List<CommunityDto>>

    @GET("api/v1/community/org/all")
    suspend fun getOrganizationCommunities(): ApiResponse<List<CommunityDto>>

    @PATCH("api/v1/community/post/request/{communityId}/{status}/{postId}")
    suspend fun updateCommunityPostStatus(
        @Path("communityId") communityId: String,
        @Path("status") status: String,
        @Path("postId") postId: String
    ): ApiResponse<CommunityDto>

    @GET("api/v1/community/{id}")
    suspend fun getCommunityById(@Path("id") id: String): ApiResponse<CommunityDto>

    @Multipart
    @PATCH("api/v1/community/{id}")
    suspend fun updateCommunity(
        @Path("id") id: String,
        @Part("name") name: RequestBody? = null,
        @Part("id") communityId: RequestBody? = null,
        @Part("organization") organization: RequestBody? = null,
        @Part("about") about: RequestBody? = null,
        @Part profileImage: MultipartBody.Part? = null,
        @Part coverImage: MultipartBody.Part? = null
    ): ApiResponse<CommunityDto>

    @DELETE("api/v1/community/{id}")
    suspend fun deleteCommunity(@Path("id") id: String): ApiResponse<Unit>

    @PUT("api/v1/community/moderator")
    suspend fun updateCommunityModerators(
        @Body request: CommunityModeratorRequest
    ): ApiResponse<CommunityDto>

    @PUT("api/v1/community/member")
    suspend fun updateCommunityMembership(
        @Body request: CommunityMemberRequest
    ): ApiResponse<CommunityDto>
}
