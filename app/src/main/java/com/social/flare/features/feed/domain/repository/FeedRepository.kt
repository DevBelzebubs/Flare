package com.social.flare.features.feed.domain.repository

import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.post.domain.model.PostDetail
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    fun getFeedPosts(currentUserId: String): Flow<List<Post>>
    fun getFeedPostsGuest(): Flow<List<Post>>
    fun getPostReplies(parentPostId: String, currentUserId: String): Flow<List<Post>>
    suspend fun createPost(
        authorId: String,
        content: String,
        mediaUrls: List<String>,
        parentPostId: String? = null
    )

    suspend fun toggleLike(postId: String, citizenId: String, isCurrentlyLiked: Boolean): Result<Unit>
    suspend fun toggleSavePost(postId: String, citizenId: String, isCurrentlySaved: Boolean)
    suspend fun updatePost(postId: String, currentUserId: String, newContent: String): Result<Unit>
    suspend fun deletePost(postId: String, currentUserId: String): Result<Unit>
    fun getUserPosts(userId: String): Flow<List<Post>>
    fun getPostDetail(postId: String, currentUserId: String): Flow<PostDetail>
    suspend fun getPostById(PostId: String) : Post?
    suspend fun sharePost(authorId: String, originalPostId: String)
    fun getSharedPosts(userId: String): Flow<List<Post>>
}