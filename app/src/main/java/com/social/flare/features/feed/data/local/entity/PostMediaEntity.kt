package com.social.flare.features.feed.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "post_media_table")
data class PostMediaEntity(
    @PrimaryKey val media_id: String,
    val post_id: String,
    val media_uri: String,
    val media_type: String,
    val order_index: Int
)