package com.social.flare.features.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.feed.domain.repository.FeedRepository
import com.social.flare.features.feed.domain.usecase.GetFeedUseCase
import com.social.flare.features.post.domain.usecase.DeletePostUseCase
import com.social.flare.features.post.domain.usecase.UpdatePostUseCase
import com.social.flare.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModel(
    private val getFeedUseCase: GetFeedUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val updatePostUseCase: UpdatePostUseCase,
    private val repository: FeedRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState = _uiState.asStateFlow()
    private var currentUserId: String? = null

    fun loadFeed(activeUserId: String) {
        currentUserId = activeUserId

        viewModelScope.launch {
            profileRepository.getCitizenProfile(activeUserId).collect { user ->
                _uiState.update { it.copy(activeUser = user) }
            }
        }
        viewModelScope.launch {
            getFeedUseCase(activeUserId).collect { posts ->
                _uiState.update { it.copy(posts = posts, isLoading = false) }
            }
        }
    }

    fun onEvent(event: FeedEvent) {
        when (event) {
            is FeedEvent.OnLikeClick -> handleLike(event.postId)
            is FeedEvent.OnDeletePost -> deletePost(event.postId)
            is FeedEvent.OnEditPost -> editPost(event.postId, event.newContent)
            is FeedEvent.OnRefresh -> { /* Flow es reactivo */ }
            is FeedEvent.OnShareClick -> { /* Lógica de compartir */ }
            is FeedEvent.OnCommentClick -> { /* Lógica de comentarios */ }
            is FeedEvent.OnSaveClick -> { /* Lógica de guardado */ }
            is FeedEvent.OnPostClick -> { }
        }
    }

    private fun handleLike(postId: String) {
        val userId = currentUserId ?: return
        val post = _uiState.value.posts.find { it.id == postId } ?: return

        viewModelScope.launch {
            repository.toggleLike(
                postId = post.id,
                citizenId = userId,
                isCurrentlyLiked = post.isLikedByMe
            )
        }
    }

    private fun deletePost(postId: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            val result = deletePostUseCase(postId, userId)
            if (result.isFailure) {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message) }
            }
        }
    }

    private fun editPost(postId: String, newContent: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            val result = updatePostUseCase(postId, userId, newContent)
            if (result.isFailure) {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}