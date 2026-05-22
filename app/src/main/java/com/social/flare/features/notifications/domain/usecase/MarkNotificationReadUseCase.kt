package com.social.flare.features.notifications.domain.usecase

import com.social.flare.features.notifications.domain.repository.NotificationRepository

class MarkNotificationReadUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: String) {
        repository.markAsRead(notificationId)
    }
}