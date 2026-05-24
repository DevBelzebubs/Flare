package com.social.flare.features.post.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.feed.domain.repository.FeedRepository
import com.social.flare.features.post.domain.model.PostDetailUiState
import com.social.flare.features.post.domain.usecase.CreatePostUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PostDetailViewModel(
    private val repository: FeedRepository,
    private val createPostUseCase: CreatePostUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()
    private var detailJob: Job? = null

    fun loadPostDetail(postId: String, activeUserId: String) {
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.getPostDetail(postId, activeUserId).collect { detail ->
                    _uiState.update {
                        it.copy(isLoading = false, postDetail = detail)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun toggleLike(postId: String, activeUserId: String) {
        viewModelScope.launch {
            val currentDetail = _uiState.value.postDetail ?: return@launch

            val isCurrentlyLiked = if (currentDetail.mainPost.id == postId) {
                currentDetail.mainPost.isLikedByMe
            } else {
                currentDetail.replies.find { it.id == postId }?.isLikedByMe ?: false
            }

            val result = repository.toggleLike(
                postId = postId,
                citizenId = activeUserId,
                isCurrentlyLiked = isCurrentlyLiked
            )

            if (result.isFailure) {
                val errorMsg = result.exceptionOrNull()?.message ?: "Error desconocido"
                _uiState.update { it.copy(errorMessage = "Error al actualizar el like: $errorMsg") }
            }
        }
    }
    fun toggleSave(postId: String, activeUserId: String) {
        viewModelScope.launch {
            val currentDetail = _uiState.value.postDetail ?: return@launch
            val isCurrentlySaved = currentDetail.mainPost.isSavedByMe
            repository.toggleSavePost(postId, activeUserId, isCurrentlySaved)
        }
    }

    fun sharePost(authorId: String, originalPostId: String) {
        viewModelScope.launch {
            repository.sharePost(authorId, originalPostId)
        }
    }

    fun updatePost(postId: String, currentUserId: String, newContent: String) {
        viewModelScope.launch {
            val result = repository.updatePost(postId, currentUserId, newContent)
            if (result.isFailure) {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun deletePost(postId: String, currentUserId: String) {
        viewModelScope.launch {
            val result = repository.deletePost(postId, currentUserId)
            if (result.isFailure) {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun createReply(authorId: String, content: String, parentPostId: String, mediaUris: List<Uri> = emptyList()) {
        viewModelScope.launch {
            val result = runCatching {
                createPostUseCase(
                    authorId = authorId,
                    content = content,
                    mediaUris = mediaUris,
                    parentPostId = parentPostId
                )
            }

            if (result.isFailure) {
                val errorMsg = result.exceptionOrNull()?.message ?: "Error al publicar respuesta"
                _uiState.update { it.copy(errorMessage = errorMsg) }
            }
        }
    }
}