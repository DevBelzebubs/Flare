package com.social.flare.features.ai.domain.repository

import com.social.flare.features.ai.domain.model.AiPersona
import com.social.flare.features.feed.domain.model.Post

interface AiAgentRepository {
    suspend fun generatePost(persona: AiPersona, topicContext: String): Result<String>
    suspend fun generateComment(persona: AiPersona, targetPostContent: String): Result<String>
    suspend fun getActiveBots(): Result<List<AiPersona>>
    suspend fun decideAction(persona: AiPersona, targetPostContent: String): Result<String>
}