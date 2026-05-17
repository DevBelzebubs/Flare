package com.social.flare.features.profile.domain.usecase

import com.social.flare.features.profile.domain.model.FollowStats
import com.social.flare.features.profile.domain.repository.FollowRepository
import kotlinx.coroutines.flow.Flow

class GetFollowStatsUseCase(
    private val followRepository: FollowRepository
) {
    operator fun invoke(targetUserId: String, currentUserId: String): Flow<FollowStats> {
        return followRepository.getFollowStats(
            targetUserId = targetUserId,
            currentUserId = currentUserId
        )
    }
}