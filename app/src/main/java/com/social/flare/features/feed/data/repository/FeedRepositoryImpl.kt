package com.social.flare.features.feed.data.repository

import com.social.flare.features.feed.data.local.dao.PostDao
import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.feed.data.local.entity.PostLikeEntity
import com.social.flare.features.feed.data.mapper.toDomain
import com.social.flare.features.feed.data.mapper.toDomainModel
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.feed.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class FeedRepositoryImpl(
    private val postDao: PostDao
) : FeedRepository {

    override fun getFeedPosts(currentUserId: String): Flow<List<Post>> {
        return postDao.getFeedPosts(currentUserId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPostReplies(parentPostId: String, currentUserId: String): Flow<List<Post>> {
        return postDao.getPostReplies(parentPostId, currentUserId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createPost(
        authorId: String,
        content: String?,
        mediaUrls: List<String>,
        replyToPostId: String?
    ): Result<Unit> {
        return try {
            val newPost = PostEntity(
                post_id = UUID.randomUUID().toString(),
                author_id = authorId,
                content = content,
                media_urls = mediaUrls,
                reply_to_post_id = replyToPostId,
                created_at = System.currentTimeMillis(),
                sync_status = 0
            )
            postDao.insertPost(newPost)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleLike(
        postId: String,
        citizenId: String,
        isCurrentlyLiked: Boolean
    ): Result<Unit> {
        return try {
            if (isCurrentlyLiked) {
                postDao.deleteLike(postId, citizenId)
            } else {
                postDao.insertLike(PostLikeEntity(postId, citizenId))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePost(
        postId: String,
        currentUserId: String,
        newContent: String
    ): Result<Unit> {
        return try {
            val rowsAffected = postDao.updatePostContent(postId,currentUserId,newContent)
            if (rowsAffected > 0){
                Result.success(Unit)
            } else {
                Result.failure(Exception("No se pudo editar. El post no existe o no te pertenece."))
            }
        }catch (e: Exception){
            Result.failure(e);
        }
    }

    override suspend fun deletePost(
        postId: String,
        currentUserId: String
    ): Result<Unit> {
        return try {
            postDao.deletePostSafely(postId,currentUserId)
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e);
        }
    }

    override fun getUserPosts(userId: String): Flow<List<Post>> {
        return postDao.getPostsByAuthor(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}