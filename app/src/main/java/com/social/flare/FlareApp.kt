package com.social.flare

import android.app.Application
import androidx.room.Room
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.cloudinary.android.MediaManager
import com.social.flare.core.database.FlareDatabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

class FlareApp : Application(), ImageLoaderFactory {

    lateinit var database: FlareDatabase
        private set

    lateinit var supabase: SupabaseClient
        private set

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("FlareApp", "UNCAUGHT EXCEPTION on thread: ${thread.name}", throwable)
        }

        database = Room.databaseBuilder(
            applicationContext,
            FlareDatabase::class.java,
            "flare_offline_database"
        )
            .fallbackToDestructiveMigration(true)
            .build()

        val rawUrl = BuildConfig.SUPABASE_URL
        val cleanUrl = rawUrl.substringBefore("/rest/v1").substringBefore("/auth/v1").substringBefore("/realtime/v1")
        supabase = createSupabaseClient(
            supabaseUrl = cleanUrl,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            defaultSerializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
                coerceInputValues = true
            })
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }

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

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("flare_image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}