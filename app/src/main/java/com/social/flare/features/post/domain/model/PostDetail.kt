package com.social.flare.features.post.domain.model

import com.social.flare.features.feed.domain.model.Post

data class PostDetail(
    val mainPost: Post,
    val replies: List<Post>
)