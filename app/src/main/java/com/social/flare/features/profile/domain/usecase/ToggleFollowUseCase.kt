package com.social.flare.features.profile.domain.usecase

import com.social.flare.features.profile.domain.repository.FollowRepository

class ToggleFollowUseCase(
    private val followRepository: FollowRepository
) {
    suspend operator fun invoke(followerId: String, followedId: String, isCurrentlyFollowing: Boolean) {
        if (followerId == followedId) return
        if (isCurrentlyFollowing) {
            followRepository.unfollowUser(followerId = followerId, followedId = followedId)
        } else {
            followRepository.followUser(followerId = followerId, followedId = followedId)
        }
    }
}