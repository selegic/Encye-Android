package com.selegic.encye.data.local.converter

import androidx.room.TypeConverter
import com.selegic.encye.data.remote.dto.AutoCategoryDto
import com.selegic.encye.data.remote.dto.ImageDto
import com.selegic.encye.data.remote.dto.UserDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        return value?.let { Json.decodeFromString(it) } ?: emptyList()
    }

    @TypeConverter
    fun fromImageDto(value: ImageDto?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toImageDto(value: String?): ImageDto? {
        return value?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromAutoCategoryDto(value: AutoCategoryDto?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toAutoCategoryDto(value: String?): AutoCategoryDto? {
        return value?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromUserDto(value: UserDto?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toUserDto(value: String?): UserDto? {
        return value?.let { Json.decodeFromString(it) }
    }
}
