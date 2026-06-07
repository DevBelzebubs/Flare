package com.social.flare.features.ai.domain.model

data class AiPersona(
    val citizenId: String,
    val username: String,
    val displayName: String,
    val systemPrompt: String,
    val temperature: Double = 0.7
)