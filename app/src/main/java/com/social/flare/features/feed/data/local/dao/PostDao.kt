package com.social.flare.features.feed.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.social.flare.features.feed.data.local.entity.HashtagEntity
import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.feed.data.local.entity.PostHashtagEntity
import com.social.flare.features.feed.data.local.entity.PostLikeEntity
import com.social.flare.features.feed.data.local.entity.PostVoteEntity
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
    val isSavedByMe: Boolean,
    val isSharedByMe: Boolean = false,
    val sharesCount: Int = 0
)

data class AuthorPostCount(
    val authorId: String,
    val count: Int
)

data class UserVote(
    val post_id: String,
    val selected_option_index: Int
)

@Dao
interface PostDao {
    @Query("SELECT * FROM post_table ORDER BY created_at DESC LIMIT 100")
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
        p.likes_count AS likesCount,
        p.comments_count AS commentsCount,
        EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isLikedByMe,
        EXISTS(SELECT 1 FROM saved_post_table WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isSavedByMe,
        EXISTS(SELECT 1 FROM post_table WHERE shared_post_id = p.post_id AND author_id = :currentUserId) AS isSharedByMe,
        (SELECT COUNT(*) FROM post_table WHERE shared_post_id = p.post_id) AS sharesCount
    FROM post_table p
    INNER JOIN citizen_table c ON p.author_id = c.citizen_id
    WHERE p.parent_post_id IS NULL AND p.shared_post_id IS NULL 
    ORDER BY p.created_at DESC
    LIMIT 100
    """)
    fun getFeedPosts(currentUserId: String): Flow<List<PostWithDetails>>
    @Transaction
    @Query("""
        SELECT 
        p.*, 
        c.display_name AS authorDisplayName, 
        c.username AS authorUsername, 
        c.avatar_url AS authorAvatarUrl,
        p.likes_count AS likesCount,
        p.comments_count AS commentsCount,
        0 AS isLikedByMe,
        0 AS isSavedByMe,
        0 AS isSharedByMe,
        (SELECT COUNT(*) FROM post_table WHERE shared_post_id = p.post_id) AS sharesCount
    FROM post_table p
    INNER JOIN citizen_table c ON p.author_id = c.citizen_id
    WHERE p.parent_post_id IS NULL AND p.shared_post_id IS NULL
    ORDER BY p.created_at DESC
    LIMIT 100
    """)
    fun getFeedPostsGuest(): Flow<List<PostWithDetails>>

    @Query("""
        SELECT 
        p.*, 
        c.display_name AS authorDisplayName, 
        c.username AS authorUsername, 
        c.avatar_url AS authorAvatarUrl,
        p.likes_count AS likesCount,
        p.comments_count AS commentsCount,
        EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isLikedByMe,
        EXISTS(SELECT 1 FROM saved_post_table WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isSavedByMe,
        EXISTS(SELECT 1 FROM post_table WHERE shared_post_id = p.post_id AND author_id = :currentUserId) AS isSharedByMe,
        (SELECT COUNT(*) FROM post_table WHERE shared_post_id = p.post_id) AS sharesCount
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
        p.likes_count AS likesCount,
        p.comments_count AS commentsCount,
        EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isLikedByMe,
        EXISTS(SELECT 1 FROM saved_post_table WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isSavedByMe,
        EXISTS(SELECT 1 FROM post_table WHERE shared_post_id = p.post_id AND author_id = :currentUserId) AS isSharedByMe,
        (SELECT COUNT(*) FROM post_table WHERE shared_post_id = p.post_id) AS sharesCount
    FROM post_table p
    INNER JOIN citizen_table c ON p.author_id = c.citizen_id
    WHERE p.parent_post_id = :parentPostId
    ORDER BY p.created_at ASC
    LIMIT 50
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
            deletePostHashtags(postId)
            deleteOrphanHashtags()
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
        p.likes_count AS likesCount,
        p.comments_count AS commentsCount,
        EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.post_id AND citizen_id = :userId) AS isLikedByMe,
        EXISTS(SELECT 1 FROM saved_post_table WHERE post_id = p.post_id AND citizen_id = :userId) AS isSavedByMe,
        EXISTS(SELECT 1 FROM post_table WHERE shared_post_id = p.post_id AND author_id = :userId) AS isSharedByMe,
        (SELECT COUNT(*) FROM post_table WHERE shared_post_id = p.post_id) AS sharesCount
    FROM post_table p
    INNER JOIN citizen_table c ON p.author_id = c.citizen_id
    WHERE p.author_id = :userId AND p.parent_post_id IS NULL AND p.shared_post_id IS NULL
    ORDER BY p.created_at DESC
    LIMIT 100
""")
    fun getPostsByAuthor(userId: String): Flow<List<PostWithDetails>>

    @Query("""
        SELECT 
        p.*, 
        c.display_name AS authorDisplayName, 
        c.username AS authorUsername, 
        c.avatar_url AS authorAvatarUrl,
        p.likes_count AS likesCount,
        p.comments_count AS commentsCount,
        EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isLikedByMe,
        EXISTS(SELECT 1 FROM saved_post_table WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isSavedByMe,
        EXISTS(SELECT 1 FROM post_table WHERE shared_post_id = p.post_id AND author_id = :currentUserId) AS isSharedByMe,
        (SELECT COUNT(*) FROM post_table WHERE shared_post_id = p.post_id) AS sharesCount
    FROM post_table p
    INNER JOIN citizen_table c ON p.author_id = c.citizen_id
    WHERE p.parent_post_id = :parentId
    ORDER BY p.created_at ASC
    LIMIT 50
    """)
    fun getRepliesForPost(parentId: String, currentUserId: String): Flow<List<PostWithDetails>>

    @Query("""
        SELECT p.*,
           c.display_name AS authorDisplayName,
           c.username AS authorUsername,
           c.avatar_url AS authorAvatarUrl,
           p.likes_count AS likesCount,
           p.comments_count AS commentsCount,
           EXISTS(SELECT 1 FROM post_likes WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isLikedByMe,
           EXISTS(SELECT 1 FROM saved_post_table WHERE post_id = p.post_id AND citizen_id = :currentUserId) AS isSavedByMe,
           EXISTS(SELECT 1 FROM post_table WHERE shared_post_id = p.post_id AND author_id = :currentUserId) AS isSharedByMe,
           (SELECT COUNT(*) FROM post_table WHERE shared_post_id = p.post_id) AS sharesCount
    FROM post_table p
    INNER JOIN saved_post_table sp ON p.post_id = sp.post_id
    INNER JOIN citizen_table c ON p.author_id = c.citizen_id
    WHERE sp.citizen_id = :currentUserId
    ORDER BY sp.saved_at DESC
    LIMIT 100
    """)
    fun getSavedPosts(currentUserId: String): Flow<List<PostWithDetails>>
    @Transaction
    @Query("""
        SELECT 
        p.*, 
        c.display_name AS authorDisplayName, 
        c.username AS authorUsername, 
        c.avatar_url AS authorAvatarUrl,
        p.likes_count AS likesCount,
        p.comments_count AS commentsCount,
        0 AS isLikedByMe,
        0 AS isSavedByMe,
        0 AS isSharedByMe,
        (SELECT COUNT(*) FROM post_table WHERE shared_post_id = p.post_id) AS sharesCount
    FROM post_table p
    INNER JOIN citizen_table c ON p.author_id = c.citizen_id
    WHERE p.post_id = :postId
    """)
    suspend fun getPostById(postId: String): PostWithDetails?

    @Transaction
    @Query("""
        SELECT 
        p.*, 
        c.display_name AS authorDisplayName, 
        c.username AS authorUsername, 
        c.avatar_url AS authorAvatarUrl,
        p.likes_count AS likesCount,
        p.comments_count AS commentsCount,
        0 AS isLikedByMe,
        0 AS isSavedByMe,
        0 AS isSharedByMe,
        (SELECT COUNT(*) FROM post_table WHERE shared_post_id = p.post_id) AS sharesCount
    FROM post_table p
    INNER JOIN citizen_table c ON p.author_id = c.citizen_id
    WHERE p.parent_post_id IS NULL
    ORDER BY p.created_at DESC
    LIMIT 500
    """)
    suspend fun getAllPostsAdmin(): List<PostWithDetails>

    @Transaction
    suspend fun deletePostAdmin(postId: String) {
        deleteAdjacentLikes(postId)
        deleteAdjacentReplies(postId)
        deleteTargetPost(postId)
    }

    @Query("SELECT COUNT(*) FROM post_table WHERE author_id = :userId AND parent_post_id IS NULL AND shared_post_id IS NULL")
    suspend fun countPostsByAuthor(userId: String): Int

    @Query("SELECT author_id AS authorId, COUNT(*) AS count FROM post_table WHERE author_id IN (:authorIds) AND parent_post_id IS NULL AND shared_post_id IS NULL GROUP BY author_id")
    suspend fun getPostsCountsByAuthors(authorIds: List<String>): List<AuthorPostCount>

    @Transaction
    @Query("""
        SELECT 
        p.*, 
        c.display_name AS authorDisplayName, 
        c.username AS authorUsername, 
        c.avatar_url AS authorAvatarUrl,
        p.likes_count AS likesCount,
        p.comments_count AS commentsCount,
        0 AS isLikedByMe,
        0 AS isSavedByMe,
        0 AS isSharedByMe,
        (SELECT COUNT(*) FROM post_table WHERE shared_post_id = p.post_id) AS sharesCount
    FROM post_table p
    INNER JOIN citizen_table c ON p.author_id = c.citizen_id
    WHERE p.author_id = :userId AND p.shared_post_id IS NOT NULL
    ORDER BY p.created_at DESC
    LIMIT 100
    """)
    fun getSharedPosts(userId: String): Flow<List<PostWithDetails>>

    // --- POLL VOTES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVote(vote: PostVoteEntity)

    @Query("DELETE FROM post_votes WHERE post_id = :postId AND citizen_id = :citizenId")
    suspend fun deleteVote(postId: String, citizenId: String)

    @Query("SELECT selected_option_index FROM post_votes WHERE post_id = :postId AND citizen_id = :citizenId LIMIT 1")
    suspend fun getUserVote(postId: String, citizenId: String): Int?

    @Query("SELECT post_id, selected_option_index FROM post_votes WHERE post_id IN (:postIds) AND citizen_id = :citizenId")
    suspend fun getUserVotes(postIds: List<String>, citizenId: String): List<UserVote>

    @Transaction
    suspend fun castVoteTransaction(postId: String, citizenId: String, newVote: PostVoteEntity, post: PostEntity) {
        deleteVote(postId, citizenId)
        insertVote(newVote)
        insertPost(post)
    }

    @Transaction
    suspend fun toggleLikeTransaction(postId: String, citizenId: String, like: PostLikeEntity?, isLiked: Boolean) {
        if (isLiked) {
            deleteLike(postId, citizenId)
        } else {
            insertLike(like!!)
        }
    }

    @Transaction
    suspend fun insertPostWithHashtags(post: PostEntity, hashtags: List<HashtagEntity>, postHashtags: List<PostHashtagEntity>) {
        insertPost(post)
        hashtags.forEach { insertHashtag(it) }
        postHashtags.forEach { insertPostHashtag(it) }
    }

    @Transaction
    suspend fun syncHashtagsTransaction(hashtags: List<HashtagEntity>, postHashtags: List<PostHashtagEntity>) {
        clearAllHashtags()
        clearAllPostHashtags()
        hashtags.forEach { insertHashtag(it) }
        postHashtags.forEach { insertPostHashtag(it) }
    }

    @Query("DELETE FROM post_votes WHERE post_id = :postId")
    suspend fun deleteAllVotesForPost(postId: String)

    @Query("SELECT post_id FROM post_table WHERE author_id = :authorId AND shared_post_id = :originalPostId LIMIT 1")
    suspend fun getSharedPostId(authorId: String, originalPostId: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHashtag(hashtag: HashtagEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPostHashtag(relation: PostHashtagEntity)

    @Query("DELETE FROM hashtags")
    suspend fun clearAllHashtags()

    @Query("DELETE FROM post_hashtags")
    suspend fun clearAllPostHashtags()

    @Query("SELECT tag_id FROM hashtags WHERE name = :name LIMIT 1")
    suspend fun getHashtagId(name: String): String?

    @Query("DELETE FROM post_hashtags WHERE post_id = :postId")
    suspend fun deletePostHashtags(postId: String)

    @Query("DELETE FROM hashtags WHERE tag_id NOT IN (SELECT DISTINCT tag_id FROM post_hashtags)")
    suspend fun deleteOrphanHashtags()
}