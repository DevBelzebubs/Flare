package com.social.flare.features.ai.data.repository.dto

import kotlinx.serialization.Serializable

@Serializable
data class AiPersonaFetchDto(
    val citizen_id: String,
    val system_prompt: String,
    val temperature: Double,
    val is_active: Boolean? = null,
    val citizens: CitizenMinimalDto? = null
)

@Serializable
data class CitizenMinimalDto(
    val username: String,
    val display_name: String
)