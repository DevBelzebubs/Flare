package com.social.flare.features.post.presentation

data class AddPostUiState(
    val isUploading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)