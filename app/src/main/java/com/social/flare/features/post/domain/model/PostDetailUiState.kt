package com.social.flare.features.post.domain.model

data class PostDetailUiState(
    val isLoading: Boolean = true,
    val postDetail: PostDetail? = null,
    val errorMessage: String? = null,
    val isReplying: Boolean = false,
    val isEditing: Boolean = false,
    val isDeleting: Boolean = false
)
