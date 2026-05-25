package com.social.flare.features.post.domain.usecase

import android.net.Uri
import com.social.flare.core.media.CloudinaryService
import com.social.flare.features.feed.data.mapper.toPollOptionsJson
import com.social.flare.features.feed.domain.repository.FeedRepository

class CreatePostUseCase(
    private val repository: FeedRepository,
    private val cloudinaryService: CloudinaryService
) {
    suspend operator fun invoke(
        authorId: String,
        content: String,
        mediaUris: List<Uri>,
        parentPostId: String? = null,
        pollQuestion: String? = null,
        pollOptions: List<String>? = null,
        pollExpiresAt: Long? = null,
        locationName: String? = null,
        locationLat: Double? = null,
        locationLng: Double? = null
    ) {
        val uploadedUrls = mediaUris.map { uri ->
            cloudinaryService.uploadImage(uri)
        }

        repository.createPost(
            authorId = authorId,
            content = content,
            mediaUrls = uploadedUrls,
            parentPostId = parentPostId,
            pollQuestion = pollQuestion,
            pollOptions = pollOptions?.toPollOptionsJson(),
            pollExpiresAt = pollExpiresAt,
            locationName = locationName,
            locationLat = locationLat,
            locationLng = locationLng
        )
    }
}