package com.social.flare.features.feed.domain.usecase

import com.social.flare.features.feed.domain.repository.FeedRepository

class GetFeedUseCase(private val repository: FeedRepository) {
    operator fun invoke() = repository.getFeed()
}