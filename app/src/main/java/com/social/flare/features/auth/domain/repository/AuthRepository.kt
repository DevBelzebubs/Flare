package com.social.flare.features.auth.domain.repository

interface AuthRepository {
    suspend fun login(email: String, pass: String): Result<String>
    suspend fun register(
        displayName: String,
        username: String,
        email: String,
        pass: String
    ): Result<String>

    suspend fun logout()
    suspend fun changePassword(newPassword: String): Result<Unit>
    // suspend fun resetPassword(email: String)
}
