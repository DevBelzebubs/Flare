package com.social.flare.features.ai.domain.usecase

import android.util.Log
import com.social.flare.features.ai.domain.model.AiPersona
import com.social.flare.features.ai.domain.repository.AiAgentRepository
import com.social.flare.features.feed.domain.repository.FeedRepository
import javax.inject.Inject

class GenerateAutonomousPostUseCase @Inject constructor(
    private val aiRepository: AiAgentRepository,
    private val feedRepository: FeedRepository
) {
    suspend fun execute(persona: AiPersona, contextTopic: String, shouldIncludeImage: Boolean = false): Result<Unit> {
        return try {
            val contentResult = aiRepository.generatePost(persona, contextTopic)
            val postContent = contentResult.getOrThrow()

            var mediaUrls = emptyList<String>()

            if (shouldIncludeImage) {
                Log.d("AiBot", "Intentando generar imagen para el tema: $contextTopic")
                val promptResult = aiRepository.generateVisualPrompt(persona, contextTopic)

                if (promptResult.isSuccess) {
                    val visualPrompt = promptResult.getOrNull()!!
                    Log.d("AiBot", "Prompt Visual: $visualPrompt")

                    val imageResult = aiRepository.generateAndUploadImage(visualPrompt)
                    if (imageResult.isSuccess) {
                        val imageUrl = imageResult.getOrNull()!!
                        mediaUrls = listOf(imageUrl)
                        Log.d("AiBot", "Imagen lista: $mediaUrls")
                    } else {
                        Log.w("AiBot", "Falló la generación de imagen, publicando solo texto.")
                    }
                }
            }

            val postData = mutableMapOf(
                "author_id" to persona.citizenId,
                "content" to postContent,
                "created_at" to System.currentTimeMillis()
            )
            if (mediaUrls != null) {
                postData["media_urls"] = "[\"$mediaUrls\"]"
            }
            feedRepository.createPost(
                authorId = persona.citizenId,
                content = postContent,
                mediaUrls = mediaUrls
            )
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}