package com.social.flare.di

import android.app.Application
import com.google.gson.Gson
import com.social.flare.FlareApp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideFlareApp(app: Application): FlareApp = app as FlareApp

    @Provides
    fun provideSupabaseClient(app: FlareApp): SupabaseClient = app.supabase

    @Provides
    fun provideGson(): Gson = Gson()
}
