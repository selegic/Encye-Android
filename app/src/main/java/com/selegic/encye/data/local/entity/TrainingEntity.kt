package com.selegic.encye.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.selegic.encye.data.remote.dto.ImageDto
import com.selegic.encye.data.remote.dto.TrainingModuleDto
import com.selegic.encye.data.remote.dto.UserDto

@Entity(tableName = "trainings")
data class TrainingEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val summary: String? = null,
    val coverImage: ImageDto? = null,
    val modules: List<TrainingModuleDto> = emptyList(),
    val organization: String? = null,
    val similar: List<String> = emptyList(),
    val createdBy: UserDto? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val version: Int? = null,
    val cachedAtEpochMillis: Long
)
