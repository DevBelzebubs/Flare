package com.social.flare.features.post.domain.usecase

import android.net.Uri
import com.social.flare.core.media.CloudinaryService
import com.social.flare.features.feed.domain.repository.FeedRepository

class CreatePostUseCase(
    private val repository: FeedRepository,
    private val cloudinaryService: CloudinaryService
) {
    suspend operator fun invoke(
        authorId: String,
        content: String,
        mediaUris: List<Uri>,
        parentPostId: String? = null
    ) {
        val uploadedUrls = mediaUris.map { uri ->
            cloudinaryService.uploadImage(uri)
        }

        repository.createPost(
            authorId = authorId,
            content = content,
            mediaUrls = uploadedUrls,
            parentPostId = parentPostId
        )
    }
}