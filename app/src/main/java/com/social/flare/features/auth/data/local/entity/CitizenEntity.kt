package com.social.flare.features.auth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(tableName = "citizen_table")
data class CitizenEntity(
    @PrimaryKey
    val citizen_id: String,
    val email: String = "",
    val username: String,
    val display_name: String,
    @Transient
    val password: String = "",
    val avatar_url: String? = null,
    val banner_url: String? = null,
    val bio: String? = null,
    val is_admin: Boolean = false,
    val status: String = "active"
)