package com.social.flare.features.admin.domain.usecase

import android.util.Log
import com.social.flare.features.admin.domain.repository.AdminRepository
import com.social.flare.features.ai.domain.model.AiPersona
import com.social.flare.features.ai.domain.repository.AiAgentRepository
import javax.inject.Inject

class CreateAiProfileUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
    private val aiRepository: AiAgentRepository
) {
    suspend fun execute(
        username: String,
        displayName: String,
        systemPrompt: String,
        temperature: Double
    ): Result<Unit> {
        if (username.isBlank() || systemPrompt.isBlank()) {
            return Result.failure(IllegalArgumentException("El usuario y el prompt son obligatorios"))
        }
        val newPersona = AiPersona(
            citizenId = "bot_${System.currentTimeMillis()}",
            username = username,
            displayName = displayName,
            systemPrompt = systemPrompt,
            temperature = temperature
        )
        val createResult = adminRepository.createAiPersona(newPersona)
        return if (createResult.isSuccess) {
            Log.d("AiBot", "Perfil base creado: ${newPersona.citizenId}. Iniciando generación de avatar...")

            // Construimos un contexto fuerte para que la IA entienda que es un Headshot/Avatar
            val avatarPromptModifier = "A professional close-up studio headshot profile picture of a person, " +
                    "clear facial features, centered portrait, avatar icon, minimalist background, photorealistic, 8k resolution. " +
                    "Context based on: $systemPrompt"

            val visualPromptResult = aiRepository.generateVisualPrompt(
                persona = newPersona,
                topicContext = avatarPromptModifier
            )

            val finalPrompt = visualPromptResult.getOrDefault(
                "A clean, modern professional avatar portrait icon, flat background"
            )
            Log.d("AiBot", "📝 Prompt visual del avatar: $finalPrompt")

            val imageResult = aiRepository.generateAndUploadImage(finalPrompt)

            if (imageResult.isSuccess) {
                val cloudinaryAvatarUrl = imageResult.getOrNull()!!
                Log.d("AiBot", "Avatar generado y subido: $cloudinaryAvatarUrl")

                adminRepository.updateBotAvatar(newPersona.citizenId, cloudinaryAvatarUrl)
            } else {
                Log.e("AiBot", "Falló la generación de avatar. Se usará el avatar por defecto. Error: ${imageResult.exceptionOrNull()?.message}")
            }

            Result.success(Unit)
        } else {
            Log.e("AiBot", "Error al crear el perfil base.")
            createResult
        }
    }
}