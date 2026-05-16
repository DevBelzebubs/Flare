package com.social.flare.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.feed.data.local.dao.PostDao
import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.feed.data.local.entity.PostLikeEntity
// IMPORTAMOS LA NUEVA ENTIDAD Y EL DAO DE HISTORIAS
import com.social.flare.features.feed.data.local.dao.StoryDao
import com.social.flare.features.feed.data.local.entity.SavedPostEntity
import com.social.flare.features.feed.data.local.entity.StoryEntity

@Database(
    entities = [
        CitizenEntity::class,
        PostEntity::class,
        PostLikeEntity::class,
        StoryEntity::class,
        SavedPostEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(MediaConverters::class)
abstract class FlareDatabase : RoomDatabase() {
    abstract fun citizenDao(): CitizenDao
    abstract fun postDao(): PostDao

    abstract fun storyDao(): StoryDao
}