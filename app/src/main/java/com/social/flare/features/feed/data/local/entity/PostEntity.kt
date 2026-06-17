package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "post_table",
    indices = [
        Index("author_id"),
        Index("parent_post_id"),
        Index("shared_post_id"),
        Index("created_at")
    ],
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["post_id"],
            childColumns = ["parent_post_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PostEntity(
    @PrimaryKey val post_id: String,
    val author_id: String,
    val content: String? = null,
    val media_urls: String? = null,
    val created_at: Long,
    val likes_count: Int = 0,
    val comments_count: Int = 0,
    val sync_status: Int = 0,
    val parent_post_id: String? = null,
    val shared_post_id: String? = null,
    val poll_question: String? = null,
    val poll_options: String? = null,
    val poll_expires_at: Long? = null,
    val poll_vote_counts: String? = null,
    val location_name: String? = null,
    val location_lat: Double? = null,
    val location_lng: Double? = null
)