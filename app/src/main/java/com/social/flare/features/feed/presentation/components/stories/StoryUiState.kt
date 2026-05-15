package com.social.flare.features.feed.presentation.components.stories

data class StoryUiState(
    val isUploading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)