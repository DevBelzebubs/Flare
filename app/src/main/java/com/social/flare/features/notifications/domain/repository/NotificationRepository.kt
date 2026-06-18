package com.social.flare.features.notifications.domain.repository

import com.social.flare.features.notifications.domain.model.FlareNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<List<FlareNotification>>
    fun getUnreadCount(userId: String): Flow<Int>
    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsRead(userId: String)
    fun connectToRealtimeNotifications(userId: String, scope: CoroutineScope)
    fun disconnectFromRealtimeNotifications()
}