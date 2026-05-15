package com.social.flare.features.feed.presentation

import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.feed.data.local.entity.StoryWithAuthor
import com.social.flare.features.feed.domain.model.Post

data class FeedUiState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val error: String? = null,
    val isGuest : Boolean = true,
    val activeUser: CitizenEntity? = null,
    val stories: List<StoryWithAuthor> = emptyList()
)