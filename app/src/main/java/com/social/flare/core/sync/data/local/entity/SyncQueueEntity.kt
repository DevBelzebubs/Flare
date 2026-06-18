package com.social.flare.core.sync.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue_table")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val operation_type: String,
    val payload_json: String,
    val media_uri: String,
    val created_at: Long,
    var status: String
)