package com.social.flare.features.post.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.post.domain.usecase.CreatePostUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddPostViewModel(
    private val createPostUseCase: CreatePostUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPostUiState())
    val uiState: StateFlow<AddPostUiState> = _uiState.asStateFlow()

    fun createPost(authorId: String, content: String, mediaUris: List<Uri>, parentPostId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null) }

            val result = runCatching {
                createPostUseCase(
                    authorId = authorId,
                    content = content,
                    mediaUris = mediaUris,
                    parentPostId = parentPostId
                )
            }

            if (result.isSuccess) {
                _uiState.update { it.copy(isUploading = false, isSuccess = true) }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Error desconocido al publicar"
                _uiState.update { it.copy(isUploading = false, errorMessage = errorMsg) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearState() {
        _uiState.update { AddPostUiState() }
    }
}