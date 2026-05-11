package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "post_table")
data class PostEntity(
    @PrimaryKey val post_id: String,
    val author_id: String,
    val content: String?,
    val media_urls: String,
    val reply_to_post_id: String? = null,
    val created_at: Long,
    val sync_status: Int
)