package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.dto.ApiResponse
import com.selegic.encye.data.remote.dto.ImageDto
import java.io.File

interface ImageRepository {
    suspend fun uploadImage(
        image: File,
        category: String
    ): ApiResponse<ImageDto>
}
