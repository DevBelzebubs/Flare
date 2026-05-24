package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "saved_post_table",
    primaryKeys = ["citizen_id", "post_id"],
    indices = [Index("post_id"), Index("citizen_id")]
)
data class SavedPostEntity(
    val citizen_id: String,
    val post_id: String,
    val saved_at: Long = System.currentTimeMillis()
)