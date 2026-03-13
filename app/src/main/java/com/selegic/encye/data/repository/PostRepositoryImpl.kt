package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.PostApiService
import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.PostDto
import com.selegic.encye.data.remote.dto.SharePostRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val postApiService: PostApiService
) : PostRepository {

    override suspend fun createPost(
        title: String?,
        content: String?,
        communityId: String?,
        mentions: List<String>?,
        images: List<File>
    ): ApiResponse<PostDto> {
        return postApiService.createPost(
            title = title?.toPlainTextRequestBody(),
            content = content?.toPlainTextRequestBody(),
            community = communityId?.toPlainTextRequestBody(),
            mentions = mentions?.map { it.toPlainTextRequestBody() },
            image = images.map { it.toImagePart() }
        )
    }

    override suspend fun sharePost(id: String, content: String?): ApiResponse<PostDto> {
        return postApiService.sharePost(id, SharePostRequest(content = content))
    }

    override suspend fun deletePost(id: String): ApiResponse<Unit> {
        return postApiService.deletePost(id)
    }

    override suspend fun updatePost(
        id: String,
        title: String?,
        content: String?,
        mentions: List<String>?,
        images: List<File>
    ): ApiResponse<PostDto> {
        return postApiService.updatePost(
            id = id,
            title = title?.toPlainTextRequestBody(),
            content = content?.toPlainTextRequestBody(),
            mentions = mentions?.map { it.toPlainTextRequestBody() },
            image = images.map { it.toImagePart() }
        )
    }

    override suspend fun getPostById(id: String): ApiResponse<PostDto> {
        return postApiService.getPostById(id)
    }

    override suspend fun getAllPosts(page: Int, limit: Int): ApiResponse<List<PostDto>> {
        return postApiService.getAllPosts(page, limit)
    }

    override suspend fun getOrganizationPosts(page: Int, limit: Int): ApiResponse<List<PostDto>> {
        return postApiService.getOrganizationPosts(page, limit)
    }

    override suspend fun getPosts(page: Int, limit: Int): List<PostDto> {
        return getOrganizationPosts(page, limit).data.orEmpty()
    }

    private fun String.toPlainTextRequestBody(): RequestBody {
        return toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun File.toImagePart(): MultipartBody.Part {
        val requestFile = asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image", name, requestFile)
    }
}
