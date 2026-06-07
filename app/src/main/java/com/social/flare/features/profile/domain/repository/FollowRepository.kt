package com.social.flare.features.profile.domain.repository

import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.profile.domain.model.FollowStats
import kotlinx.coroutines.flow.Flow

interface FollowRepository {
    suspend fun followUser(followerId: String, followedId: String)
    suspend fun unfollowUser(followerId: String, followedId: String)
    fun isFollowing(followerId: String, followedId: String): Flow<Boolean>
    fun getFollowStats(targetUserId: String, currentUserId: String): Flow<FollowStats>
    suspend fun getFollowers(userId: String): List<CitizenEntity>
    suspend fun getFollowing(userId: String): List<CitizenEntity>
    suspend fun getFollowedIds(userId: String): List<String>
}