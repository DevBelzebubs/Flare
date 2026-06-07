package com.social.flare.di

import com.social.flare.BuildConfig
import com.social.flare.features.ai.data.remote.OpenRouterApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val OPEN_ROUTER_BASE_URL = "https://openrouter.ai/"

    @Provides
    @Singleton
    fun provideOpenRouterOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val headerInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${BuildConfig.LLAMA_API_KEY}")
                .addHeader("HTTP-Referer", "https://github.com/DevBelzebubs/Flare")
                .addHeader("X-Title", "Flare Social App")
                .build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(logging)
            .build()
    }
    @Provides
    @Singleton
    fun provideOpenRouterApi(client: OkHttpClient): OpenRouterApi {
        return Retrofit.Builder()
            .baseUrl(OPEN_ROUTER_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenRouterApi::class.java)
    }
}