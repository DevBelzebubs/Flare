package com.social.flare.features.profile.data.repository

import com.social.flare.features.profile.data.local.dao.FollowDao
import com.social.flare.features.profile.data.local.entity.FollowEntity
import com.social.flare.features.profile.domain.model.FollowStats
import com.social.flare.features.profile.domain.repository.FollowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class FollowRepositoryImpl(
    private val followDao: FollowDao
) : FollowRepository {

    override suspend fun followUser(followerId: String, followedId: String) {
        followDao.insertFollow(FollowEntity(followerId = followerId, followedId = followedId))
    }

    override suspend fun unfollowUser(followerId: String, followedId: String) {
        followDao.deleteFollow(followerId = followerId, followedId = followedId)
    }

    override fun isFollowing(followerId: String, followedId: String): Flow<Boolean> {
        return followDao.isFollowing(followerId = followerId, followedId = followedId)
    }

    override fun getFollowStats(targetUserId: String, currentUserId: String): Flow<FollowStats> {
        val followersFlow = followDao.getFollowersCount(targetUserId)
        val followingFlow = followDao.getFollowingCount(targetUserId)
        val isFollowingFlow = followDao.isFollowing(followerId = currentUserId, followedId = targetUserId)

        return combine(followersFlow, followingFlow, isFollowingFlow) { followers, following, isFollowing ->
            FollowStats(
                followersCount = followers,
                followingCount = following,
                isFollowingByMe = isFollowing
            )
        }
    }
}