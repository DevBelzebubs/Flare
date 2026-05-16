package com.social.flare.features.post.domain.usecase

import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.feed.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow

class GetPostRepliesUseCase(
    private val repository: FeedRepository
) {
    operator fun invoke(parentPostId: String, currentUserId: String): Flow<List<Post>> {
        return repository.getPostReplies(parentPostId, currentUserId)
    }
}