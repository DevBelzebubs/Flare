package com.social.flare.features.feed.domain.model

data class StoryComment(
    val id: String,
    val storyId: String,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val content: String,
    val createdAt: Long
)