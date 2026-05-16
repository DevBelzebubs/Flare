package com.social.flare.features.feed.domain.model

import androidx.room.Embedded
import com.social.flare.features.feed.data.local.entity.StoryCommentEntity

data class StoryCommentWithAuthor(
    @Embedded val comment: StoryCommentEntity,
    val authorUsername: String,
    val authorAvatarUrl: String?
)