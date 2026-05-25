package com.social.flare.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.feed.data.local.dao.PostDao
import com.social.flare.features.feed.data.local.dao.PostWithDetails
import com.social.flare.features.feed.data.mapper.toDomain
import com.social.flare.features.feed.domain.model.Post

import com.social.flare.features.post.domain.usecase.GetUserPostsUseCase
import com.social.flare.features.profile.domain.model.FollowStats
import com.social.flare.features.profile.domain.repository.ProfileRepository
import com.social.flare.features.profile.domain.usecase.GetFollowStatsUseCase
import com.social.flare.features.profile.domain.usecase.ToggleFollowUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val getUserPostsUseCase: GetUserPostsUseCase,
    private val postDao: PostDao,
    private val toggleFollowUseCase: ToggleFollowUseCase,
    private val getFollowStatsUseCase: GetFollowStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // --- NUEVO: ESTADO INDEPENDIENTE PARA SEGUIDORES ---
    // Lo exponemos separado para que el botón de la UI cambie inmediatamente
    private val _followStats = MutableStateFlow(FollowStats(0, 0, false))
    val followStats: StateFlow<FollowStats> = _followStats.asStateFlow()

    private var loadJob: Job? = null

    // Renombrado a loadProfileData para reflejar que carga CUALQUIER perfil
    fun loadProfileData(targetCitizenId: String, currentCitizenId: String?) {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                // 1. Escuchar los Stats de seguimiento en tiempo real
                launch {
                    getFollowStatsUseCase(
                        targetUserId = targetCitizenId,
                        currentUserId = currentCitizenId ?: ""
                    ).collect { stats ->
                        _followStats.value = stats
                    }
                }

                // 2. Cargar el ciudadano y combinar con Posts y Stats
                val citizenFlow = repository.getCitizenProfile(targetCitizenId)

                combine(
                    citizenFlow,
                    getUserPostsUseCase(targetCitizenId),
                    postDao.getSavedPosts(targetCitizenId),
                    _followStats
                ) { citizen, myPosts, savedPostsDetails, stats ->
                    if (citizen == null) {
                        ProfileUiState.UserNotFound
                    } else {
                        val savedPosts = savedPostsDetails.map { it.toDomain() }
                        ProfileUiState.Success(
                            citizen = citizen,
                            postsCount = myPosts.size,
                            followersCount = stats.followersCount,
                            followingCount = stats.followingCount,
                            myPosts = myPosts,
                            savedPosts = savedPosts
                        )
                    }
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // --- NUEVO: ACCIÓN DE SEGUIR / DEJAR DE SEGUIR ---
    fun toggleFollow(followerId: String, followedId: String) {
        viewModelScope.launch {
            toggleFollowUseCase(
                followerId = followerId,
                followedId = followedId,
                isCurrentlyFollowing = _followStats.value.isFollowingByMe
            )
        }
    }
    fun loadActiveUserProfile(citizenId: String) {
        loadProfileData(targetCitizenId = citizenId, currentCitizenId = citizenId)
    }
}