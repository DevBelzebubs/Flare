package com.social.flare.features.auth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "citizen_table")
data class CitizenEntity(
    @PrimaryKey
    val citizen_id: String,
    val username: String,
    val display_name: String,
    val password: String,
    val avatar_url: String?,
    val banner_url: String? = null,
    val bio: String?,
    val is_admin: Boolean = false,
    val status: String = "active"
)