package com.social.flare.features.ai.domain.usecase

import com.social.flare.features.ai.domain.model.AiPersona
import com.social.flare.features.ai.domain.repository.AiAgentRepository
import com.social.flare.features.feed.domain.repository.FeedRepository
import javax.inject.Inject

class GenerateAutonomousPostUseCase @Inject constructor(
    private val aiRepository: AiAgentRepository,
    private val feedRepository: FeedRepository
) {
    suspend fun execute(persona: AiPersona, contextTopic: String): Result<Unit> {
        val generationResult = aiRepository.generatePost(persona, contextTopic)

        return if (generationResult.isSuccess) {
            val postContent = generationResult.getOrThrow()
            feedRepository.createPost(
                authorId = persona.citizenId,
                content = postContent,
                mediaUrls = emptyList()
            )
            Result.success(Unit)
        } else {
            Result.failure(
                generationResult.exceptionOrNull() ?: Exception("Unknown AI generation error")
            )
        }
    }
}