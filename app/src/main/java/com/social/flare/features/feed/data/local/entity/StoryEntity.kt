package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.social.flare.features.auth.data.local.entity.CitizenEntity
@Entity(
    tableName = "story_table",
    foreignKeys = [
        ForeignKey(
            entity = CitizenEntity::class,
            parentColumns = ["citizen_id"],
            childColumns = ["author_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("author_id")]
)
data class StoryEntity(
    @PrimaryKey val story_id: String,
    val author_id: String,
    val media_url: String,
    val created_at: Long,
    val is_viewed: Boolean = false
)