package com.social.flare.features.feed.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.social.flare.features.auth.data.local.entity.CitizenEntity

@Entity(
    tableName = "saved_post_table",
    primaryKeys = ["citizen_id", "post_id"],
    foreignKeys = [
        ForeignKey(
            entity = CitizenEntity::class,
            parentColumns = ["citizen_id"],
            childColumns = ["citizen_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["post_id"],
            childColumns = ["post_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("post_id"), Index("citizen_id")]
)
data class SavedPostEntity(
    val citizen_id: String,
    val post_id: String,
    val saved_at: Long = System.currentTimeMillis()
)