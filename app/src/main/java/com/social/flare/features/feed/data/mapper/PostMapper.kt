package com.social.flare.features.feed.data.mapper

import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.feed.domain.model.Post

fun PostEntity.toDomain(
    authorDisplayName: String,
    authorUsername: String,
    authorAvatarUrl: String?,
    mediaUrls: List<String>,
    likesCount: Int,
    commentsCount: Int,
    isLikedByMe: Boolean
): Post {
    return Post(
        id = this.post_id,
        authorId = this.author_id,
        authorDisplayName = authorDisplayName,
        authorUsername = authorUsername,
        authorAvatarUrl = authorAvatarUrl,
        content = this.content,
        createdAt = this.created_at,
        mediaUrls = mediaUrls,
        likesCount = likesCount,
        commentsCount = commentsCount,
        isLikedByMe = isLikedByMe
    )
}