package com.social.flare.features.feed.data.repository

import android.net.Uri
import com.social.flare.core.media.CloudinaryService
import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.feed.data.local.dao.StoryDao
import com.social.flare.features.feed.data.local.entity.StoryCommentEntity
import com.social.flare.features.feed.data.local.entity.StoryEntity
import com.social.flare.features.feed.data.local.entity.StoryViewEntity
import com.social.flare.features.feed.data.local.entity.StoryWithAuthor
import com.social.flare.features.feed.data.mapper.toDomainModel
import com.social.flare.features.feed.domain.model.StoryComment
import com.social.flare.features.feed.domain.repository.StoryRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class StoryRepositoryImpl @Inject constructor(
    private val storyDao: StoryDao,
    private val citizenDao: CitizenDao,
    private val cloudinaryService: CloudinaryService,
    private val supabase: SupabaseClient
) : StoryRepository {

    override suspend fun createStory(
        authorId: String,
        imageUri: Uri,
        musicUrl: String?,
        musicTitle: String?,
        musicArtist: String?,
        musicCoverUrl: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val imageUrl = try {
                cloudinaryService.uploadImage(imageUri)
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("Error al subir la imagen a la nube", e))
            }
            val currentTime = System.currentTimeMillis()
            val expiresIn24Hours = currentTime + (24 * 60 * 60 * 1000L)

            val newStory = StoryEntity(
                story_id = UUID.randomUUID().toString(),
                author_id = authorId,
                media_url = imageUrl,
                created_at = currentTime,
                expires_at = expiresIn24Hours,
                is_viewed = false,
                music_url = musicUrl,
                music_title = musicTitle,
                music_artist = musicArtist,
                music_cover_url = musicCoverUrl
            )

            supabase.postgrest["stories"].insert(newStory)
            storyDao.insertStory(newStory)

            Result.success(Unit)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private val deletedStoryIds = mutableSetOf<String>()

    override fun getActiveStories(currentUserId: String): Flow<List<StoryWithAuthor>> = flow {
        withContext(Dispatchers.IO) {
            try {
                val currentTime = System.currentTimeMillis()

                supabase.postgrest["stories"].delete {
                    filter { lte("expires_at", currentTime) }
                }

                val stories = supabase.postgrest["stories"]
                    .select { filter { gt("expires_at", currentTime) } }
                    .decodeList<StoryEntity>()

                val authorIds = stories.map { it.author_id }.distinct()

                val authors = supabase.postgrest["citizens"]
                    .select { filter { isIn("citizen_id", authorIds) } }
                    .decodeList<CitizenEntity>()

                val myViews = supabase.postgrest["story_views"]
                    .select { filter { eq("citizen_id", currentUserId) } }
                    .decodeList<StoryViewEntity>()

                authors.forEach { citizenDao.insertCitizen(it) }
                stories.filter { it.story_id !in deletedStoryIds }.forEach { storyDao.insertStory(it) }
                myViews.forEach { storyDao.insertStoryView(it) }

                deletedStoryIds.toList().forEach { storyDao.deleteStory(it) }
                deletedStoryIds.clear()

            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        emitAll(storyDao.getActiveStories(
            currentUserId = currentUserId,
            currentTime = System.currentTimeMillis()
        ))
    }

    override suspend fun markStoryAsViewed(storyId: String, citizenId: String) {
        withContext(Dispatchers.IO) {
        val view = StoryViewEntity(
            story_id = storyId,
            citizen_id = citizenId,
            viewed_at = System.currentTimeMillis()
        )

        storyDao.markStoryAsViewedTransaction(storyId, view)

        try {
            supabase.postgrest["story_views"].insert(view)

            supabase.postgrest["stories"].update({
                set("is_viewed", true)
            }) {
                filter { eq("story_id", storyId) }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        }
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
    ) = withContext(Dispatchers.IO) {
        val commentEntity = StoryCommentEntity(
            comment_id = UUID.randomUUID().toString(),
            story_id = storyId,
            author_id = authorId,
            content = content,
            created_at = System.currentTimeMillis()
        )
        try {
            supabase.postgrest["story_comments"].insert(commentEntity)
        } catch (e: Throwable) { e.printStackTrace() }
        storyDao.insertStoryComment(commentEntity)
    }

    override suspend fun deleteStory(storyId: String) = withContext(Dispatchers.IO) {
        deletedStoryIds.add(storyId)
        storyDao.deleteStory(storyId)
        try {
            val story = storyDao.getStoryByIdSync(storyId)
            if (story != null) {
                if (story.media_url.isNotBlank()) {
                    cloudinaryService.deleteImage(story.media_url)
                }
                supabase.postgrest["stories"].delete { filter { eq("story_id", storyId) } }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        storyDao.deleteStory(storyId)
    }
}