package com.social.flare.features.notifications.domain.usecase

import com.social.flare.features.notifications.domain.model.FlareNotification
import com.social.flare.features.notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationsUseCase(
    private val repository: NotificationRepository
) {
    operator fun invoke(userId: String): Flow<List<FlareNotification>> {
        return repository.getNotifications(userId)
    }
}