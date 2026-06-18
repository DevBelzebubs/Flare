package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "story_table",
    indices = [Index("author_id")]
)
data class StoryEntity(
    @PrimaryKey val story_id: String,
    val author_id: String,
    val media_url: String,
    val created_at: Long,
    val expires_at: Long,
    val is_viewed: Boolean = false,
    val music_url: String? = null
)