package com.social.flare.features.notifications.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_table")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val recipientId: String,
    val actorId: String,
    val actorUsername: String,
    val actorAvatarUrl: String?,
    val type: String,
    val referencedPostId: String?,
    val referencedPostMediaUrl: String?,
    val extraText: String?,
    val createdAt: Long,
    val isRead: Boolean
)