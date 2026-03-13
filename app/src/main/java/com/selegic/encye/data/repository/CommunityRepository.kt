package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.dto.CommunityDto

interface CommunityRepository {
    suspend fun getOrganizationCommunities(): List<CommunityDto>
}
