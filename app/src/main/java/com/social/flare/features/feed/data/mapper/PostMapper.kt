package com.social.flare.features.feed.data.mapper

import com.social.flare.features.feed.data.local.dao.PostWithDetails
import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.feed.domain.model.Post

fun PostWithDetails.toDomain(): Post {
    val mediaList = if (this.post.media_urls.isNotBlank()) {
        this.post.media_urls.split(",")
    } else {
        emptyList()
    }

    return Post(
        id = this.post.post_id,
        authorId = this.post.author_id,
        authorDisplayName = this.authorDisplayName,
        authorUsername = this.authorUsername,
        authorAvatarUrl = this.authorAvatarUrl,
        content = this.post.content,
        mediaUrls = mediaList,
        parentPostId = this.post.parent_post_id,
        createdAt = this.post.created_at,
        likesCount = this.likesCount,
        commentsCount = this.commentsCount,
        isLikedByMe = this.isLikedByMe,
        isSavedByMe = this.isSavedByMe,
        sharedPostId = this.post.shared_post_id
    )
}

fun PostEntity.toDomainModel(activeUserId: String): Post {
    val mediaList = if (this.media_urls.isNotBlank()) {
        this.media_urls.split(",")
    } else {
        emptyList()
    }

    return Post(
        id = this.post_id,
        authorId = this.author_id,
        authorDisplayName = "Usuario",
        authorUsername = "@usuario",
        authorAvatarUrl = null,
        content = this.content,
        mediaUrls = mediaList,
        parentPostId = this.parent_post_id,
        createdAt = this.created_at,
        likesCount = 0,
        commentsCount = 0,
        isLikedByMe = false,
        isSavedByMe = false,
        sharedPostId = this.shared_post_id
    )
}