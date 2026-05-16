package com.social.flare.features.feed.domain.repository

import android.net.Uri
import com.social.flare.features.feed.data.local.entity.StoryWithAuthor
import com.social.flare.features.feed.domain.model.StoryComment
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    suspend fun createStory(authorId: String, imageUri: Uri): Result<Unit>
    fun getActiveStories(currentUserId: String): Flow<List<StoryWithAuthor>>
    suspend fun markStoryAsViewed(storyId: String, citizenId: String)
    fun getStoryComments(storyId: String): Flow<List<StoryComment>>
    suspend fun addCommentToStory(storyId: String, authorId: String, content: String)
}