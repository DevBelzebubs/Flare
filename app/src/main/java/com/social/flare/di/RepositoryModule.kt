package com.social.flare.di

import com.social.flare.FlareApp
import com.social.flare.core.media.CloudinaryService
import com.social.flare.features.admin.data.repository.AdminRepositoryImpl
import com.social.flare.features.admin.domain.repository.AdminRepository
import com.social.flare.features.ai.data.repository.AiAgentRepositoryImpl
import com.social.flare.features.ai.domain.repository.AiAgentRepository
import com.social.flare.features.feed.data.repository.FeedRepositoryImpl
import com.social.flare.features.feed.data.repository.MusicRepositoryImpl
import com.social.flare.features.feed.data.repository.StoryRepositoryImpl
import com.social.flare.features.feed.domain.repository.FeedRepository
import com.social.flare.features.feed.domain.repository.MusicRepository
import com.social.flare.features.feed.domain.repository.StoryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAiAgentRepository(impl: AiAgentRepositoryImpl): AiAgentRepository
    @Binds
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository
    companion object {
        @Provides
        @Singleton
        fun provideFeedRepository(
            app: FlareApp,
            supabase: SupabaseClient
        ): FeedRepository {
            return FeedRepositoryImpl(
                app.database.postDao(),
                app.database.citizenDao(),
                app.database.followDao(),
                supabase
            )
        }

        @Provides
        @Singleton
        fun provideCloudinaryService(
            app: FlareApp
        ): CloudinaryService {
            return CloudinaryService(app.applicationContext)
        }

        @Provides
        @Singleton
        fun provideStoryRepository(
            app: FlareApp,
            supabase: SupabaseClient,
            cloudinaryService: CloudinaryService
        ): StoryRepository {
            return StoryRepositoryImpl(
                storyDao = app.database.storyDao(),
                citizenDao = app.database.citizenDao(),
                cloudinaryService = cloudinaryService,
                supabase = supabase
            )
        }
        @Provides
        @Singleton
        fun provideAdminRepository(
            app: FlareApp,
            supabase: SupabaseClient,
            cloudinaryService: CloudinaryService
        ): AdminRepository {
            return AdminRepositoryImpl(
                citizenDao = app.database.citizenDao(),
                postDao = app.database.postDao(),
                newsDao = app.database.newsDao(),
                supabase = supabase,
                cloudinaryService = cloudinaryService
            )
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AiRepositoryEntryPoint {
    fun aiAgentRepository(): AiAgentRepository
}