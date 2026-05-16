package com.social.flare.features.feed.data.local.entity

import androidx.room.Embedded

data class StoryWithAuthor(
    @Embedded val story: StoryEntity,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val isViewedByMe: Boolean
)