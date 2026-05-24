package com.social.flare.features.profile.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "follow_table",
    primaryKeys = ["followerId", "followedId"],
    foreignKeys = [
        ForeignKey(
            entity = CitizenEntity::class,
            parentColumns = ["citizen_id"],
            childColumns = ["followerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CitizenEntity::class,
            parentColumns = ["citizen_id"],
            childColumns = ["followedId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("followerId"),
        Index("followedId")
    ]
)
data class FollowEntity(
    val followerId: String,
    val followedId: String,
    val timestamp: Long = System.currentTimeMillis()
)