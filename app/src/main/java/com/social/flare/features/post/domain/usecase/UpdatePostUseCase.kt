package com.social.flare.features.post.domain.usecase

import com.social.flare.features.feed.domain.repository.FeedRepository

class UpdatePostUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(
        postId: String,
        currentUserId: String,
        newContent: String
    ): Result<Unit> {
        if (newContent.isBlank()) {
            return Result.failure(Exception("El contenido no puede estar vacío"))
        }
        return repository.updatePost(postId, currentUserId, newContent)
    }
}