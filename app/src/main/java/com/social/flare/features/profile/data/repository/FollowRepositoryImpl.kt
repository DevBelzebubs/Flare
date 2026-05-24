package com.social.flare.features.profile.data.repository

import com.social.flare.features.profile.data.local.dao.FollowDao
import com.social.flare.features.profile.data.local.entity.FollowEntity
import com.social.flare.features.profile.domain.model.FollowStats
import com.social.flare.features.profile.domain.repository.FollowRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class FollowRepositoryImpl(
    private val followDao: FollowDao,
    private val supabase: SupabaseClient
) : FollowRepository {

    override suspend fun followUser(followerId: String, followedId: String) {
        val entity = FollowEntity(followerId = followerId, followedId = followedId)
        try {
            supabase.postgrest["follows"].insert(entity)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        followDao.insertFollow(entity)
    }

    override suspend fun unfollowUser(followerId: String, followedId: String) {
        try {
            supabase.postgrest["follows"].delete {
                filter { eq("followerId", followerId) }
                filter { eq("followedId", followedId) }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        followDao.deleteFollow(followerId, followedId)
    }

    override fun isFollowing(followerId: String, followedId: String): Flow<Boolean> {
        return followDao.isFollowing(followerId, followedId)
    }

    override fun getFollowStats(
        targetUserId: String,
        currentUserId: String
    ): Flow<FollowStats> {
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