package com.social.flare.features.feed.data.mapper

import com.social.flare.features.feed.data.local.dao.PostWithDetails
import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.feed.domain.model.Post
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.JsonNull

private val json = Json { ignoreUnknownKeys = true }

fun parseIntList(raw: String?): List<Int>? {
    if (raw.isNullOrBlank()) return null
    return try {
        json.decodeFromJsonElement<List<Int>>(json.parseToJsonElement(raw))
    } catch (_: Exception) {
        null
    }
}

fun List<Int>.toVoteCountsJson(): String? {
    if (isEmpty()) return null
    return buildJsonArray { this@toVoteCountsJson.forEach { add(JsonPrimitive(it)) } }.toString()
}

fun parsePollOptions(raw: String?): List<String>? {
    if (raw.isNullOrBlank()) return null
    return try {
        json.decodeFromJsonElement<List<String>>(json.parseToJsonElement(raw))
    } catch (_: Exception) {
        null
    }
}

fun List<String>.toPollOptionsJson(): String? {
    if (isEmpty()) return null
    return buildJsonArray { this@toPollOptionsJson.forEach { add(JsonPrimitive(it)) } }.toString()
}

fun PostWithDetails.toDomain(): Post {
    val mediaList = this.post.media_urls?.takeIf { it.isNotBlank() }?.split(",") ?: emptyList()

    return Post(
        id = this.post.post_id,
        authorId = this.post.author_id,
        authorDisplayName = this.authorDisplayName,
        authorUsername = this.authorUsername,
        authorAvatarUrl = this.authorAvatarUrl,
        content = this.post.content,
        mediaUrls = mediaList,
        parentPostId = this.post.parent_post_id,
        createdAt = this.post.created_at,
        likesCount = this.likesCount,
        commentsCount = this.commentsCount,
        isLikedByMe = this.isLikedByMe,
        isSavedByMe = this.isSavedByMe,
        isSharedByMe = this.isSharedByMe,
        sharedPostId = this.post.shared_post_id,
        sharesCount = this.sharesCount,
        pollQuestion = this.post.poll_question,
        pollOptions = parsePollOptions(this.post.poll_options),
        pollExpiresAt = this.post.poll_expires_at,
        pollVoteCounts = parseIntList(this.post.poll_vote_counts),
        locationName = this.post.location_name,
        locationLat = this.post.location_lat,
        locationLng = this.post.location_lng
    )
}

fun PostEntity.toDomainModel(activeUserId: String): Post {
    val mediaList = this.media_urls?.takeIf { it.isNotBlank() }?.split(",") ?: emptyList()

    return Post(
        id = this.post_id,
        authorId = this.author_id,
        authorDisplayName = "Usuario",
        authorUsername = "@usuario",
        authorAvatarUrl = null,
        content = this.content,
        mediaUrls = mediaList,
        parentPostId = this.parent_post_id,
        createdAt = this.created_at,
        likesCount = 0,
        commentsCount = 0,
        isLikedByMe = false,
        isSavedByMe = false,
        sharedPostId = this.shared_post_id,
        pollQuestion = this.poll_question,
        pollOptions = parsePollOptions(this.poll_options),
        pollExpiresAt = this.poll_expires_at,
        pollVoteCounts = parseIntList(this.poll_vote_counts),
        locationName = this.location_name,
        locationLat = this.location_lat,
        locationLng = this.location_lng
    )
}