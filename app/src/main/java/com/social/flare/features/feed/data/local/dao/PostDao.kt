package com.social.flare.features.feed.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.feed.data.local.entity.PostLikeEntity
import kotlinx.coroutines.flow.Flow
data class PostWithDetails(
    @Embedded val post: PostEntity,
    val authorDisplayName: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val likesCount: Int,
    val commentsCount: Int,
    val isLikedByMe: Boolean
)
@Dao
interface PostDao {
    @Query("SELECT * FROM post_table ORDER BY created_at DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLike(like: PostLikeEntity)

    @Query("DELETE FROM post_likes WHERE post_id = :postId AND citizen_id = :citizenId")
    suspend fun deleteLike(postId: String, citizenId: String)
    // Posts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)
    @Query("""
        SELECT p.*,
               c.display_name AS authorDisplayName,
               c.username AS authorUsername,
               c.avatar_url AS authorAvatarUrl,
               (SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id) AS likesCount,
               (SELECT COUNT(*) FROM post_table WHERE reply_to_post_id = p.post_id) AS commentsCount,
               EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isLikedByMe
        FROM post_table p
        INNER JOIN citizen_table c ON p.author_id = c.citizen_id
        WHERE p.reply_to_post_id IS NULL
        ORDER BY p.created_at DESC
    """)
    fun getFeedPosts(currentUserId: String): Flow<List<PostWithDetails>>
    @Query("""
        SELECT p.*,
               c.display_name AS authorDisplayName,
               c.username AS authorUsername,
               c.avatar_url AS authorAvatarUrl,
               (SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id) AS likesCount,
               (SELECT COUNT(*) FROM post_table WHERE reply_to_post_id = p.post_id) AS commentsCount,
               EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isLikedByMe
        FROM post_table p
        INNER JOIN citizen_table c ON p.author_id = c.citizen_id
        WHERE p.reply_to_post_id = :parentPostId
        ORDER BY p.created_at ASC
    """)
    fun getPostReplies(parentPostId: String, currentUserId: String): Flow<List<PostWithDetails>>

}