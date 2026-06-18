package com.social.flare.features.auth.domain.usecase

import com.social.flare.features.auth.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke (username: String, pass: String): Result<String> {
        if (username.isBlank() || pass.isBlank()){
            return Result.failure(Exception("Los campos no pueden estar vacíos"))
        }
        if (pass.length < 5) {
            return Result.failure(Exception("La contraseña debe tener al menos 5 caracteres"))
        }
        return authRepository.login(username,pass);
    }
}