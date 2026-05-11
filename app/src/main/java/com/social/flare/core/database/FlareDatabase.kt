package com.social.flare.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.feed.data.local.entity.PostLikeEntity

@Database(
    entities = [
        CitizenEntity::class,
        PostEntity::class,
        PostLikeEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class FlareDatabase : RoomDatabase() {
    abstract fun citizenDao(): CitizenDao
}