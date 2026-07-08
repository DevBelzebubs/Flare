package com.social.flare.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.feed.data.local.dao.PostDao
import com.social.flare.features.feed.data.mapper.toDomain
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.feed.domain.repository.FeedRepository
import com.social.flare.features.post.domain.usecase.GetUserPostsUseCase
import com.social.flare.features.profile.domain.model.FollowStats
import com.social.flare.features.profile.domain.repository.ProfileRepository
import com.social.flare.features.profile.domain.usecase.GetFollowStatsUseCase
import com.social.flare.features.profile.domain.usecase.ToggleFollowUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val getUserPostsUseCase: GetUserPostsUseCase,
    private val postDao: PostDao,
    private val feedRepository: FeedRepository,
    private val toggleFollowUseCase: ToggleFollowUseCase,
    private val getFollowStatsUseCase: GetFollowStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _followStats = MutableStateFlow(FollowStats(0, 0, false))
    val followStats: StateFlow<FollowStats> = _followStats.asStateFlow()
    private val _isFollowingLoading = MutableStateFlow(false)
    val isFollowingLoading: StateFlow<Boolean> = _isFollowingLoading.asStateFlow()

    private var loadJob: Job? = null
    private var followStatsJob: Job? = null

    fun loadProfileData(targetCitizenId: String, currentCitizenId: String?) {
        loadJob?.cancel()
        followStatsJob?.cancel()

        _uiState.value = ProfileUiState.Loading

        followStatsJob = viewModelScope.launch {
            try {
                getFollowStatsUseCase(
                    targetUserId = targetCitizenId,
                    currentUserId = currentCitizenId ?: ""
                ).collect { stats ->
                    _followStats.value = stats
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
            }
        }

        loadJob = viewModelScope.launch {
            try {
                val citizenFlow = repository.getCitizenProfile(targetCitizenId)

                combine(
                    citizenFlow,
                    getUserPostsUseCase(targetCitizenId),
                    postDao.getSavedPosts(targetCitizenId),
                    feedRepository.getSharedPosts(targetCitizenId),
                    _followStats
                ) { citizen, myPosts, savedPostsDetails, sharedPosts, _ ->
                    if (citizen == null) {
                        ProfileUiState.UserNotFound
                    } else {
                        val savedPosts = savedPostsDetails.map { it.toDomain() }
                        ProfileUiState.Success(
                            citizen = citizen,
                            postsCount = myPosts.size,
                            myPosts = myPosts,
                            savedPosts = savedPosts,
                            sharedPosts = sharedPosts
                        )
                    }
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.value = ProfileUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun toggleFollow(followerId: String, followedId: String) {
        viewModelScope.launch {
            _isFollowingLoading.value = true
            val wasFollowing = _followStats.value.isFollowingByMe
            toggleFollowUseCase(
                followerId = followerId,
                followedId = followedId,
                isCurrentlyFollowing = wasFollowing
            )
            _followStats.update { currentStats ->
                if (wasFollowing) {
                    currentStats.copy(
                        isFollowingByMe = false,
                        followersCount = (currentStats.followersCount - 1).coerceAtLeast(0)
                    )
                } else {
                    currentStats.copy(
                        isFollowingByMe = true,
                        followersCount = currentStats.followersCount + 1
                    )
                }
            }
            _isFollowingLoading.value = false
        }
    }
    fun loadActiveUserProfile(citizenId: String) {
        loadProfileData(targetCitizenId = citizenId, currentCitizenId = citizenId)
    }

    override fun onCleared() {
        super.onCleared()
        loadJob?.cancel()
        followStatsJob?.cancel()
    }
}