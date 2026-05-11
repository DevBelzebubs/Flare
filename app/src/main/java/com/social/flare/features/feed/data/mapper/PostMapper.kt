package com.social.flare.features.feed.data.mapper

import com.social.flare.features.feed.data.local.dao.PostWithDetails
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
        isLikedByMe = this.isLikedByMe
    )
}