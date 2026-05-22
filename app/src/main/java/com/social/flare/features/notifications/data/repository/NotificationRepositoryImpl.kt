package com.social.flare.features.notifications.data.repository

import android.util.Log
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
    private val supabase: SupabaseClient
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
                supabase.realtime.connect()
                realtimeChannel = supabase.channel("notifications-user-$userId")

                realtimeChannel!!.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "notification_table"
                    filter = "recipientId=eq.$userId"
                }.onEach { action ->
                    val newNotification = action.decodeRecord<NotificationEntity>()

                    Log.d("FLARE_SYNC", "Notificación recibida: ${newNotification.type}")

                    notificationDao.insertNotification(newNotification)
                }.launchIn(coroutineScope)

                realtimeChannel!!.subscribe()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}