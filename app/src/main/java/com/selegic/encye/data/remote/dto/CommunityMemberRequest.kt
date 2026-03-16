package com.selegic.encye.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommunityMemberRequest(
    val communityId: String,
    val join: Boolean? = null,
    val leave: Boolean? = null
)
