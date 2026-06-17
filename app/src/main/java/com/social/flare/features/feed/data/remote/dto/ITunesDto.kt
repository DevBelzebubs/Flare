package com.social.flare.features.feed.data.remote.dto
import kotlinx.serialization.Serializable
@Serializable
data class ITunesResponse(
    val results: List<ITunesTrackDto>
)

@Serializable
data class ITunesTrackDto(
    val trackId: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val artworkUrl100: String? = null,
    val previewUrl: String? = null
)