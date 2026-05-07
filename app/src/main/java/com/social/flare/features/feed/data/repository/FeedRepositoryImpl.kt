package com.social.flare.features.feed.data.repository

import com.social.flare.features.feed.data.local.dao.PostDao
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.feed.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FeedRepositoryImpl(
    private val postDao: PostDao
) : FeedRepository {
    override fun getFeed(): Flow<List<Post>> {
        return emptyFlow()
    }
}