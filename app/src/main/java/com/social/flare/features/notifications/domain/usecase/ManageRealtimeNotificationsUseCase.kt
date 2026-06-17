package com.social.flare.features.notifications.domain.usecase

import com.social.flare.features.notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope

class ManageRealtimeNotificationsUseCase(
    private val repository: NotificationRepository
) {
    fun connect(userId: String, scope: CoroutineScope) {
        repository.connectToRealtimeNotifications(userId, scope)
    }

    fun disconnect() {
        repository.disconnectFromRealtimeNotifications()
    }
}