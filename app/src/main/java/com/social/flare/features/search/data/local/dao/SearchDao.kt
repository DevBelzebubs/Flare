package com.social.flare.features.search.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.feed.data.local.dao.PostWithDetails
import com.social.flare.features.feed.data.local.entity.HashtagEntity
import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.search.domain.model.TrendingHashtag
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchDao {
    @Query("SELECT * FROM citizen_table WHERE username LIKE '%' || :query || '%' OR display_name LIKE '%' || :query || '%' LIMIT 20")
    fun searchUsers(query: String): Flow<List<CitizenEntity>>

    @Transaction
    @Query("""
        SELECT p.*, c.display_name AS authorDisplayName, c.username AS authorUsername,
        c.avatar_url AS authorAvatarUrl, p.likes_count AS likesCount,
        p.comments_count AS commentsCount, 0 AS isLikedByMe, 0 AS isSavedByMe,
        0 AS isSharedByMe,
        (SELECT COUNT(*) FROM post_table WHERE shared_post_id = p.post_id) AS sharesCount
        FROM post_table p
        INNER JOIN citizen_table c ON p.author_id = c.citizen_id
        WHERE p.content LIKE '%' || :query || '%' AND p.parent_post_id IS NULL AND p.shared_post_id IS NULL
        ORDER BY p.created_at DESC
        LIMIT 50
    """)
    fun searchPosts(query: String): Flow<List<PostWithDetails>>

    @Query("SELECT * FROM hashtags WHERE name LIKE '%' || :query || '%'")
    fun searchHashtags(query: String): Flow<List<HashtagEntity>>

    @Transaction
    @Query("""
        SELECT p.*, c.display_name AS authorDisplayName, c.username AS authorUsername,
        c.avatar_url AS authorAvatarUrl, p.likes_count AS likesCount,
        p.comments_count AS commentsCount, 0 AS isLikedByMe, 0 AS isSavedByMe,
        0 AS isSharedByMe,
        (SELECT COUNT(*) FROM post_table WHERE shared_post_id = p.post_id) AS sharesCount
        FROM post_table p
        INNER JOIN post_hashtags ph ON p.post_id = ph.post_id
        INNER JOIN hashtags h ON ph.tag_id = h.tag_id
        INNER JOIN citizen_table c ON p.author_id = c.citizen_id
        WHERE h.name = :hashtag
        ORDER BY p.created_at DESC
    """)
    fun getPostsByHashtag(hashtag: String): Flow<List<PostWithDetails>>

    @Query("""
        SELECT h.name AS name, COUNT(ph.post_id) AS postCount
        FROM hashtags h
        INNER JOIN post_hashtags ph ON h.tag_id = ph.tag_id
        GROUP BY h.tag_id, h.name
        ORDER BY postCount DESC
        LIMIT 10
    """)
    fun getTrendingHashtags(): Flow<List<TrendingHashtag>>
}