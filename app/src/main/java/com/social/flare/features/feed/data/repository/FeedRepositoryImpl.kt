package com.social.flare.features.feed.data.repository

import com.social.flare.features.feed.data.local.dao.PostDao
import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.feed.data.local.entity.PostLikeEntity
import com.social.flare.features.feed.data.local.entity.SavedPostEntity
import com.social.flare.features.feed.data.mapper.toDomain
import com.social.flare.features.feed.data.mapper.toDomainModel
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.feed.domain.repository.FeedRepository
import com.social.flare.features.post.domain.model.PostDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
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
        content: String,
        mediaUrls: List<String>,
        parentPostId: String?
    ) {
        val newPostId = UUID.randomUUID().toString()
        val mediaString = mediaUrls.joinToString(",")

        val newPost = PostEntity(
            post_id = newPostId,
            author_id = authorId,
            content = content,
            media_urls = mediaString,
            created_at = System.currentTimeMillis(),
            likes_count = 0,
            comments_count = 0,
            parent_post_id = parentPostId
        )

        postDao.insertPost(newPost)
        if (parentPostId != null) {
            postDao.incrementCommentsCount(parentPostId)
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

    override suspend fun toggleSavePost(postId: String, citizenId: String, isCurrentlySaved: Boolean) {
        if (isCurrentlySaved) {
            postDao.deleteSavedPost(citizenId, postId)
        } else {
            postDao.insertSavedPost(
                SavedPostEntity(
                    citizen_id = citizenId,
                    post_id = postId,
                    saved_at = System.currentTimeMillis()
                )
            )
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
            Result.failure(e)
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
            Result.failure(e)
        }
    }

    override fun getUserPosts(userId: String): Flow<List<Post>> {
        return postDao.getPostsByAuthor(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPostDetail(
        postId: String,
        currentUserId: String
    ): Flow<PostDetail> {
        val mainPostFlow = postDao.getPostById(postId, currentUserId).filterNotNull()

        val repliesFlow = postDao.getPostReplies(postId, currentUserId)

        return combine(mainPostFlow, repliesFlow) { mainPostEntity, repliesEntities ->
            PostDetail(
                mainPost = mainPostEntity.toDomain(),
                replies = repliesEntities.map { it.toDomain() }
            )
        }
    }
}