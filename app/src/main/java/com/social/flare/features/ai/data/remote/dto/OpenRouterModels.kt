package com.social.flare.features.ai.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OpenRouterRequest(
    @SerializedName("model")
    val model: String = "meta-llama/llama-3.2-3b-instruct:free",
    @SerializedName("messages")
    val messages: List<AiMessage>,
    @SerializedName("temperature")
    val temperature: Double = 0.7
)

data class AiMessage(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String
)

data class OpenRouterResponse(
    @SerializedName("id") val id: String,
    @SerializedName("choices") val choices: List<Choice>
)

data class Choice(
    @SerializedName("message") val message: AiMessage,
    @SerializedName("finish_reason") val finishReason: String?
)