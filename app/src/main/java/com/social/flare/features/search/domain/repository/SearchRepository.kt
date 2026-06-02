package com.social.flare.features.search.domain.repository

import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.search.domain.model.TrendingHashtag
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun searchUsers(query: String): Flow<List<CitizenEntity>>
    fun searchPosts(query: String, currentUserId: String): Flow<List<Post>>
    fun searchHashtagPosts(hashtag: String, currentUserId: String): Flow<List<Post>>
    fun getExplorePosts(currentUserId: String): Flow<List<Post>>
    fun getTrendingHashtags(): Flow<List<TrendingHashtag>>
}