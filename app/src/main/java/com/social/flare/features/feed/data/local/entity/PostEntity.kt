package com.social.flare.features.feed.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "post_table")
data class PostEntity(
    @PrimaryKey val post_id: String,
    val author_id: String,
    val content: String?,
    val created_at: Long,
    val sync_status: Int
)