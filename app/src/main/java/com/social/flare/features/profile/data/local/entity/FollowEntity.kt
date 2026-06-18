package com.social.flare.features.profile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "follow_table",
    primaryKeys = ["followerId", "followedId"],
    indices = [Index("followerId"), Index("followedId")]
)
data class FollowEntity(
    val followerId: String,
    val followedId: String,
    val timestamp: Long = System.currentTimeMillis()
)