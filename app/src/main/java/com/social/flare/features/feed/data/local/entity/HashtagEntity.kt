package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "hashtags", indices = [Index("name")])
data class HashtagEntity(
    @PrimaryKey val tag_id: String,
    val name: String,
    val created_at: Long
)