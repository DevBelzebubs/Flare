package com.social.flare.features.profile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.social.flare.features.profile.data.local.entity.FollowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollow(follow: FollowEntity)

    @Query("DELETE FROM follow_table WHERE followerId = :followerId AND followedId = :followedId")
    suspend fun deleteFollow(followerId: String, followedId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM follow_table WHERE followerId = :followerId AND followedId = :followedId)")
    fun isFollowing(followerId: String, followedId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM follow_table WHERE followedId = :userId")
    fun getFollowersCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM follow_table WHERE followerId = :userId")
    fun getFollowingCount(userId: String): Flow<Int>

    @Query("SELECT followedId FROM follow_table WHERE followerId = :userId")
    suspend fun getFollowedIds(userId: String): List<String>

    @Query("SELECT followerId FROM follow_table WHERE followedId = :userId")
    suspend fun getFollowerIds(userId: String): List<String>
}