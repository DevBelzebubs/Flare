package com.social.flare.features.feed.data.mapper

import com.social.flare.features.feed.data.local.dao.PostWithDetails
import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.feed.domain.model.Post

fun PostWithDetails.toDomain(): Post {
    return Post(
        id = this.post.post_id,
        authorId = this.post.author_id,
        authorDisplayName = this.authorDisplayName,
        authorUsername = this.authorUsername,
        authorAvatarUrl = this.authorAvatarUrl,
        content = this.post.content,
        mediaUrls = this.post.media_urls,
        replyToPostId = this.post.reply_to_post_id,
        createdAt = this.post.created_at,
        likesCount = this.likesCount,
        commentsCount = this.commentsCount,
        isLikedByMe = this.isLikedByMe,
        isSavedByMe = this.isSavedByMe
    )
}

fun PostEntity.toDomainModel(activeUserId: String): Post {
    return Post(
        id = this.post_id,
        authorId = this.author_id,
        authorDisplayName = "Usuario",
        authorUsername = "@usuario",
        authorAvatarUrl = null,
        content = this.content,
        mediaUrls = this.media_urls,
        replyToPostId = this.reply_to_post_id,
        createdAt = this.created_at,
        likesCount = 0,
        commentsCount = 0,
        isLikedByMe = false,
        //isSavedByMe = this.isSavedByMe
    )
}