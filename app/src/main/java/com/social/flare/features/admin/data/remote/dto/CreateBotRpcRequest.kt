package com.social.flare.features.admin.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateBotRpcRequest(
    val bot_username: String,
    val bot_display_name: String,
    val bot_system_prompt: String,
    val bot_temperature: Double
)