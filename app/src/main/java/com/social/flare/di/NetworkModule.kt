package com.social.flare.di

import com.social.flare.BuildConfig
import com.social.flare.features.ai.data.remote.HuggingFaceApi
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
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val OPEN_ROUTER_BASE_URL = "https://openrouter.ai/"

    @Provides
    @Singleton
    @Named("openrouter")
    fun provideOpenRouterOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
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
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    @Provides
    @Singleton
    fun provideOpenRouterApi(@Named("openrouter") client: OkHttpClient): OpenRouterApi {
        return Retrofit.Builder()
            .baseUrl(OPEN_ROUTER_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenRouterApi::class.java)
    }
    @Provides
    @Singleton
    @Named("huggingface")
    fun provideHuggingFaceOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideHuggingFaceApi(@Named("huggingface") client: OkHttpClient): HuggingFaceApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api-inference.huggingface.co/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(HuggingFaceApi::class.java)
    }
}