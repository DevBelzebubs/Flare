package com.social.flare.features.ai.domain.repository

import android.net.Uri
import com.social.flare.features.ai.domain.model.AiPersona
import com.social.flare.features.feed.domain.model.Post

interface AiAgentRepository {
    suspend fun generatePost(persona: AiPersona, topicContext: String): Result<String>
    suspend fun generateComment(persona: AiPersona, targetPostContent: String): Result<String>
    suspend fun getActiveBots(): Result<List<AiPersona>>
    suspend fun decideAction(persona: AiPersona, targetPostContent: String): Result<String>
    suspend fun generateAndUploadImage(prompt: String): Result<String>
    suspend fun generateVisualPrompt(persona: AiPersona, topicContext: String): Result<String>
    suspend fun followUser(followerId: String, followedId: String): Result<Unit>
    suspend fun generateLocalImageUri(prompt: String): Result<Uri>
}