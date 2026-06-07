package com.social.flare.di

import com.social.flare.features.ai.data.repository.AiAgentRepositoryImpl
import com.social.flare.features.ai.domain.repository.AiAgentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAiAgentRepository(impl: AiAgentRepositoryImpl): AiAgentRepository
}
