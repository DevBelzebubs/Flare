package com.social.flare.features.notifications.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.notifications.domain.usecase.GetNotificationsUseCase
import com.social.flare.features.notifications.domain.usecase.GetSuggestedAccountsUseCase
import com.social.flare.features.notifications.domain.usecase.ManageRealtimeNotificationsUseCase
import com.social.flare.features.notifications.domain.usecase.MarkNotificationReadUseCase
import com.social.flare.features.profile.domain.usecase.ToggleFollowUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val manageRealtimeNotificationsUseCase: ManageRealtimeNotificationsUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
    private val toggleFollowUseCase: ToggleFollowUseCase,
    private val getSuggestedAccountsUseCase: GetSuggestedAccountsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private var activeUserId: String? = null

    fun initialize(userId: String) {
        if (activeUserId == userId) return
        activeUserId = userId

        viewModelScope.launch {
            getNotificationsUseCase(userId).collect { notifications ->
                _uiState.update {
                    it.copy(notifications = notifications, isLoading = false)
                }
            }
        }
        loadSuggestedAccounts()
    }

    fun loadSuggestedAccounts() {
        val userId = activeUserId ?: return
        viewModelScope.launch {
            val accounts = getSuggestedAccountsUseCase(userId)
            _uiState.update { it.copy(suggestedAccounts = accounts) }
        }
    }

    fun followSuggested(followedId: String) {
        val followerId = activeUserId ?: return
        viewModelScope.launch {
            val isCurrentlyFollowing = followedId in _uiState.value.suggestedFollowedIds
            toggleFollowUseCase(followerId, followedId, isCurrentlyFollowing)
            _uiState.update {
                val updated = if (isCurrentlyFollowing) it.suggestedFollowedIds - followedId
                else it.suggestedFollowedIds + followedId
                it.copy(suggestedFollowedIds = updated)
            }
        }
    }


    fun onNotificationClick(notificationId: String) {
        viewModelScope.launch {
            markNotificationReadUseCase(notificationId)
        }
    }

    fun toggleFollowBack(followedId: String, isCurrentlyFollowing: Boolean) {
        val followerId = activeUserId ?: return
        viewModelScope.launch {
            toggleFollowUseCase(followerId, followedId, isCurrentlyFollowing)
        }
    }

    override fun onCleared() {
        manageRealtimeNotificationsUseCase.disconnect()
        super.onCleared()
    }

}