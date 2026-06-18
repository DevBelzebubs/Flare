package com.social.flare.features.feed.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class FlareTrack(
    val id: String,
    val title: String,
    val artist: String,
    val coverUrl: String,
    val previewUrl: String
)
