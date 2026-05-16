package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "story_view_table",
    primaryKeys = ["story_id", "citizen_id"],
    foreignKeys = [
        ForeignKey(
            entity = StoryEntity::class,
            parentColumns = ["story_id"],
            childColumns = ["story_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("story_id"), Index("citizen_id")]
)
data class StoryViewEntity(
    val story_id: String,
    val citizen_id: String,
    val viewed_at: Long
)