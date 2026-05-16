package com.social.flare.features.feed.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.feed.data.local.entity.PostLikeEntity
import com.social.flare.features.feed.data.local.entity.SavedPostEntity
import kotlinx.coroutines.flow.Flow

data class PostWithDetails(
    @Embedded val post: PostEntity,
    val authorDisplayName: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val likesCount: Int,
    val commentsCount: Int,
    val isLikedByMe: Boolean,
    val isSavedByMe: Boolean
)

@Dao
interface PostDao {
    @Query("SELECT * FROM post_table ORDER BY created_at DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    // --- LIKES ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLike(like: PostLikeEntity)

    @Query("DELETE FROM post_likes WHERE post_id = :postId AND citizen_id = :citizenId")
    suspend fun deleteLike(postId: String, citizenId: String)

    // --- SAVED POSTS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPost(savedPost: SavedPostEntity)

    @Query("DELETE FROM saved_post_table WHERE citizen_id = :citizenId AND post_id = :postId")
    suspend fun deleteSavedPost(citizenId: String, postId: String)

    // --- POSTS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Query("""
        SELECT 
            p.*, 
            c.display_name AS authorDisplayName, 
            c.username AS authorUsername, 
            c.avatar_url AS authorAvatarUrl,
            (SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id) AS likesCount,
            (SELECT COUNT(*) FROM post_table WHERE parent_post_id = p.post_id) AS commentsCount,
            EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isLikedByMe,
            EXISTS(SELECT 1 FROM saved_post_table WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isSavedByMe
        FROM post_table p
        INNER JOIN citizen_table c ON p.author_id = c.citizen_id
        WHERE p.parent_post_id IS NULL 
        ORDER BY p.created_at DESC
    """)
    fun getFeedPosts(currentUserId: String): Flow<List<PostWithDetails>>

    @Query("""
        SELECT 
            p.*, 
            c.display_name AS authorDisplayName, 
            c.username AS authorUsername, 
            c.avatar_url AS authorAvatarUrl,
            (SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id) AS likesCount,
            (SELECT COUNT(*) FROM post_table WHERE parent_post_id = p.post_id) AS commentsCount,
            EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isLikedByMe,
            EXISTS(SELECT 1 FROM saved_post_table WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isSavedByMe
        FROM post_table p
        INNER JOIN citizen_table c ON p.author_id = c.citizen_id
        WHERE p.post_id = :postId
    """)
    fun getPostById(postId: String, currentUserId: String): Flow<PostWithDetails?>

    @Query("""
        SELECT 
            p.*, 
            c.display_name AS authorDisplayName, 
            c.username AS authorUsername, 
            c.avatar_url AS authorAvatarUrl,
            (SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id) AS likesCount,
            (SELECT COUNT(*) FROM post_table WHERE parent_post_id = p.post_id) AS commentsCount,
            EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isLikedByMe,
            EXISTS(SELECT 1 FROM saved_post_table WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isSavedByMe
        FROM post_table p
        INNER JOIN citizen_table c ON p.author_id = c.citizen_id
        WHERE p.parent_post_id = :parentPostId
        ORDER BY p.created_at ASC
    """)
    fun getPostReplies(parentPostId: String, currentUserId: String): Flow<List<PostWithDetails>>

    @Query("UPDATE post_table SET content = :newContent, sync_status = 0 WHERE post_id = :postId AND author_id = :currentUserId")
    suspend fun updatePostContent(postId: String, currentUserId: String, newContent: String): Int

    @Query("SELECT author_id FROM post_table WHERE post_id = :postId")
    suspend fun getPostAuthor(postId: String): String?

    @Query("DELETE FROM post_likes WHERE post_id = :postId")
    suspend fun deleteAdjacentLikes(postId: String)

    @Query("DELETE FROM post_table WHERE parent_post_id = :postId")
    suspend fun deleteAdjacentReplies(postId: String)

    @Query("UPDATE post_table SET comments_count = comments_count + 1 WHERE post_id = :postId")
    suspend fun incrementCommentsCount(postId: String)

    @Query("DELETE FROM post_table WHERE post_id = :postId")
    suspend fun deleteTargetPost(postId: String)

    @Transaction
    suspend fun deletePostSafely(postId: String, currentUserId: String) {
        val author = getPostAuthor(postId)
        if (author == currentUserId) {
            deleteAdjacentLikes(postId)
            deleteAdjacentReplies(postId)
            deleteTargetPost(postId)
        } else {
            throw SecurityException("Access Denied: No tienes permisos para borrar este post.")
        }
    }

    @Query("""
        SELECT 
            p.*, 
            c.display_name AS authorDisplayName, 
            c.username AS authorUsername, 
            c.avatar_url AS authorAvatarUrl,
            (SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id) AS likesCount,
            (SELECT COUNT(*) FROM post_table WHERE parent_post_id = p.post_id) AS commentsCount,
            EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.post_id AND citizen_id = :userId) AS isLikedByMe,
            EXISTS(SELECT 1 FROM saved_post_table WHERE post_id = p.post_id AND citizen_id = :userId) AS isSavedByMe
        FROM post_table p
        INNER JOIN citizen_table c ON p.author_id = c.citizen_id
        WHERE p.author_id = :userId AND p.parent_post_id IS NULL
        ORDER BY p.created_at DESC
    """)
    fun getPostsByAuthor(userId: String): Flow<List<PostWithDetails>>

    @Query("""
        SELECT 
            p.*, 
            c.display_name AS authorDisplayName, 
            c.username AS authorUsername, 
            c.avatar_url AS authorAvatarUrl,
            (SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id) AS likesCount,
            (SELECT COUNT(*) FROM post_table WHERE parent_post_id = p.post_id) AS commentsCount,
            EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isLikedByMe,
            EXISTS(SELECT 1 FROM saved_post_table WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isSavedByMe
        FROM post_table p
        INNER JOIN citizen_table c ON p.author_id = c.citizen_id
        WHERE p.parent_post_id = :parentId
        ORDER BY p.created_at ASC
    """)
    fun getRepliesForPost(parentId: String, currentUserId: String): Flow<List<PostWithDetails>>

    @Query("""
        SELECT p.*,
               c.display_name AS authorDisplayName,
               c.username AS authorUsername,
               c.avatar_url AS authorAvatarUrl,
               (SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id) AS likesCount,
               (SELECT COUNT(*) FROM post_table WHERE parent_post_id = p.post_id) AS commentsCount,
               EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isLikedByMe,
               EXISTS(SELECT 1 FROM saved_post_table WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isSavedByMe
        FROM post_table p
        INNER JOIN saved_post_table sp ON p.post_id = sp.post_id
        INNER JOIN citizen_table c ON p.author_id = c.citizen_id
        WHERE sp.citizen_id = :currentUserId
        ORDER BY sp.saved_at DESC
    """)
    fun getSavedPosts(currentUserId: String): Flow<List<PostWithDetails>>
}