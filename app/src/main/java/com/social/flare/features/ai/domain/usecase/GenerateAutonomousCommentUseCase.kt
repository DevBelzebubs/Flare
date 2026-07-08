package com.social.flare.features.ai.domain.usecase

import com.social.flare.features.ai.domain.model.AiPersona
import com.social.flare.features.ai.domain.repository.AiAgentRepository
import com.social.flare.features.feed.domain.repository.FeedRepository
import javax.inject.Inject
import kotlin.collections.emptyList

class GenerateAutonomousCommentUseCase @Inject constructor(
    private val aiRepository: AiAgentRepository,
    private val feedRepository: FeedRepository
) {
    suspend fun execute(persona: AiPersona, postId: String, postContent: String): Result<Unit> {
        val commentResult = aiRepository.generateComment(persona, postContent)

        return if (commentResult.isSuccess) {
            val finalCommentText = commentResult.getOrThrow()
            if (finalCommentText.isBlank()) {
                return Result.failure(Exception("Comentario vacío después de generar"))
            }

            try {
                feedRepository.createPost(
                    authorId = persona.citizenId,
                    content = finalCommentText,
                    mediaUrls = emptyList(),
                    parentPostId = postId
                )
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(commentResult.exceptionOrNull() ?: Exception("Unknown AI comment error"))
        }
    }
}