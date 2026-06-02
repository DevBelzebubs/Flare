package com.social.flare.features.auth.domain.usecase

import com.social.flare.features.auth.domain.repository.AuthRepository

class ChangePasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(newPassword: String): Result<Unit> {
        return authRepository.changePassword(newPassword)
    }
}
