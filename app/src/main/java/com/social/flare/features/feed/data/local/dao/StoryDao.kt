package com.social.flare.features.feed.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.social.flare.features.feed.data.local.entity.StoryCommentEntity
import com.social.flare.features.feed.data.local.entity.StoryEntity
import com.social.flare.features.feed.data.local.entity.StoryViewEntity
import com.social.flare.features.feed.data.local.entity.StoryWithAuthor
import com.social.flare.features.feed.domain.model.StoryCommentWithAuthor
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStoryView(storyView: StoryViewEntity)
    @Transaction
    @Query("""
        SELECT s.*, 
               c.avatar_url AS authorAvatarUrl, 
               c.username AS authorUsername,
               EXISTS(
                   SELECT 1 FROM story_view_table 
                   WHERE story_id = s.story_id AND citizen_id = :currentUserId
               ) AS isViewedByMe
        FROM story_table s
        INNER JOIN citizen_table c ON s.author_id = c.citizen_id
        WHERE s.expires_at > :currentTime 
        AND (
            s.author_id = :currentUserId 
            OR 
            s.author_id IN (
                SELECT followedId FROM follow_table WHERE followerId = :currentUserId
            )
        )
        ORDER BY isViewedByMe ASC, s.created_at DESC
    """)
    fun getActiveStories(currentUserId: String, currentTime: Long): Flow<List<StoryWithAuthor>>

    @Query("UPDATE story_table SET is_viewed = 1 WHERE story_id = :storyId")
    suspend fun markStoryAsViewed(storyId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoryComment(comment: StoryCommentEntity)
    @Query("""
        SELECT 
            sc.*, 
            c.username AS authorUsername, 
            c.avatar_url AS authorAvatarUrl
        FROM story_comment_table sc
        INNER JOIN citizen_table c ON sc.author_id = c.citizen_id
        WHERE sc.story_id = :storyId
        ORDER BY sc.created_at ASC
    """)
    fun getCommentsForStory(storyId: String): Flow<List<StoryCommentWithAuthor>>
    @Query("DELETE FROM story_table WHERE story_id = :storyId")
    suspend fun deleteStory(storyId: String)
    @Query("SELECT * FROM story_table WHERE story_id = :storyId")
    suspend fun getStoryByIdSync(storyId: String): StoryEntity?
}