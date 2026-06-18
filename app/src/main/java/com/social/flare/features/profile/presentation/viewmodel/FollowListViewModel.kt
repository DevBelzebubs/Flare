package com.social.flare.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.profile.domain.repository.FollowRepository
import com.social.flare.features.profile.domain.usecase.ToggleFollowUseCase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FollowListViewModel(
    private val followRepository: FollowRepository,
    private val toggleFollowUseCase: ToggleFollowUseCase
) : ViewModel() {

    private val _users = MutableStateFlow<List<CitizenEntity>>(emptyList())
    val users: StateFlow<List<CitizenEntity>> = _users.asStateFlow()

    private val _followedIds = MutableStateFlow<Set<String>>(emptySet())
    val followedIds: StateFlow<Set<String>> = _followedIds.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentUserId: String? = null
    private var loadJob: Job? = null

    fun load(userId: String, type: String, activeCitizenId: String?) {
        currentUserId = activeCitizenId
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            val result = if (type == "followers") {
                followRepository.getFollowers(userId)
            } else {
                followRepository.getFollowing(userId)
            }
            _users.value = result
            _isLoading.value = false
            if (activeCitizenId != null) {
                _followedIds.value = followRepository.getFollowedIds(activeCitizenId).toSet()
            }
        }
    }

    fun toggleFollow(followedId: String) {
        val followerId = currentUserId ?: return
        viewModelScope.launch {
            val isCurrentlyFollowing = followedId in _followedIds.value
            toggleFollowUseCase(followerId, followedId, isCurrentlyFollowing)
            _followedIds.update {
                if (isCurrentlyFollowing) it - followedId else it + followedId
            }
        }
    }

    fun getFollowedIds(): Set<String> = _followedIds.value
}
