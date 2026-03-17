package com.selegic.encye.data.repository

import com.selegic.encye.data.remote.CommunityApiService
import com.selegic.encye.data.remote.dto.CommunityDto
import com.selegic.encye.data.remote.dto.CommunityMemberRequest
import javax.inject.Inject

class CommunityRepositoryImpl @Inject constructor(
    private val communityApiService: CommunityApiService
) : CommunityRepository {

    override suspend fun getAllCommunities(): List<CommunityDto> {
        return communityApiService.getAllCommunities().data.orEmpty()
    }

    override suspend fun updateCommunityMembership(
        communityId: String,
        join: Boolean,
        leave: Boolean
    ): CommunityDto {
        return communityApiService.updateCommunityMembership(
            CommunityMemberRequest(
                communityId = communityId,
                join = join.takeIf { it },
                leave = leave.takeIf { it }
            )
        ).data ?: throw IllegalStateException("Community membership update returned no data")
    }
}
