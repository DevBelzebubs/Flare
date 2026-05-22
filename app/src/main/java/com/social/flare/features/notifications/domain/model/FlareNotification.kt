package com.social.flare.features.notifications.domain.model

enum class NotificationType {
    LIKE,
    COMMENT,
    FOLLOW
}

data class FlareNotification(
    val id: String,
    val recipientId: String,
    val actorId: String,
    val actorUsername: String,
    val actorAvatarUrl: String?,
    val type: NotificationType,
    val referencedPostId: String? = null,
    val referencedPostMediaUrl: String? = null,
    val extraText: String? = null,
    val createdAt: Long,
    val isRead: Boolean
)