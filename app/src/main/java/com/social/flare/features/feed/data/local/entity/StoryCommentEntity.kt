package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.social.flare.features.auth.data.local.entity.CitizenEntity
@Entity(
    tableName = "story_comment_table",
    foreignKeys = [
        ForeignKey(
            entity = StoryEntity::class,
            parentColumns = ["story_id"],
            childColumns = ["story_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CitizenEntity::class,
            parentColumns = ["citizen_id"],
            childColumns = ["author_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("story_id"),
        Index("author_id")
    ]
)
data class StoryCommentEntity(
    @PrimaryKey val comment_id: String,
    val story_id: String,
    val author_id: String,
    val content: String,
    val created_at: Long
)