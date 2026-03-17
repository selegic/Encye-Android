package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.dto.CommunityDto

interface CommunityRepository {
    suspend fun getAllCommunities(): List<CommunityDto>
    suspend fun updateCommunityMembership(
        communityId: String,
        join: Boolean = false,
        leave: Boolean = false
    ): CommunityDto
}
