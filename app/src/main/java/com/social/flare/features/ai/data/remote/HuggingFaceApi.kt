package com.social.flare.features.ai.data.remote

import com.social.flare.features.ai.data.remote.dto.HuggingFaceRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface HuggingFaceApi {
    @POST("models/{modelId}")
    suspend fun generateImage(
        @Path("modelId") modelId: String = "stabilityai/stable-diffusion-xl-base-1.0",
        @Header("Authorization") authorization: String,
        @Body request: HuggingFaceRequest
    ): Response<ResponseBody>
}