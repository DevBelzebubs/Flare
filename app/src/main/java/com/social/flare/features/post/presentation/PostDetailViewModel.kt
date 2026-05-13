package com.social.flare.features.post.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.feed.domain.repository.FeedRepository
import com.social.flare.features.post.domain.model.PostDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PostDetailViewModel(
    private val repository: FeedRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()
    fun loadPostDetail(postId: String, activeUserId: String) {
        viewModelScope.launch {
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
}