package com.social.flare.features.feed.data.mapper

import com.social.flare.features.feed.domain.model.StoryCommentWithAuthor
import com.social.flare.features.feed.domain.model.StoryComment

fun StoryCommentWithAuthor.toDomainModel(): StoryComment {
    return StoryComment(
        id = this.comment.comment_id,
        storyId = this.comment.story_id,
        authorId = this.comment.author_id,
        authorUsername = this.authorUsername,
        authorAvatarUrl = this.authorAvatarUrl,
        content = this.comment.content,
        createdAt = this.comment.created_at
    )
}