package com.social.flare.features.feed.data.repository

import android.net.Uri
import com.social.flare.core.media.CloudinaryService
import com.social.flare.features.feed.data.local.dao.StoryDao
import com.social.flare.features.feed.data.local.entity.StoryEntity
import com.social.flare.features.feed.data.local.entity.StoryWithAuthor
import com.social.flare.features.feed.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
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
            val newStory = StoryEntity(
                story_id = UUID.randomUUID().toString(),
                author_id = authorId,
                media_url = imageUrl,
                created_at = System.currentTimeMillis(),
                is_viewed = false
            )

            storyDao.insertStory(newStory)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override fun getActiveStories(): Flow<List<StoryWithAuthor>> {
        val twentyFourHoursInMillis = 24 * 60 * 60 * 1000L
        val twentyFourHoursAgo = System.currentTimeMillis() - twentyFourHoursInMillis
        return storyDao.getActiveStories(twentyFourHoursAgo)
    }

    override suspend fun markStoryAsViewed(storyId: String) {
        storyDao.markStoryAsViewed(storyId)
    }
}