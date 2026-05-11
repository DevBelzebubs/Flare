package com.social.flare.features.feed.domain.repository

import com.social.flare.features.feed.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    fun getFeedPosts(currentUserId: String): Flow<List<Post>>
    fun getPostReplies(parentPostId: String, currentUserId: String): Flow<List<Post>>
    suspend fun createPost(
        authorId: String,
        content: String?,
        mediaUrls: List<String>,
        replyToPostId: String?
    ): Result<Unit>

    suspend fun toggleLike(postId: String, citizenId: String, isCurrentlyLiked: Boolean): Result<Unit>
}