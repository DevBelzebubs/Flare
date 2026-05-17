package com.social.flare.features.feed.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.feed.domain.model.StoryCommentWithAuthor
import com.social.flare.features.feed.domain.model.StoryComment
import com.social.flare.features.feed.domain.repository.StoryRepository
import com.social.flare.features.feed.presentation.components.stories.StoryUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StoryViewModel(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryUiState())
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()

    private val _comments = MutableStateFlow<List<StoryComment>>(emptyList())
    val comments: StateFlow<List<StoryComment>> = _comments.asStateFlow()

    private var commentsJob: Job? = null

    fun createStory(authorId: String, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null, isSuccess = false) }
            val result = storyRepository.createStory(authorId, imageUri)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isUploading = false, isSuccess = true) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isUploading = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    fun loadCommentsForStory(storyId: String) {
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            storyRepository.getStoryComments(storyId).collect { commentList ->
                _comments.value = commentList
            }
        }
    }

    fun addComment(storyId: String, authorId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            storyRepository.addCommentToStory(storyId, authorId, content)
        }
    }
    fun markStoryAsViewed(storyId: String, citizenId: String) {
        viewModelScope.launch {
            storyRepository.markStoryAsViewed(storyId, citizenId)
        }
    }
    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            storyRepository.deleteStory(storyId)
        }
    }
}