package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "post_likes", primaryKeys = ["post_id", "citizen_id"], indices = [Index("post_id"), Index("citizen_id")])
data class PostLikeEntity(
    val post_id: String,
    val citizen_id: String
)