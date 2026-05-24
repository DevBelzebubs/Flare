package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "post_media_table")
data class PostMediaEntity(
    @PrimaryKey val media_id: String,
    val post_id: String,
    val media_uri: String,
    val media_type: String,
    val order_index: Int
)