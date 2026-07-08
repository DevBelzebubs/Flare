package com.social.flare.features.notifications.data.repository

import android.util.Log
import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
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
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class NotificationRepositoryImpl(
    private val notificationDao: NotificationDao,
    private val citizenDao: CitizenDao,
    private val supabase: SupabaseClient
) : NotificationRepository {
    @Volatile
    private var realtimeChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null
    @Volatile
    private var realtimeJob: Job? = null
    private var realtimeScope: CoroutineScope? = null
    override fun getNotifications(userId: String): Flow<List<FlareNotification>> = flow {
        withContext(Dispatchers.IO) {
            try {
                val notifications = supabase.postgrest["notifications"]
                    .select { filter { eq("recipientId", userId) } }
                    .decodeList<NotificationEntity>()
                val missingIds = mutableListOf<String>()
                notifications.forEach { notification ->
                    if (notification.actorAvatarUrl.isNullOrBlank()) {
                        val citizen = citizenDao.getCitizenById(notification.actorId)
                        if (citizen?.avatar_url != null) {
                            notificationDao.insertNotification(
                                notification.copy(actorAvatarUrl = citizen.avatar_url)
                            )
                        } else {
                            missingIds.add(notification.actorId)
                            notificationDao.insertNotification(notification)
                        }
                    } else {
                        notificationDao.insertNotification(notification)
                    }
                }
                if (missingIds.isNotEmpty()) {
                    resolveMissingAvatars(notifications, missingIds)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        emitAll(notificationDao.getNotifications(userId).map { entities ->
            entities.map { it.toDomain() }
        })
    }

    private suspend fun resolveMissingAvatars(
        notifications: List<NotificationEntity>,
        missingIds: MutableList<String>
    ) {
        for (id in missingIds) {
            try {
                val citizen = supabase.postgrest["citizens"]
                    .select { filter { eq("citizen_id", id) } }
                    .decodeSingle<CitizenEntity>()
                if (citizen?.avatar_url != null) {
                    citizenDao.insertCitizen(citizen)
                    val notif = notifications.find { it.actorId == id }
                    if (notif != null) {
                        notificationDao.insertNotification(
                            notif.copy(actorAvatarUrl = citizen.avatar_url)
                        )
                    }
                }
            } catch (e: Throwable) {
                Log.e("FLARE_SYNC", "Error fetching citizen $id for avatar", e)
            }
        }
    }

    override fun getUnreadCount(userId: String): Flow<Int> {
        return notificationDao.getUnreadCount(userId)
    }

    override suspend fun markAsRead(notificationId: String) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["notifications"].update({
                set("isRead", true)
            }) {
                filter { eq("id", notificationId) }
            }
        } catch (e: Throwable) { e.printStackTrace() }
        notificationDao.markAsRead(notificationId)
    }

    override suspend fun markAllAsRead(userId: String) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["notifications"].update({
                set("isRead", true)
            }) {
                filter { eq("recipientId", userId) }
            }
        } catch (e: Throwable) { e.printStackTrace() }
        notificationDao.markAllAsRead(userId)
    }

    override fun connectToRealtimeNotifications(userId: String, scope: CoroutineScope) {
        disconnectFromRealtimeNotifications()
        realtimeScope = scope

        scope.launch {
            try {
                supabase.realtime.connect()

                val channel = supabase.channel("notifications-user-$userId")

                realtimeJob = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "notifications"
                    filter("recipientId", FilterOperator.EQ, userId)
                }.onEach { action ->
                    val newNotification = action.decodeRecord<NotificationEntity>()
                    Log.d("FLARE_SYNC", "Notificación recibida: ${newNotification.type}")
                    if (newNotification.actorAvatarUrl.isNullOrBlank()) {
                        val existing = citizenDao.getCitizenById(newNotification.actorId)
                        if (existing?.avatar_url != null) {
                            notificationDao.insertNotification(
                                newNotification.copy(actorAvatarUrl = existing.avatar_url)
                            )
                            return@onEach
                        }
                        try {
                            val remote = supabase.postgrest["citizens"]
                                .select { filter { eq("citizen_id", newNotification.actorId) } }
                                .decodeSingle<CitizenEntity>()
                            if (remote?.avatar_url != null) {
                                citizenDao.insertCitizen(remote)
                                notificationDao.insertNotification(
                                    newNotification.copy(actorAvatarUrl = remote.avatar_url)
                                )
                                return@onEach
                            }
                        } catch (e: Throwable) {
                            Log.e("FLARE_SYNC", "Error fetching citizen for realtime avatar", e)
                        }
                    }
                    notificationDao.insertNotification(newNotification)
                }.launchIn(scope)

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
            CoroutineScope(Dispatchers.IO + NonCancellable).launch {
                try {
                    channel.unsubscribe()
                    supabase.realtime.removeChannel(channel)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
        realtimeChannel = null
        realtimeScope = null
    }
}