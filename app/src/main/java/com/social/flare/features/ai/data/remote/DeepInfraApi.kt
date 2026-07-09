package com.social.flare.features.ai.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DeepInfraApi {
    @POST("openai/images/generations")
    suspend fun generateImage(
        @Body request: DeepInfraRequest
    ): Response<DeepInfraResponse>
}

data class DeepInfraRequest(
    val prompt: String,
    val model: String = "stabilityai/sdxl-turbo",
    val size: String = "1024x1024",
    val n: Int = 1,
    val response_format: String = "b64_json"
)

data class DeepInfraResponse(
    val created: Long,
    val data: List<ImageData>
)

data class ImageData(
    val b64_json: String? = null,
    val url: String? = null
)