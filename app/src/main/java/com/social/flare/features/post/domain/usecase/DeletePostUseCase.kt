package com.social.flare.features.post.domain.usecase

import com.social.flare.core.media.CloudinaryService
import com.social.flare.features.feed.domain.repository.FeedRepository

class DeletePostUseCase(
    private val repository: FeedRepository,
    private val cloudinaryService: CloudinaryService
) {
    suspend operator fun invoke(postId: String, currentUserId: String): Result<Unit> {
        return try {
            val post = repository.getPostById(postId)
            if (post != null && post.authorId == currentUserId) {
                post.mediaUrls.forEach { url ->
                    cloudinaryService.deleteImage(url)
                }
                repository.deletePost(postId, currentUserId)
                Result.success(Unit)
            } else {
                Result.failure(Exception("No tienes permiso para borrar este post o no existe."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}