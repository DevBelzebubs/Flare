package com.social.flare.features.auth.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "citizen_table")
data class CitizenEntity(
    @PrimaryKey
    val citizen_id: String,
    val username: String,
    val display_name: String,
    val avatar_url: String?,
    val bio: String?
)