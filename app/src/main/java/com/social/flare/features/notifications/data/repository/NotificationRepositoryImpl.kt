package com.social.flare.features.notifications.data.repository

import com.social.flare.features.notifications.data.local.dao.NotificationDao
import com.social.flare.features.notifications.data.local.entity.NotificationEntity
import com.social.flare.features.notifications.data.mapper.toDomain
import com.social.flare.features.notifications.data.remote.NotificationWebSocketClient
import com.social.flare.features.notifications.domain.model.FlareNotification
import com.social.flare.features.notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class NotificationRepositoryImpl(
    private val notificationDao: NotificationDao,
    okHttpClient: OkHttpClient
) : NotificationRepository {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val webSocketClient = NotificationWebSocketClient(okHttpClient) { json ->
        handleIncomingNotification(json)
    }

    override fun getNotifications(userId: String): Flow<List<FlareNotification>> {
        return notificationDao.getNotifications(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUnreadCount(userId: String): Flow<Int> {
        return notificationDao.getUnreadCount(userId)
    }

    override suspend fun markAsRead(notificationId: String) {
        notificationDao.markAsRead(notificationId)
    }

    override suspend fun markAllAsRead(userId: String) {
        notificationDao.markAllAsRead(userId)
    }

    override fun connectToRealtimeNotifications(userId: String) {
        webSocketClient.connect(userId)
    }

    override fun disconnectFromRealtimeNotifications() {
        webSocketClient.disconnect()
    }
    private fun handleIncomingNotification(json: org.json.JSONObject) {
        coroutineScope.launch {
            try {
                val entity = NotificationEntity(
                    id = json.getString("id"),
                    recipientId = json.getString("recipientId"),
                    actorId = json.getString("actorId"),
                    actorUsername = json.getString("actorUsername"),
                    actorAvatarUrl = json.optString("actorAvatarUrl").takeIf { it.isNotEmpty() },
                    type = json.getString("type"), // LIKE, COMMENT, FOLLOW
                    referencedPostId = json.optString("referencedPostId").takeIf { it.isNotEmpty() },
                    referencedPostMediaUrl = json.optString("referencedPostMediaUrl").takeIf { it.isNotEmpty() },
                    extraText = json.optString("extraText").takeIf { it.isNotEmpty() },
                    createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                    isRead = false
                )
                notificationDao.insertNotification(entity)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}