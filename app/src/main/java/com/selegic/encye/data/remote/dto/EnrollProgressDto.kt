package com.selegic.encye.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class EnrollUserRequest(
    val trainingId: String
)

@Serializable
data class UpdateProgressRequest(
    val trainingId: String,
    val moduleId: String,
    val moduleStatus: String
)

@Serializable
data class UserEnrollmentDto(
    @SerialName("_id")
    val id: String,
    val userId: String,
    val trainingId: String,
    val enrollmentDate: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class ModuleProgressDto(
    val module: String,
    val moduleStatus: String,
    val completedAt: String? = null
)

@Serializable
data class UserProgressDto(
    @SerialName("_id")
    val id: String,
    val userId: String,
    val trainingId: JsonElement? = null,
    val moduleProgress: List<ModuleProgressDto> = emptyList(),
    val trainingStatus: String,
    val completedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
