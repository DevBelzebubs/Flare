package com.social.flare.features.notifications.domain.usecase

import com.social.flare.features.notifications.domain.repository.NotificationRepository

class ManageRealtimeNotificationsUseCase(
    private val repository: NotificationRepository
) {
    fun connect(userId: String) {
        repository.connectToRealtimeNotifications(userId)
    }

    fun disconnect() {
        repository.disconnectFromRealtimeNotifications()
    }
}