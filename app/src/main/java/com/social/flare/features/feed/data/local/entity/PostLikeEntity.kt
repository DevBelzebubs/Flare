package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity

@Entity(tableName = "post_likes", primaryKeys = ["post_id", "citizen_id"])
data class PostLikeEntity(
    val post_id: String,
    val citizen_id: String
)