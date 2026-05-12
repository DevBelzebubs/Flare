package com.social.flare.features.profile.presentation

import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.feed.domain.model.Post
import kotlinx.coroutines.flow.Flow

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(
        val citizen: Flow<CitizenEntity?>,
        val postsCount: Int = 0,
        val followersCount: Int = 0,
        val followingCount: Int = 0,
        val posts: List<Post> = emptyList()
    ) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    object UserNotFound : ProfileUiState
}