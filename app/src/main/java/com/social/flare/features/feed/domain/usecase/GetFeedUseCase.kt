package com.social.flare.features.feed.domain.usecase

import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.feed.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow

class GetFeedUseCase(private val repository: FeedRepository) {
    operator fun invoke(currentUserId: String): Flow<List<Post>> {
        return repository.getFeedPosts(currentUserId)
    }
}