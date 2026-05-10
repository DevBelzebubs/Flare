package com.social.flare.features.auth.domain.usecase

import com.social.flare.features.auth.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke (username: String, pass: String): Result<String> {
        if (username.isBlank() || pass.isBlank()){
            return Result.failure(Exception("Los campos no pueden estar vacíos"))
        }
        return authRepository.login(username,pass);
    }
}