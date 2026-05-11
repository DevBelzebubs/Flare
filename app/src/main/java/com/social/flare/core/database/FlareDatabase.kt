package com.social.flare.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.feed.data.local.dao.PostDao
import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.feed.data.local.entity.PostLikeEntity

@Database(
    entities = [
        CitizenEntity::class,
        PostEntity::class,
        PostLikeEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(MediaConverters::class)
abstract class FlareDatabase : RoomDatabase() {
    abstract fun citizenDao(): CitizenDao
    abstract fun postDao(): PostDao
}