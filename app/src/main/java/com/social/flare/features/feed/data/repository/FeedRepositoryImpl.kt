package com.social.flare.features.feed.data.repository

import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.feed.data.local.dao.PostDao
import com.social.flare.features.feed.data.local.entity.HashtagEntity
import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.feed.data.local.entity.PostHashtagEntity
import com.social.flare.features.feed.data.local.entity.PostLikeEntity
import com.social.flare.features.feed.data.local.entity.PostVoteEntity
import com.social.flare.features.feed.data.local.entity.SavedPostEntity
import com.social.flare.features.feed.data.mapper.parseIntList
import com.social.flare.features.feed.data.mapper.toDomain
import com.social.flare.features.feed.data.mapper.toVoteCountsJson
import com.social.flare.features.feed.domain.FeedRanking
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.feed.domain.repository.FeedRepository
import com.social.flare.features.post.domain.model.PostDetail
import com.social.flare.features.profile.data.local.dao.FollowDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class FeedRepositoryImpl(
    private val postDao: PostDao,
    private val citizenDao: CitizenDao,
    private val followDao: FollowDao? = null,
    private val supabase: SupabaseClient
) : FeedRepository {

    override fun getFeedPosts(currentUserId: String): Flow<List<Post>> = flow {
        withContext(Dispatchers.IO) { syncPostsFromSupabase(currentUserId, isGuest = false) }
        val sourceFlow = postDao.getFeedPosts(currentUserId)
        sourceFlow.collect { entities ->
            val followedIds = followDao?.getFollowedIds(currentUserId) ?: emptyList()
            val now = System.currentTimeMillis()
            val pollPostIds = entities.mapNotNull { entity ->
                if (entity.post.poll_question != null) entity.post.post_id else null
            }
            val userVotesMap = if (pollPostIds.isNotEmpty()) {
                postDao.getUserVotes(pollPostIds, currentUserId).associate { it.post_id to it.selected_option_index }
            } else emptyMap()
            val posts = entities.map { entity ->
                val post = entity.toDomain()
                if (post.pollQuestion != null) {
                    val counts = parseIntList(entity.post.poll_vote_counts)
                    val userVote = userVotesMap[post.id]
                    post.copy(pollVoteCounts = counts, userSelectedOptionIndex = userVote)
                } else {
                    post
                }
            }.sortedByDescending { post ->
                FeedRanking.score(
                    likesCount = post.likesCount,
                    commentsCount = post.commentsCount,
                    createdAt = post.createdAt,
                    currentTime = now,
                    isFollowed = followedIds.contains(post.authorId)
                )
            }
            emit(posts)
        }
    }

    override fun getFeedPostsGuest(): Flow<List<Post>> = flow {
        withContext(Dispatchers.IO) { syncPostsFromSupabase(isGuest = true) }
        emitAll(
            postDao.getFeedPostsGuest().map { entities ->
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
        )
    }

    private suspend fun syncPostsFromSupabase(
        currentUserId: String? = null,
        isGuest: Boolean = false,
        page: Int = 0,
        pageSize: Int = 50
    ) {
        try {
            val posts = supabase.postgrest["posts"]
                .select {
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(pageSize.toLong())
                }
                .decodeList<PostEntity>()

            val authorIds = posts.map { it.author_id }.distinct()
            if (authorIds.isNotEmpty()) {
                val authors = supabase.postgrest["citizens"]
                    .select { filter { isIn("citizen_id", authorIds) } }
                    .decodeList<CitizenEntity>()

                authors.forEach { citizenDao.insertCitizen(it) }
            }
            posts.forEach { postDao.insertPost(it) }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        if (!isGuest && currentUserId != null) {
            try {
                val likes = supabase.postgrest["post_likes"]
                    .select { filter { eq("citizen_id", currentUserId) } }
                    .decodeList<PostLikeEntity>()
                likes.forEach { postDao.insertLike(it) }
            } catch (e: Throwable) {
                e.printStackTrace()
            }

            try {
                val saves = supabase.postgrest["saved_posts"]
                    .select { filter { eq("citizen_id", currentUserId) } }
                    .decodeList<SavedPostEntity>()
                saves.forEach { postDao.insertSavedPost(it) }
            } catch (e: Throwable) {
                e.printStackTrace()
            }

            try {
                val votes = supabase.postgrest["post_votes"]
                    .select { filter { eq("citizen_id", currentUserId) } }
                    .decodeList<PostVoteEntity>()
                votes.forEach { postDao.insertVote(it) }
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
            val pollPostIds = entities.mapNotNull { entity ->
                if (entity.post.poll_question != null) entity.post.post_id else null
            }
            val userVotesMap = if (pollPostIds.isNotEmpty()) {
                postDao.getUserVotes(pollPostIds, currentUserId).associate { it.post_id to it.selected_option_index }
            } else emptyMap()
            entities.map { entity ->
                val post = entity.toDomain()
                if (post.pollQuestion != null) {
                    val counts = parseIntList(entity.post.poll_vote_counts)
                    val userVote = userVotesMap[post.id]
                    post.copy(pollVoteCounts = counts, userSelectedOptionIndex = userVote)
                } else post
            }
        }
    }

    override suspend fun createPost(
        authorId: String,
        content: String,
        mediaUrls: List<String>,
        parentPostId: String?,
        pollQuestion: String?,
        pollOptions: String?,
        pollExpiresAt: Long?,
        locationName: String?,
        locationLat: Double?,
        locationLng: Double?
    ) = withContext(Dispatchers.IO) {
        val newPostId = UUID.randomUUID().toString()
        val newPost = PostEntity(
            post_id = newPostId,
            author_id = authorId,
            content = content,
            media_urls = mediaUrls.joinToString(","),
            created_at = System.currentTimeMillis(),
            likes_count = 0,
            comments_count = 0,
            parent_post_id = parentPostId,
            poll_question = pollQuestion,
            poll_options = pollOptions,
            poll_expires_at = pollExpiresAt,
            location_name = locationName,
            location_lat = locationLat,
            location_lng = locationLng
        )

        try {
            supabase.postgrest["posts"].insert(newPost)
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }

        try {
            val hashtagPattern = Regex("#(\\w+)")
            val hashtagNames = hashtagPattern.findAll(content)
                .map { it.groupValues[1].lowercase() }
                .distinct()
                .toList()

            val hashtagEntities = mutableListOf<HashtagEntity>()
            val postHashtagEntities = mutableListOf<PostHashtagEntity>()

            for (tag in hashtagNames) {
                val existingId = postDao.getHashtagId(tag)
                val tagId = existingId ?: UUID.randomUUID().toString().also {
                    supabase.postgrest["hashtags"].insert(HashtagEntity(it, tag, System.currentTimeMillis()))
                }
                val hashtagEntity = HashtagEntity(tagId, tag, System.currentTimeMillis())
                hashtagEntities.add(hashtagEntity)
                postHashtagEntities.add(PostHashtagEntity(newPostId, tagId))
                try {
                    supabase.postgrest["post_hashtags"].insert(PostHashtagEntity(newPostId, tagId))
                } catch (_: Exception) {}
            }

            postDao.insertPostWithHashtags(newPost, hashtagEntities, postHashtagEntities)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        if (parentPostId != null) {
            try {
                val parent = supabase.postgrest["posts"]
                    .select { filter { eq("post_id", parentPostId) } }
                    .decodeSingle<PostEntity>()
                supabase.postgrest["posts"].update({
                    set("comments_count", parent.comments_count + 1)
                }) {
                    filter { eq("post_id", parentPostId) }
                }
            } catch (_: Exception) {}
            postDao.incrementCommentsCount(parentPostId)
        }
    }

    override suspend fun castVote(postId: String, citizenId: String, optionIndex: Int) = withContext(Dispatchers.IO) {
        val currentPost = postDao.getPostById(postId)
        val currentCounts = parseIntList(currentPost?.post?.poll_vote_counts)
            ?: currentPost?.post?.let { post ->
                val options = post.poll_options
                if (options != null) {
                    val parsed = com.social.flare.features.feed.data.mapper.parsePollOptions(options)
                    parsed?.map { 0 }
                } else null
            }
            ?: return@withContext

        val existingVote = postDao.getUserVote(postId, citizenId)
        val newCounts = currentCounts.toMutableList()

        if (existingVote != null) {
            if (existingVote == optionIndex) return@withContext
            newCounts[existingVote] = maxOf(0, newCounts[existingVote] - 1)
        }

        if (optionIndex in newCounts.indices) {
            newCounts[optionIndex] = newCounts[optionIndex] + 1
        }

        postDao.castVoteTransaction(postId, citizenId, PostVoteEntity(postId, citizenId, optionIndex), currentPost!!.post.copy(poll_vote_counts = newCounts.toVoteCountsJson()))

        try {
            supabase.postgrest["post_votes"].upsert(PostVoteEntity(postId, citizenId, optionIndex))
            supabase.postgrest["posts"].update({
                set("poll_vote_counts", newCounts.toVoteCountsJson())
            }) { filter { eq("post_id", postId) } }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override suspend fun toggleLike(
        postId: String,
        citizenId: String,
        isCurrentlyLiked: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentPost = supabase.postgrest["posts"]
                .select { filter { eq("post_id", postId) } }
                .decodeSingle<PostEntity>()

            if (isCurrentlyLiked) {
                supabase.postgrest["post_likes"].delete {
                    filter { eq("post_id", postId) }
                    filter { eq("citizen_id", citizenId) }
                }
                val newCount = maxOf(0, currentPost.likes_count - 1)
                supabase.postgrest["posts"].update({ set("likes_count", newCount) }) {
                    filter { eq("post_id", postId) }
                }
                postDao.toggleLikeTransaction(postId, citizenId, null, isLiked = true)

            } else {
                supabase.postgrest["post_likes"].insert(PostLikeEntity(postId, citizenId))

                val newCount = currentPost.likes_count + 1
                supabase.postgrest["posts"].update({ set("likes_count", newCount) }) {
                    filter { eq("post_id", postId) }
                }
                postDao.toggleLikeTransaction(postId, citizenId, PostLikeEntity(postId, citizenId), isLiked = false)
            }

            val updatedPost = supabase.postgrest["posts"]
                .select { filter { eq("post_id", postId) } }
                .decodeSingle<PostEntity>()
            postDao.insertPost(updatedPost)

            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override suspend fun toggleSavePost(
        postId: String,
        citizenId: String,
        isCurrentlySaved: Boolean
    ) = withContext(Dispatchers.IO) {
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
    ):         Result<Unit> = withContext(Dispatchers.IO) {
        try {
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
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["posts"].delete {
                filter { eq("post_id", postId) }
                filter { eq("author_id", currentUserId) }
            }
            postDao.deletePostSafely(postId, currentUserId)
            supabase.postgrest["post_hashtags"].delete {
                filter { eq("post_id", postId) }
            }
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override fun getUserPosts(userId: String): Flow<List<Post>> = flow {
        withContext(Dispatchers.IO) {
            try {
                val userPosts = supabase.postgrest["posts"]
                    .select { filter { eq("author_id", userId) } }
                    .decodeList<PostEntity>()

                val author = supabase.postgrest["citizens"]
                    .select { filter { eq("citizen_id", userId) } }
                    .decodeSingle<CitizenEntity>()

                citizenDao.insertCitizen(author)
                userPosts.forEach { postDao.insertPost(it) }
            } catch (e: Throwable) { e.printStackTrace() }
        }
        emitAll(postDao.getPostsByAuthor(userId).map { entities ->
            val pollPostIds = entities.mapNotNull { entity ->
                if (entity.post.poll_question != null) entity.post.post_id else null
            }
            val userVotesMap = if (pollPostIds.isNotEmpty()) {
                postDao.getUserVotes(pollPostIds, userId).associate { it.post_id to it.selected_option_index }
            } else emptyMap()
            entities.map { entity ->
                val post = entity.toDomain()
                if (post.pollQuestion != null) {
                    val counts = parseIntList(entity.post.poll_vote_counts)
                    val userVote = userVotesMap[post.id]
                    post.copy(pollVoteCounts = counts, userSelectedOptionIndex = userVote)
                } else post
            }
        })
    }

    override fun getPostDetail(
        postId: String,
        currentUserId: String
    ): Flow<PostDetail> = flow {
        withContext(Dispatchers.IO) {
            try {
                val post = supabase.postgrest["posts"]
                    .select { filter { eq("post_id", postId) } }
                    .decodeSingle<PostEntity>()
                if (post.shared_post_id != null) {
                    try {
                        val originalPost = supabase.postgrest["posts"]
                            .select { filter { eq("post_id", post.shared_post_id!!) } }
                            .decodeSingle<PostEntity>()
                        val originalAuthor = supabase.postgrest["citizens"]
                            .select { filter { eq("citizen_id", originalPost.author_id) } }
                            .decodeSingle<CitizenEntity>()
                        citizenDao.insertCitizen(originalAuthor)
                        postDao.insertPost(originalPost)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
                val author = supabase.postgrest["citizens"]
                    .select { filter { eq("citizen_id", post.author_id) } }
                    .decodeSingle<CitizenEntity>()

                val replies = supabase.postgrest["posts"]
                    .select { filter { eq("parent_post_id", postId) } }
                    .decodeList<PostEntity>()

                val replyAuthorIds = replies.map { it.author_id }.distinct()
                val replyAuthors = if (replyAuthorIds.isNotEmpty()) {
                    supabase.postgrest["citizens"]
                        .select { filter { isIn("citizen_id", replyAuthorIds) } }
                        .decodeList<CitizenEntity>()
                } else emptyList()

                val likes = supabase.postgrest["post_likes"]
                    .select { filter { eq("citizen_id", currentUserId) } }
                    .decodeList<PostLikeEntity>()

                citizenDao.insertCitizen(author)
                replyAuthors.forEach { citizenDao.insertCitizen(it) }

                postDao.insertPost(post)
                replies.forEach { postDao.insertPost(it) }

                likes.forEach { postDao.insertLike(it) }

            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        val mainPostFlow = postDao.getPostById(postId, currentUserId).filterNotNull()
        val repliesFlow = postDao.getPostReplies(postId, currentUserId)

        val parentPostFlow = mainPostFlow.flatMapLatest { mainEntity ->
            val sharedId = mainEntity.post.shared_post_id
            if (sharedId != null) {
                postDao.getPostById(sharedId, currentUserId)
            } else {
                flowOf(null)
            }
        }
        emitAll(combine(mainPostFlow, repliesFlow, parentPostFlow) { mainPostEntity, repliesEntities, parentPostEntity ->
            val mainPost = mainPostEntity.toDomain()
            val allPostIds = buildList {
                if (mainPost.pollQuestion != null) add(mainPost.id)
                addAll(repliesEntities.mapNotNull { entity ->
                    if (entity.post.poll_question != null) entity.post.post_id else null
                })
            }
            val userVotesMap = if (allPostIds.isNotEmpty()) {
                postDao.getUserVotes(allPostIds, currentUserId).associate { it.post_id to it.selected_option_index }
            } else emptyMap()

            val enrichedMain = if (mainPost.pollQuestion != null) {
                val counts = parseIntList(mainPostEntity.post.poll_vote_counts)
                val userVote = userVotesMap[mainPost.id]
                mainPost.copy(pollVoteCounts = counts, userSelectedOptionIndex = userVote)
            } else mainPost

            val enrichedReplies = repliesEntities.map { entity ->
                val reply = entity.toDomain()
                if (reply.pollQuestion != null) {
                    val counts = parseIntList(entity.post.poll_vote_counts)
                    val userVote = userVotesMap[reply.id]
                    reply.copy(pollVoteCounts = counts, userSelectedOptionIndex = userVote)
                } else reply
            }

            PostDetail(
                parentPost = parentPostEntity?.toDomain(),
                mainPost = enrichedMain,
                replies = enrichedReplies
            )
        })
    }

    override suspend fun getPostById(postId: String): Post? = withContext(Dispatchers.IO) {
        try {
            val post = supabase.postgrest["posts"]
                .select { filter { eq("post_id", postId) } }
                .decodeSingle<PostEntity>()
            val author = supabase.postgrest["citizens"]
                .select { filter { eq("citizen_id", post.author_id) } }
                .decodeSingle<CitizenEntity>()
            citizenDao.insertCitizen(author)
            postDao.insertPost(post)

            val cached = postDao.getPostById(postId)
            cached?.let { entity ->
                val domain = entity.toDomain()
                if (domain.pollQuestion != null) {
                    val counts = parseIntList(entity.post.poll_vote_counts)
                    domain.copy(pollVoteCounts = counts)
                } else domain
            }
        } catch (e: Throwable) {
            postDao.getPostById(postId)?.toDomain()
        }
    }

    override suspend fun toggleSharePost(authorId: String, originalPostId: String, isCurrentlyShared: Boolean) = withContext(Dispatchers.IO) {
        if (isCurrentlyShared) {
            val sharedPostId = postDao.getSharedPostId(authorId, originalPostId)
            if (sharedPostId != null) {
                supabase.postgrest["posts"].delete { filter { eq("post_id", sharedPostId) } }
                postDao.deleteTargetPost(sharedPostId)
            }
        } else {
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
    }

    override fun getSharedPosts(userId: String): Flow<List<Post>> = flow {
        withContext(Dispatchers.IO) {
            try {
                val shared = supabase.postgrest["posts"]
                    .select {
                        filter { eq("author_id", userId) }
                        filter { isNotNull("shared_post_id") }
                    }
                    .decodeList<PostEntity>()

                val originalIds = shared.mapNotNull { it.shared_post_id }.distinct()
                if (originalIds.isNotEmpty()) {
                    val originals = supabase.postgrest["posts"]
                        .select { filter { isIn("post_id", originalIds) } }
                        .decodeList<PostEntity>()
                    originals.forEach { postDao.insertPost(it) }
                }

                shared.forEach { postDao.insertPost(it) }
            } catch (e: Throwable) { e.printStackTrace() }
        }
        emitAll(postDao.getSharedPosts(userId).map { entities ->
            val pollPostIds = entities.mapNotNull { entity ->
                if (entity.post.poll_question != null) entity.post.post_id else null
            }
            val userVotesMap = if (pollPostIds.isNotEmpty()) {
                postDao.getUserVotes(pollPostIds, userId).associate { it.post_id to it.selected_option_index }
            } else emptyMap()
            entities.map { entity ->
                var post = entity.toDomain()
                if (post.content.isNullOrBlank() && post.mediaUrls.isEmpty() && post.sharedPostId != null) {
                    val original = postDao.getPostById(post.sharedPostId!!)
                    if (original != null) {
                        val origPost = original.toDomain()
                        post = post.copy(
                            content = origPost.content,
                            mediaUrls = origPost.mediaUrls
                        )
                    }
                }
                if (post.pollQuestion != null) {
                    val counts = parseIntList(entity.post.poll_vote_counts)
                    val userVote = userVotesMap[post.id]
                    post = post.copy(pollVoteCounts = counts, userSelectedOptionIndex = userVote)
                }
                post
            }
        })
    }

    private fun PostgrestFilterBuilder.isNotNull(column: String) {
        filterNot(column, FilterOperator.IS, null)
    }

}
