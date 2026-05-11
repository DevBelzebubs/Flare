package com.social.flare.features.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.feed.domain.repository.FeedRepository
import com.social.flare.features.feed.domain.usecase.GetFeedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModel(
    private val getFeedUseCase: GetFeedUseCase,
    private val repository: FeedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    fun loadFeed(activeUserId: String) {
        currentUserId = activeUserId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getFeedUseCase(activeUserId).collect { databasePosts ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        posts = databasePosts
                    )
                }
            }
        }
    }

    fun onEvent(event: FeedEvent) {
        when (event) {
            is FeedEvent.OnLikeClick -> {
                val userId = currentUserId ?: return

                val post = _uiState.value.posts.find { it.id == event.postId } ?: return

                viewModelScope.launch {
                    repository.toggleLike(
                        postId = post.id,
                        citizenId = userId,
                        isCurrentlyLiked = post.isLikedByMe
                    )
                }
            }
            is FeedEvent.OnRefresh -> { /* Flow es reactivo, ya no se ocupa */ }
            is FeedEvent.OnShareClick -> { /* Lógica de compartir */ }
            else -> {}
        }
    }
}