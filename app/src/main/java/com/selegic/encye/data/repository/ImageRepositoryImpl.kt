package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.ImageApiService
import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.ImageDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val imageApiService: ImageApiService
) : ImageRepository {

    override suspend fun uploadImage(
        image: File,
        category: String
    ): ApiResponse<ImageDto> {
        val imagePart = MultipartBody.Part.createFormData(
            name = "image",
            filename = image.name,
            body = image.asRequestBody("image/*".toMediaTypeOrNull())
        )

        return imageApiService.uploadImage(
            image = imagePart,
            category = category.toRequestBody("text/plain".toMediaTypeOrNull())
        )
    }
}
