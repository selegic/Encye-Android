package com.selegic.encye.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommunityModeratorRequest(
    val communityId: String,
    val moderators: String,
    val action: String
)
