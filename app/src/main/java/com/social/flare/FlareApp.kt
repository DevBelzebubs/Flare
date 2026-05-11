package com.social.flare

import android.app.Application
import androidx.room.Room
import com.social.flare.core.database.FlareDatabase

class FlareApp : Application() {

    lateinit var database: FlareDatabase
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            FlareDatabase::class.java,
            "flare_offline_database"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }
}