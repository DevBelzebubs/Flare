package com.social.flare.features.ai.domain.usecase

import android.util.Log
import com.social.flare.features.ai.domain.model.AiPersona
import com.social.flare.features.ai.domain.repository.AiAgentRepository
import com.social.flare.features.feed.domain.repository.StoryRepository
import java.io.File
import javax.inject.Inject

class GenerateAutonomousStoryUseCase @Inject constructor(
    private val aiRepository: AiAgentRepository,
    private val storyRepository: StoryRepository
) {
    suspend fun execute(persona: AiPersona, contextTopic: String): Result<Unit> {
        return try {
            Log.d("AiBot", "📸 Preparando historia para: $contextTopic")

            val storyPromptModifier =
                "Vertical 9:16 aspect ratio, casual smartphone photo, instagram story style, " +
                        "first-person POV, raw unedited look, photorealistic. " +
                        "Context based on: $contextTopic"

            val promptResult = aiRepository.generateVisualPrompt(persona, storyPromptModifier)
            val visualPrompt =
                promptResult.getOrDefault("A vertical casual smartphone photo, instagram story style")

            val imageResult = aiRepository.generateLocalImageUri(visualPrompt)

            if (imageResult.isSuccess) {
                val localUri = imageResult.getOrNull()!!
                Log.d("AiBot", "🖼️ Foto generada en galería temporal: $localUri")
                storyRepository.createStory(persona.citizenId, localUri)

                Log.d("AiBot", "✅ Historia publicada con éxito por ${persona.username}")

                localUri.path?.let { File(it).delete() }

                Result.success(Unit)
            } else {
                Log.e("AiBot", "⚠️ Falló la generación de la imagen para la historia.")
                Result.failure(Exception("Image generation failed"))
            }
        } catch (e: Exception) {
            Log.e("AiBot", "❌ Error en GenerateAutonomousStoryUseCase: ${e.message}")
            Result.failure(e)
        }
    }
}