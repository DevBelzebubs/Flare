package com.social.flare.features.ai.data.remote

import com.social.flare.features.ai.data.remote.dto.OpenRouterRequest
import com.social.flare.features.ai.data.remote.dto.OpenRouterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenRouterApi {

    @POST("api/v1/chat/completions")
    suspend fun generateCompletion(
        @Body request: OpenRouterRequest
    ): Response<OpenRouterResponse>
}