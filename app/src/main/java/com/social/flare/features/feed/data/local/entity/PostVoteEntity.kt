package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "post_votes",
    primaryKeys = ["post_id", "citizen_id"]
)
data class PostVoteEntity(
    val post_id: String,
    val citizen_id: String,
    val selected_option_index: Int
)
