package com.social.flare.features.feed.domain.repository

import android.net.Uri
import com.social.flare.features.feed.data.local.entity.StoryWithAuthor
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    suspend fun createStory(authorId: String, imageUri: Uri): Result<Unit>

    fun getActiveStories(): Flow<List<StoryWithAuthor>>

    suspend fun markStoryAsViewed(storyId: String)
}