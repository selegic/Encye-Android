package com.selegic.encye.data

import com.selegic.encye.data.remote.dto.PostDto
import com.selegic.encye.data.remote.dto.UserDto

object FakeData {
    fun getPosts(): List<PostDto> {
        return listOf(
            PostDto(
                id = "1",
                content = "This is a sample post content.",
                createdBy = UserDto(
                    id = "1",
                    firstName = "John",
                    lastName = "Doe",
                    profilePicture = "https://randomuser.me/api/portraits/men/1.jpg"
                ),
                createdAt = "2024-01-01T12:00:00Z",
                updatedAt = "2024-01-01T12:00:00Z"
            )
        )
    }
}
