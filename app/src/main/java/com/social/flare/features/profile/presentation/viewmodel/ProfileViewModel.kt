package com.social.flare.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.post.domain.usecase.GetUserPostsUseCase
import com.social.flare.features.profile.domain.repository.ProfileRepository
import com.social.flare.features.profile.presentation.viewmodel.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val getUserPostsUseCase: GetUserPostsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    fun loadActiveUserProfile(citizenId: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val citizen = repository.getCitizenProfile(citizenId)
                if (citizen != null) {
                    getUserPostsUseCase(citizenId).collect { userPosts ->
                        _uiState.value = ProfileUiState.Success(
                            citizen = citizen,
                            postsCount = userPosts.size,
                            followersCount = 0,
                            followingCount = 0,
                            posts = userPosts
                        )
                    }
                } else {
                    _uiState.value = ProfileUiState.UserNotFound
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}