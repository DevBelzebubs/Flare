package com.social.flare.features.feed.data.repository

import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.feed.data.local.dao.PostDao
import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.feed.data.local.entity.PostLikeEntity
import com.social.flare.features.feed.data.local.entity.SavedPostEntity
import com.social.flare.features.feed.data.mapper.toDomain
import com.social.flare.features.feed.domain.FeedRanking
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.feed.domain.repository.FeedRepository
import com.social.flare.features.post.domain.model.PostDetail
import com.social.flare.features.profile.data.local.dao.FollowDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class FeedRepositoryImpl(
    private val postDao: PostDao,
    private val citizenDao: CitizenDao,
    private val followDao: FollowDao? = null,
    private val supabase: SupabaseClient
) : FeedRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getFeedPosts(currentUserId: String): Flow<List<Post>> {
        syncPostsFromSupabase(currentUserId, isGuest = false)
        return postDao.getFeedPosts(currentUserId).map { entities ->
            val followedIds = followDao?.getFollowedIds(currentUserId) ?: emptyList()
            val now = System.currentTimeMillis()
            entities.map { it.toDomain() }
                .sortedByDescending { post ->
                    FeedRanking.score(
                        likesCount = post.likesCount,
                        commentsCount = post.commentsCount,
                        createdAt = post.createdAt,
                        currentTime = now,
                        isFollowed = followedIds.contains(post.authorId)
                    )
                }
        }
    }

    override fun getFeedPostsGuest(): Flow<List<Post>> {
        syncPostsFromSupabase(isGuest = true)
        return postDao.getFeedPostsGuest().map { entities ->
            val now = System.currentTimeMillis()
            entities.map { it.toDomain() }
                .sortedByDescending { post ->
                    FeedRanking.score(
                        likesCount = post.likesCount,
                        commentsCount = post.commentsCount,
                        createdAt = post.createdAt,
                        currentTime = now
                    )
                }
        }
    }

    private fun syncPostsFromSupabase(currentUserId: String? = null, isGuest: Boolean = false) {
        scope.launch {
            try {
                val posts = supabase.postgrest["posts"]
                    .select { filter { isNull("parent_post_id") } }
                    .decodeList<PostEntity>()

                val authorIds = posts.map { it.author_id }.distinct()
                val authors = supabase.postgrest["citizens"]
                    .select { filter { isIn("citizen_id", authorIds) } }
                    .decodeList<CitizenEntity>()

                authors.forEach { citizenDao.insertCitizen(it) }
                posts.forEach { postDao.insertPost(it) }

                if (!isGuest && currentUserId != null) {
                    val likes = supabase.postgrest["post_likes"]
                        .select { filter { eq("citizen_id", currentUserId) } }
                        .decodeList<PostLikeEntity>()
                    val saves = supabase.postgrest["saved_posts"]
                        .select { filter { eq("citizen_id", currentUserId) } }
                        .decodeList<SavedPostEntity>()

                    likes.forEach { postDao.insertLike(it) }
                    saves.forEach { postDao.insertSavedPost(it) }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override fun getPostReplies(
        parentPostId: String,
        currentUserId: String
    ): Flow<List<Post>> {
        return postDao.getPostReplies(parentPostId, currentUserId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createPost(
        authorId: String,
        content: String,
        mediaUrls: List<String>,
        parentPostId: String?
    ) {
        val newPostId = UUID.randomUUID().toString()
        val newPost = PostEntity(
            post_id = newPostId,
            author_id = authorId,
            content = content,
            media_urls = mediaUrls.joinToString(","),
            created_at = System.currentTimeMillis(),
            likes_count = 0,
            comments_count = 0,
            parent_post_id = parentPostId
        )

        try {
            supabase.postgrest["posts"].insert(newPost)
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
        postDao.insertPost(newPost)

        if (parentPostId != null) {
            postDao.incrementCommentsCount(parentPostId)
        }
    }

    override suspend fun toggleLike(
        postId: String,
        citizenId: String,
        isCurrentlyLiked: Boolean
    ): Result<Unit> {
        return try {
            if (isCurrentlyLiked) {
                supabase.postgrest["post_likes"].delete {
                    filter { eq("post_id", postId) }
                    filter { eq("citizen_id", citizenId) }
                }
                postDao.deleteLike(postId, citizenId)
            } else {
                try {
                    supabase.postgrest["post_likes"].insert(PostLikeEntity(postId, citizenId))
                } catch (e: Throwable) { e.printStackTrace() }
                postDao.insertLike(PostLikeEntity(postId, citizenId))
            }
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override suspend fun toggleSavePost(
        postId: String,
        citizenId: String,
        isCurrentlySaved: Boolean
    ) {
        if (isCurrentlySaved) {
            supabase.postgrest["saved_posts"].delete {
                filter { eq("post_id", postId) }
                filter { eq("citizen_id", citizenId) }
            }
            postDao.deleteSavedPost(citizenId, postId)
        } else {
            val saved = SavedPostEntity(
                citizen_id = citizenId,
                post_id = postId,
                saved_at = System.currentTimeMillis()
            )
            supabase.postgrest["saved_posts"].insert(saved)
            postDao.insertSavedPost(saved)
        }
    }

    override suspend fun updatePost(
        postId: String,
        currentUserId: String,
        newContent: String
    ): Result<Unit> {
        return try {
            supabase.postgrest["posts"].update({
                set("content", newContent)
            }) {
                filter { eq("post_id", postId) }
                filter { eq("author_id", currentUserId) }
            }
            val rowsAffected = postDao.updatePostContent(postId, currentUserId, newContent)
            if (rowsAffected > 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("No se pudo editar. El post no existe o no te pertenece."))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override suspend fun deletePost(
        postId: String,
        currentUserId: String
    ): Result<Unit> {
        return try {
            supabase.postgrest["posts"].delete {
                filter { eq("post_id", postId) }
                filter { eq("author_id", currentUserId) }
            }
            postDao.deletePostSafely(postId, currentUserId)
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override fun getUserPosts(userId: String): Flow<List<Post>> {
        scope.launch {
            try {
                val userPosts = supabase.postgrest["posts"]
                    .select { filter { eq("author_id", userId) } }
                    .decodeList<PostEntity>()
                userPosts.forEach { postDao.insertPost(it) }

                val author = supabase.postgrest["citizens"]
                    .select { filter { eq("citizen_id", userId) } }
                    .decodeSingle<CitizenEntity>()
                citizenDao.insertCitizen(author)
            } catch (e: Throwable) { e.printStackTrace() }
        }
        return postDao.getPostsByAuthor(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPostDetail(
        postId: String,
        currentUserId: String
    ): Flow<PostDetail> {
        scope.launch {
            try {
                val post = supabase.postgrest["posts"]
                    .select { filter { eq("post_id", postId) } }
                    .decodeSingle<PostEntity>()
                postDao.insertPost(post)

                try {
                    val author = supabase.postgrest["citizens"]
                        .select { filter { eq("citizen_id", post.author_id) } }
                        .decodeSingle<CitizenEntity>()
                    citizenDao.insertCitizen(author)
                } catch (e: Throwable) { e.printStackTrace() }

                try {
                    val replies = supabase.postgrest["posts"]
                        .select { filter { eq("parent_post_id", postId) } }
                        .decodeList<PostEntity>()
                    replies.forEach { postDao.insertPost(it) }

                    val replyAuthorIds = replies.map { it.author_id }.distinct()
                    if (replyAuthorIds.isNotEmpty()) {
                        try {
                            val replyAuthors = supabase.postgrest["citizens"]
                                .select { filter { isIn("citizen_id", replyAuthorIds) } }
                                .decodeList<CitizenEntity>()
                            replyAuthors.forEach { citizenDao.insertCitizen(it) }
                        } catch (e: Throwable) { e.printStackTrace() }
                    }
                } catch (e: Throwable) { e.printStackTrace() }

                try {
                    val likes = supabase.postgrest["post_likes"]
                        .select { filter { eq("citizen_id", currentUserId) } }
                        .decodeList<PostLikeEntity>()
                    likes.forEach { postDao.insertLike(it) }
                } catch (e: Throwable) { e.printStackTrace() }
            } catch (e: Throwable) { e.printStackTrace() }
        }

        val mainPostFlow = postDao.getPostById(postId, currentUserId).filterNotNull()
        val repliesFlow = postDao.getPostReplies(postId, currentUserId)

        return combine(mainPostFlow, repliesFlow) { mainPostEntity, repliesEntities ->
            PostDetail(
                mainPost = mainPostEntity.toDomain(),
                replies = repliesEntities.map { it.toDomain() }
            )
        }
    }

    override suspend fun getPostById(postId: String): Post? {
        return try {
            val post = supabase.postgrest["posts"]
                .select { filter { eq("post_id", postId) } }
                .decodeSingle<PostEntity>()
            postDao.insertPost(post)

            val author = supabase.postgrest["citizens"]
                .select { filter { eq("citizen_id", post.author_id) } }
                .decodeSingle<CitizenEntity>()
            citizenDao.insertCitizen(author)

            val cached = postDao.getPostById(postId)
            cached?.toDomain()
        } catch (e: Throwable) {
            postDao.getPostById(postId)?.toDomain()
        }
    }

    override suspend fun sharePost(authorId: String, originalPostId: String) {
        val newPostId = UUID.randomUUID().toString()
        val sharedPost = PostEntity(
            post_id = newPostId,
            author_id = authorId,
            content = null,
            media_urls = "",
            created_at = System.currentTimeMillis(),
            likes_count = 0,
            comments_count = 0,
            shared_post_id = originalPostId
        )

        supabase.postgrest["posts"].insert(sharedPost)
        postDao.insertPost(sharedPost)
    }

    override fun getSharedPosts(userId: String): Flow<List<Post>> {
        scope.launch {
            try {
                val shared = supabase.postgrest["posts"]
                    .select {
                        filter { eq("author_id", userId) }
                        filter { isNotNull("shared_post_id") }
                    }
                    .decodeList<PostEntity>()
                shared.forEach { postDao.insertPost(it) }
            } catch (e: Throwable) { e.printStackTrace() }
        }
        return postDao.getSharedPosts(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun PostgrestFilterBuilder.isNull(column: String) {
        filter(column, FilterOperator.IS, null)
    }

    private fun PostgrestFilterBuilder.isNotNull(column: String) {
        filterNot(column, FilterOperator.IS, null)
    }

}
