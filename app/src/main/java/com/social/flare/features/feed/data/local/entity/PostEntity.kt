package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "post_table",
    indices = [Index("author_id"), Index("parent_post_id")],
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
    val shared_post_id: String? = null
)