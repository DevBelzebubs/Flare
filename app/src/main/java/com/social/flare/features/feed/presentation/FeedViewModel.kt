package com.social.flare.features.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.feed.domain.repository.FeedRepository
import com.social.flare.features.feed.domain.repository.StoryRepository
import com.social.flare.features.feed.domain.usecase.GetFeedUseCase
import com.social.flare.features.post.domain.usecase.DeletePostUseCase
import com.social.flare.features.post.domain.usecase.UpdatePostUseCase
import com.social.flare.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch

class FeedViewModel(
    private val getFeedUseCase: GetFeedUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val updatePostUseCase: UpdatePostUseCase,
    private val repository: FeedRepository,
    private val profileRepository: ProfileRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState = _uiState.asStateFlow()
    private var currentUserId: String? = null

    // Variables para rastrear y cancelar las corrutinas
    private var userJob: Job? = null
    private var feedJob: Job? = null
    private var storyJob: Job? = null

    fun loadFeed(activeUserId: String) {
        currentUserId = activeUserId
        cancelJobs()

        userJob = viewModelScope.launch {
            profileRepository.getCitizenProfile(activeUserId).collect { user ->
                _uiState.update { it.copy(activeUser = user) }
            }
        }

        feedJob = viewModelScope.launch {
            getFeedUseCase(activeUserId)
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { posts ->
                    _uiState.update { it.copy(posts = posts, isLoading = false) }
                }
        }

        storyJob = viewModelScope.launch {
            storyRepository.getActiveStories(activeUserId).collect { stories ->
                _uiState.update { it.copy(stories = stories) }
            }
        }
    }

    fun loadFeedGuest() {
        currentUserId = null
        cancelJobs()

        _uiState.update { it.copy(activeUser = null) }

        feedJob = viewModelScope.launch {
            repository.getFeedPostsGuest()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { posts ->
                    _uiState.update { it.copy(posts = posts, isLoading = false) }
                }
        }
    }

    private fun cancelJobs() {
        userJob?.cancel()
        feedJob?.cancel()
        storyJob?.cancel()
    }

    fun onEvent(event: FeedEvent) {
        when (event) {
            is FeedEvent.OnLikeClick -> handleLike(event.postId)
            is FeedEvent.OnDeletePost -> deletePost(event.postId)
            is FeedEvent.OnEditPost -> editPost(event.postId, event.newContent)
            is FeedEvent.OnRefresh -> { }
            is FeedEvent.OnShareClick -> handleShare(event.postId)
            is FeedEvent.OnCommentClick -> { }
            is FeedEvent.OnSaveClick -> handleSave(event.postId)
            is FeedEvent.OnPostClick -> { }
            is FeedEvent.OnAuthorClick -> {}
        }
    }

    private fun handleShare(postId: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            repository.sharePost(authorId = userId, originalPostId = postId)
            _uiState.update { it.copy(error = null) }
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
    private fun handleSave(postId: String) {
        val userId = currentUserId ?: return
        val post = _uiState.value.posts.find { it.id == postId } ?: return
        viewModelScope.launch {
            repository.toggleSavePost(
                postId = post.id,
                citizenId = userId,
                isCurrentlySaved = post.isSavedByMe
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