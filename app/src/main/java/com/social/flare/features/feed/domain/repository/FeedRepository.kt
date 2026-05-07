package com.social.flare.features.feed.domain.repository

import com.social.flare.features.feed.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    fun getFeed(): Flow<List<Post>>
}