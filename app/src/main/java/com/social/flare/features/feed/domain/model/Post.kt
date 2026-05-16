package com.social.flare.features.feed.domain.model

data class Post(
    val id: String,
    val authorId: String,
    val authorDisplayName: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val content: String?,
    val createdAt: Long,
    val mediaUrls: List<String> = emptyList(),
    val replyToPostId: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val isSavedByMe: Boolean = false,
    val parentPostId: String? = null
)