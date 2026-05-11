package com.social.flare.features.post.domain.usecase

import android.net.Uri
import com.social.flare.core.media.CloudinaryService
import com.social.flare.features.feed.domain.repository.FeedRepository

class CreatePostUseCase(
    private val feedRepository: FeedRepository,
    private val cloudinaryService: CloudinaryService
) {
    suspend operator fun invoke(
        authorId: String,
        content: String?,
        localUris: List<Uri>,
        replyToPostId: String? = null
    ): Result<Unit> {
        return try {
            val remoteUrls = if (localUris.isNotEmpty()) {
                cloudinaryService.uploadMultipleImages(localUris);
            }else{
                emptyList()
            }
            if (content.isNullOrBlank() && remoteUrls.isEmpty()){
                return Result.failure(Exception("Content or images are required"));
            }
            feedRepository.createPost(authorId, content, remoteUrls, replyToPostId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}