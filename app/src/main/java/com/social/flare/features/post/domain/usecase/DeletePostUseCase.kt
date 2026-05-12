package com.social.flare.features.post.domain.usecase

import com.social.flare.features.feed.domain.repository.FeedRepository

class DeletePostUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(postId: String, currentUserId: String): Result<Unit> {
        return repository.deletePost(postId, currentUserId)
    }
}