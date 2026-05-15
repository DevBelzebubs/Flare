package com.social.flare.features.feed.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.feed.domain.repository.StoryRepository
import com.social.flare.features.feed.presentation.components.stories.StoryUiState
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
}