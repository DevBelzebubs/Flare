package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "post_votes",
    primaryKeys = ["post_id", "citizen_id"],
    indices = [Index("post_id"), Index("citizen_id")]
)
data class PostVoteEntity(
    val post_id: String,
    val citizen_id: String,
    val selected_option_index: Int
)
