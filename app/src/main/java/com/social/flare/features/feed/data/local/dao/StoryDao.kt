package com.social.flare.features.feed.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.social.flare.features.feed.data.local.entity.StoryEntity
import com.social.flare.features.feed.data.local.entity.StoryWithAuthor
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)
    @Query("""
        SELECT 
            s.*, 
            c.username AS authorUsername, 
            c.avatar_url AS authorAvatarUrl
        FROM story_table s
        INNER JOIN citizen_table c ON s.author_id = c.citizen_id
        WHERE s.created_at >= :twentyFourHoursAgo
        ORDER BY s.created_at ASC
    """)
    fun getActiveStories(twentyFourHoursAgo: Long): Flow<List<StoryWithAuthor>>

    @Query("UPDATE story_table SET is_viewed = 1 WHERE story_id = :storyId")
    suspend fun markStoryAsViewed(storyId: String)
}