package com.social.flare.features.auth.domain.usecase

import com.social.flare.features.auth.domain.repository.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        displayName: String,
        username:String,
        email:String,
        pass:String
    ): Result<String> {
        if (displayName.isBlank() || username.isBlank() || pass.isBlank()) {
            return Result.failure(Exception("Debes completar todos los campos obligatorios"));
        }
        if (pass.length < 5) {
            return Result.failure(Exception("La contraseña debe tener al menos 5 caracteres"))
        }
        if (!username.startsWith("@")) {
            return Result.failure(Exception("El nombre de usuario debe empezar con @"));
        }
        return authRepository.register(displayName,username,email,pass)
    }
}