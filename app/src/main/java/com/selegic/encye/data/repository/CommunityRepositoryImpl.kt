package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.CommunityApiService
import com.selegic.encye.data.remote.dto.CommunityDto
import javax.inject.Inject

class CommunityRepositoryImpl @Inject constructor(
    private val communityApiService: CommunityApiService
) : CommunityRepository {

    override suspend fun getOrganizationCommunities(): List<CommunityDto> {
        return communityApiService.getOrganizationCommunities().data.orEmpty()
    }
}
