package com.social.flare.features.admin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "news_table")
data class NewsItemEntity(
    @PrimaryKey
    val news_id: String,
    val title: String,
    val description: String,
    val image_url: String? = null,
    val created_at: Long,
    val is_active: Boolean = true
)
