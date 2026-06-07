package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "post_hashtags",
    primaryKeys = ["post_id", "tag_id"],
)
data class PostHashtagEntity(
    val post_id: String,
    val tag_id: String
)