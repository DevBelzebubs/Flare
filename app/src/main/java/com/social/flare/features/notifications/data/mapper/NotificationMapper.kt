package com.social.flare.features.notifications.data.mapper

import com.social.flare.features.notifications.data.local.entity.NotificationEntity
import com.social.flare.features.notifications.domain.model.FlareNotification
import com.social.flare.features.notifications.domain.model.NotificationType

fun NotificationEntity.toDomain(): FlareNotification {
    return FlareNotification(
        id = id,
        recipientId = recipientId,
        actorId = actorId,
        actorUsername = actorUsername,
        actorAvatarUrl = actorAvatarUrl,
        type = NotificationType.valueOf(type),
        referencedPostId = referencedPostId,
        referencedPostMediaUrl = referencedPostMediaUrl,
        extraText = extraText,
        createdAt = createdAt,
        isRead = isRead
    )
}

fun FlareNotification.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        recipientId = recipientId,
        actorId = actorId,
        actorUsername = actorUsername,
        actorAvatarUrl = actorAvatarUrl,
        type = type.name,
        referencedPostId = referencedPostId,
        referencedPostMediaUrl = referencedPostMediaUrl,
        extraText = extraText,
        createdAt = createdAt,
        isRead = isRead
    )
}