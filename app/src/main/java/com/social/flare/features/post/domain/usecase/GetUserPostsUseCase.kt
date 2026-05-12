package com.social.flare.features.post.domain.usecase

import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.feed.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow

class GetUserPostsUseCase(private val repository: FeedRepository) {
    operator fun invoke(userId: String): Flow<List<Post>> {
        return repository.getUserPosts(userId)
    }
}