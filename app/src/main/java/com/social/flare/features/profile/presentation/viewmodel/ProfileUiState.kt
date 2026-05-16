package com.social.flare.features.profile.presentation.viewmodel

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
        val myPosts: List<Post> = emptyList(),
        val savedPosts: List<Post> = emptyList()
    ) : ProfileUiState

    data class Error(val message: String) : ProfileUiState
    object UserNotFound : ProfileUiState
}