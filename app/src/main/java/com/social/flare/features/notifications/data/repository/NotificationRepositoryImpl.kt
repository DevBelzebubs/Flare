package com.social.flare.features.notifications.data.repository

import android.util.Log
import com.social.flare.features.notifications.data.local.dao.NotificationDao
import com.social.flare.features.notifications.data.local.entity.NotificationEntity
import com.social.flare.features.notifications.data.mapper.toDomain
import com.social.flare.features.notifications.data.remote.NotificationWebSocketClient
import com.social.flare.features.notifications.domain.model.FlareNotification
import com.social.flare.features.notifications.domain.repository.NotificationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.postgrest.query.filter.FilterOperator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class NotificationRepositoryImpl(
    private val notificationDao: NotificationDao,
    private val supabase: SupabaseClient,
    okHttpClient: OkHttpClient
) : NotificationRepository {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val webSocketClient = NotificationWebSocketClient(okHttpClient) { json ->
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

        handleIncomingNotification(userId)
    }

    override fun disconnectFromRealtimeNotifications() {
        webSocketClient.disconnect()
    }

    private fun handleIncomingNotification(userId: String) {
        coroutineScope.launch {
            try {
                supabase.realtime.connect()

                val realtimeChannel = supabase.channel("notifications-user-$userId")

                realtimeChannel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "notification_table"
                    filter("recipientId", FilterOperator.EQ, userId)
                }.onEach { action ->
                    val newNotification = action.decodeRecord<NotificationEntity>()
                    Log.d("FLARE_SYNC", "Notificación recibida: ${newNotification.type}")
                    notificationDao.insertNotification(newNotification)
                }.launchIn(coroutineScope)

                realtimeChannel.subscribe()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
