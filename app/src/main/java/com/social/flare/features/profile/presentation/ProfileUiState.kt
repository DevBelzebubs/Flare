package com.social.flare.features.profile.presentation

import com.social.flare.features.auth.data.local.entity.CitizenEntity

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(
        val citizen: CitizenEntity,
        val postsCount: Int = 0,
        val followersCount: Int = 0,
        val followingCount: Int = 0,
        // val postsGrid: List<PostEntity> = emptyList()
    ) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    object UserNotFound : ProfileUiState
}