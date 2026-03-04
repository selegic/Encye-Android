package com.selegic.encye.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.selegic.encye.data.remote.dto.AutoCategoryDto
import com.selegic.encye.data.remote.dto.ImageDto
import com.selegic.encye.data.remote.dto.UserDto

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey
    val id: String,
    val monogoId: String,
    val title: String,
    val description: String,
    val image: ImageDto? = null,
    val tags: List<String> = emptyList(),
    val autoCategory: AutoCategoryDto? = null,
    val createdBy: UserDto? = null,
    val createdAt: String
)
