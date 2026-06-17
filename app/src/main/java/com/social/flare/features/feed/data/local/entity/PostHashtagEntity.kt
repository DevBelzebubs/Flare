package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "post_hashtags",
    primaryKeys = ["post_id", "tag_id"],
    indices = [Index("post_id"), Index("tag_id")]
)
data class PostHashtagEntity(
    val post_id: String,
    val tag_id: String
)