package com.social.flare.features.post.domain.model
data class PostDetailUiState(
    val isLoading: Boolean = true,
    val postDetail: PostDetail? = null,
    val errorMessage: String? = null
)