package com.social.flare.features.notifications.data.repository

import android.util.Log
import com.social.flare.features.notifications.data.local.dao.NotificationDao
import com.social.flare.features.notifications.data.local.entity.NotificationEntity
import com.social.flare.features.notifications.data.mapper.toDomain
import com.social.flare.features.notifications.domain.model.FlareNotification
import com.social.flare.features.notifications.domain.repository.NotificationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class NotificationRepositoryImpl(
    private val notificationDao: NotificationDao,
    private val supabase: SupabaseClient
) : NotificationRepository {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var realtimeChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null
    private var realtimeJob: Job? = null

    override fun getNotifications(userId: String): Flow<List<FlareNotification>> {
        coroutineScope.launch {
            try {
                val notifications = supabase.postgrest["notifications"]
                    .select { filter { eq("recipientId", userId) } }
                    .decodeList<NotificationEntity>()
                notifications.forEach { notificationDao.insertNotification(it) }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        return notificationDao.getNotifications(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUnreadCount(userId: String): Flow<Int> {
        return notificationDao.getUnreadCount(userId)
    }

    override suspend fun markAsRead(notificationId: String) {
        try {
            supabase.postgrest["notifications"].update({
                set("isRead", true)
            }) {
                filter { eq("id", notificationId) }
            }
        } catch (e: Throwable) { e.printStackTrace() }
        notificationDao.markAsRead(notificationId)
    }

    override suspend fun markAllAsRead(userId: String) {
        try {
            supabase.postgrest["notifications"].update({
                set("isRead", true)
            }) {
                filter { eq("recipientId", userId) }
            }
        } catch (e: Throwable) { e.printStackTrace() }
        notificationDao.markAllAsRead(userId)
    }

    override fun connectToRealtimeNotifications(userId: String) {
        disconnectFromRealtimeNotifications()

        coroutineScope.launch {
            try {
                supabase.realtime.connect()

                val channel = supabase.channel("notifications-user-$userId")

                realtimeJob = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "notifications"
                    filter("recipientId", FilterOperator.EQ, userId)
                }.onEach { action ->
                    val newNotification = action.decodeRecord<NotificationEntity>()
                    Log.d("FLARE_SYNC", "Notificación recibida: ${newNotification.type}")
                    notificationDao.insertNotification(newNotification)
                }.launchIn(coroutineScope)

                channel.subscribe()
                realtimeChannel = channel
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override fun disconnectFromRealtimeNotifications() {
        realtimeJob?.cancel()
        realtimeJob = null
        realtimeChannel?.let { channel ->
            coroutineScope.launch {
                try {
                    channel.unsubscribe()
                    supabase.realtime.removeChannel(channel)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
        realtimeChannel = null
    }
}