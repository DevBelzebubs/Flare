package com.social.flare

import android.app.Application
import androidx.room.Room
import com.cloudinary.android.MediaManager
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
        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key" to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
        )
        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}