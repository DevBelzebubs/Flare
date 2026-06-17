package com.social.flare.features.search.data.repository


import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.feed.data.local.dao.PostDao
import com.social.flare.features.feed.data.local.entity.HashtagEntity
import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.feed.data.local.entity.PostHashtagEntity
import com.social.flare.features.feed.data.mapper.toDomain
import com.social.flare.features.feed.domain.FeedRanking
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.search.data.local.dao.SearchDao
import com.social.flare.features.search.domain.model.TrendingHashtag
import com.social.flare.features.search.domain.repository.SearchRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


class SearchRepositoryImpl(
    private val postDao: PostDao,
    private val citizenDao: CitizenDao,
    private val searchDao: SearchDao,
    private val supabase: SupabaseClient
) : SearchRepository {

    override fun searchUsers(query: String): Flow<List<CitizenEntity>> = flow {
        withContext(Dispatchers.IO) {
            try {
                val results = supabase.postgrest["citizens"]
                    .select {
                        filter {
                            ilike("username", "%$query%")
                        }
                    }
                    .decodeList<CitizenEntity>()
                results.forEach { citizenDao.insertCitizen(it) }
            } catch (_: Exception) {}
        }
        emitAll(searchDao.searchUsers(query))
    }

    override fun searchPosts(query: String, currentUserId: String): Flow<List<Post>> = flow {
        withContext(Dispatchers.IO) {
            try {
                val results = supabase.postgrest["posts"]
                    .select {
                        filter {
                            ilike("content", "%$query%")
                        }
                    }
                    .decodeList<PostEntity>()
                results.forEach { postDao.insertPost(it) }
            } catch (_: Exception) {}
        }
        emitAll(
            searchDao.searchPosts(query).map { entities ->
                entities.map { it.toDomain() }
            }
        )
    }

    override fun searchHashtagPosts(hashtag: String, currentUserId: String): Flow<List<Post>> = flow {
        emitAll(
            searchDao.getPostsByHashtag(hashtag).map { entities ->
                entities.map { it.toDomain() }
            }
        )
    }

    override fun getExplorePosts(currentUserId: String): Flow<List<Post>> = flow {
        withContext(Dispatchers.IO) {
            try {
                val posts = supabase.postgrest["posts"]
                    .select()
                    .decodeList<PostEntity>()
                val authorIds = posts.map { it.author_id }.distinct()
                if (authorIds.isNotEmpty()) {
                    val authors = supabase.postgrest["citizens"]
                        .select { filter { isIn("citizen_id", authorIds) } }
                        .decodeList<CitizenEntity>()
                    authors.forEach { citizenDao.insertCitizen(it) }
                }
                posts.forEach { postDao.insertPost(it) }
            } catch (_: Exception) {}
        }
        emitAll(
            postDao.getFeedPosts(currentUserId).map { entities ->
                val now = System.currentTimeMillis()
                entities.map { it.toDomain() }
                    .sortedByDescending { post ->
                        FeedRanking.score(
                            likesCount = post.likesCount,
                            commentsCount = post.commentsCount,
                            createdAt = post.createdAt,
                            currentTime = now,
                            isFollowed = false
                        )
                    }
            }
        )
    }

    override fun getTrendingHashtags(): Flow<List<TrendingHashtag>> = flow {
        withContext(Dispatchers.IO) {
            try {
                val hashtags = supabase.postgrest["hashtags"]
                    .select()
                    .decodeList<HashtagEntity>()
                val relations = supabase.postgrest["post_hashtags"]
                    .select()
                    .decodeList<PostHashtagEntity>()
                postDao.syncHashtagsTransaction(hashtags, relations)
            } catch (_: Exception) {}
        }
        emitAll(searchDao.getTrendingHashtags())
    }
}