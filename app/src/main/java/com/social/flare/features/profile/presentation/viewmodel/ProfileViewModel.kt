package com.social.flare.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.feed.data.local.dao.PostDao
import com.social.flare.features.feed.data.local.dao.PostWithDetails
import com.social.flare.features.feed.data.mapper.toDomain
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.post.domain.usecase.GetUserPostsUseCase
import com.social.flare.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val getUserPostsUseCase: GetUserPostsUseCase,
    private val postDao: PostDao
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadActiveUserProfile(citizenId: String) {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val citizen = repository.getCitizenProfile(citizenId)
                if (citizen != null) {
                    kotlinx.coroutines.flow.combine(
                        getUserPostsUseCase(citizenId),
                        postDao.getSavedPosts(citizenId)
                    ) { myPosts: List<Post>, savedPostsDetails: List<PostWithDetails> ->
                        val savedPosts = savedPostsDetails.map { it.toDomain() }
                        ProfileUiState.Success(
                            citizen = citizen,
                            postsCount = myPosts.size,
                            followersCount = 0,
                            followingCount = 0,
                            myPosts = myPosts,
                            savedPosts = savedPosts
                        )
                    }.collect { state ->
                        _uiState.value = state
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