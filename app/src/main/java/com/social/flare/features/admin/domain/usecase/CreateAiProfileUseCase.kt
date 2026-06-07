package com.social.flare.features.admin.domain.usecase

import com.social.flare.features.admin.domain.repository.AdminRepository
import com.social.flare.features.ai.domain.model.AiPersona
import javax.inject.Inject

class CreateAiProfileUseCase @Inject constructor(
    private val adminRepository: AdminRepository
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

        return adminRepository.createAiPersona(newPersona)
    }
}