package com.social.flare.features.notifications.presentation.viewmodel

import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.notifications.domain.model.FlareNotification

data class NotificationUiState(
    val isLoading: Boolean = true,
    val notifications: List<FlareNotification> = emptyList(),
    val suggestedAccounts: List<CitizenEntity> = emptyList(),
    val error: String? = null,
    val suggestedFollowedIds: Set<String> = emptySet(),
    val notificationFollowedActorIds: Set<String> = emptySet()
)