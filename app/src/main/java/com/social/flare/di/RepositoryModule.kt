package com.social.flare.di

import com.social.flare.FlareApp
import com.social.flare.features.ai.data.repository.AiAgentRepositoryImpl
import com.social.flare.features.ai.domain.repository.AiAgentRepository
import com.social.flare.features.feed.data.repository.FeedRepositoryImpl
import com.social.flare.features.feed.domain.repository.FeedRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAiAgentRepository(impl: AiAgentRepositoryImpl): AiAgentRepository

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
    }
}
