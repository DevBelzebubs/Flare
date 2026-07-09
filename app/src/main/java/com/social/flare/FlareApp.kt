package com.social.flare

import android.app.Application
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.room.Room
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.cloudinary.android.MediaManager
import com.social.flare.core.database.FlareDatabase
import dagger.hilt.android.HiltAndroidApp
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltAndroidApp
class FlareApp : Application(), ImageLoaderFactory, Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private val initDeferred = CompletableDeferred<Unit>()
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    var isInitialized = false
        private set

    val database: FlareDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            FlareDatabase::class.java,
            "flare_offline_database"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    val supabase: SupabaseClient by lazy {
        val rawUrl = BuildConfig.SUPABASE_URL
        val cleanUrl = rawUrl.substringBefore("/rest/v1").substringBefore("/auth/v1").substringBefore("/realtime/v1")
        createSupabaseClient(
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
            httpEngine = OkHttp.create()
        }
    }

    suspend fun awaitInitialization() {
        initDeferred.await()
    }

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("FlareApp", "UNCAUGHT EXCEPTION on thread: ${thread.name}", throwable)
        }

        applicationScope.launch {
            try {
                database
                supabase

                val config = mapOf(
                    "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                    "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                    "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
                )
                try {
                    MediaManager.init(this@FlareApp, config)
                } catch (e: Exception) {
                    android.util.Log.e("FlareApp", "Cloudinary init failed", e)
                }
            } catch (e: Exception) {
                android.util.Log.e("FlareApp", "App initialization failed", e)
            } finally {
                isInitialized = true
                if (!initDeferred.isCompleted) initDeferred.complete(Unit)
            }
        }
    }

    // applicationScope lives for the app process lifetime (never explicitly cancelled,
    // since Application.onTerminate() is never called on real Android devices).

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
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
            .build()
    }
}