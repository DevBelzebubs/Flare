package com.social.flare.features.feed.data.repository

import android.net.Uri
import com.social.flare.core.media.CloudinaryService
import com.social.flare.features.feed.data.local.dao.StoryDao
import com.social.flare.features.feed.data.local.entity.StoryCommentEntity
import com.social.flare.features.feed.data.local.entity.StoryEntity
import com.social.flare.features.feed.data.local.entity.StoryViewEntity
import com.social.flare.features.feed.data.local.entity.StoryWithAuthor
import com.social.flare.features.feed.data.mapper.toDomainModel
import com.social.flare.features.feed.domain.model.StoryComment
import com.social.flare.features.feed.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class StoryRepositoryImpl(
    private val storyDao: StoryDao,
    private val cloudinaryService: CloudinaryService
) : StoryRepository {

    override suspend fun createStory(authorId: String, imageUri: Uri): Result<Unit> {
        return try {
            val imageUrl = cloudinaryService.uploadImage(imageUri)
            if (imageUrl == null) {
                return Result.failure(Exception("Error al subir la imagen a la nube"))
            }
            val currentTime = System.currentTimeMillis()
            val expiresIn24Hours = currentTime + (24 * 60 * 60 * 1000L)
            val newStory = StoryEntity(
                story_id = UUID.randomUUID().toString(),
                author_id = authorId,
                media_url = imageUrl,
                created_at = System.currentTimeMillis(),
                expires_at = expiresIn24Hours,
                is_viewed = false
            )

            storyDao.insertStory(newStory)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override fun getActiveStories(currentUserId: String): Flow<List<StoryWithAuthor>> {
        val currentTime = System.currentTimeMillis()
        return storyDao.getActiveStories(currentUserId = currentUserId, currentTime = currentTime)
    }

    override suspend fun markStoryAsViewed(storyId: String, citizenId: String) {
        storyDao.insertStoryView(
            StoryViewEntity(
                story_id = storyId,
                citizen_id = citizenId,
                viewed_at = System.currentTimeMillis()
            )
        )
    }

    override fun getStoryComments(storyId: String): Flow<List<StoryComment>> {
        return storyDao.getCommentsForStory(storyId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun addCommentToStory(
        storyId: String,
        authorId: String,
        content: String
    ) {
        val commentEntity = StoryCommentEntity(
            comment_id = UUID.randomUUID().toString(),
            story_id = storyId,
            author_id = authorId,
            content = content,
            created_at = System.currentTimeMillis()
        )
        storyDao.insertStoryComment(commentEntity)
    }

    override suspend fun deleteStory(storyId: String) {
        try {
            val story = storyDao.getStoryByIdSync(storyId)
            if (story != null){
                if (story.media_url.isNotBlank()) {
                    cloudinaryService.deleteImage(story.media_url)
                }
                storyDao.deleteStory(storyId);
            }
        } catch (e: Exception) {
            e.printStackTrace();
        }
        storyDao.deleteStory(storyId)
    }
}