package com.social.flare.features.profile.data.repository

import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.profile.data.local.dao.FollowDao
import com.social.flare.features.profile.data.local.entity.FollowEntity
import com.social.flare.features.profile.domain.model.FollowStats
import com.social.flare.features.profile.domain.repository.FollowRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class FollowRepositoryImpl(
    private val followDao: FollowDao,
    private val citizenDao: CitizenDao,
    private val supabase: SupabaseClient
) : FollowRepository {

    override suspend fun followUser(followerId: String, followedId: String) = withContext(Dispatchers.IO) {
        val entity = FollowEntity(followerId = followerId, followedId = followedId)
        try {
            supabase.postgrest["follows"].insert(entity)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        try {
            followDao.insertFollow(entity)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override suspend fun unfollowUser(followerId: String, followedId: String) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["follows"].delete {
                filter { eq("followerId", followerId) }
                filter { eq("followedId", followedId) }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        try {
            followDao.deleteFollow(followerId, followedId)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
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

    override suspend fun getFollowedIds(userId: String): List<String> = withContext(Dispatchers.IO) {
        followDao.getFollowedIds(userId)
    }

    override suspend fun getFollowers(userId: String): List<CitizenEntity> = withContext(Dispatchers.IO) {
        try {
            val follows = supabase.postgrest["follows"]
                .select { filter { eq("followedId", userId) } }
                .decodeList<FollowEntity>()
            follows.forEach { followDao.insertFollow(it) }
            val ids = follows.map { it.followerId }
            if (ids.isNotEmpty()) {
                supabase.postgrest["citizens"]
                    .select { filter { isIn("citizen_id", ids) } }
                    .decodeList<CitizenEntity>()
                    .forEach { citizenDao.insertCitizen(it) }
            }
        } catch (_: Exception) {}
        val ids = followDao.getFollowerIds(userId)
        if (ids.isEmpty()) emptyList() else citizenDao.getCitizensByIds(ids)
    }

    override suspend fun getFollowing(userId: String): List<CitizenEntity> = withContext(Dispatchers.IO) {
        try {
            val follows = supabase.postgrest["follows"]
                .select { filter { eq("followerId", userId) } }
                .decodeList<FollowEntity>()
            follows.forEach { followDao.insertFollow(it) }
            val ids = follows.map { it.followedId }
            if (ids.isNotEmpty()) {
                supabase.postgrest["citizens"]
                    .select { filter { isIn("citizen_id", ids) } }
                    .decodeList<CitizenEntity>()
                    .forEach { citizenDao.insertCitizen(it) }
            }
        } catch (_: Exception) {}
        val ids = followDao.getFollowedIds(userId)
        if (ids.isEmpty()) emptyList() else citizenDao.getCitizensByIds(ids)
    }
}