package com.social.flare.features.feed.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import com.social.flare.features.feed.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM post_table ORDER BY created_at DESC")
    fun getAllPosts(): Flow<List<PostEntity>>
}