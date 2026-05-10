package com.social.flare.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
@Database(
    entities = [
        CitizenEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FlareDatabase : RoomDatabase() {
    abstract fun citizenDao(): CitizenDao
}