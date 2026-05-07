package com.social.flare.features.feed.presentation

import com.social.flare.features.feed.domain.model.Post

data class FeedUiState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val error: String? = null
)