package com.social.flare.features.feed.domain.repository

import com.social.flare.features.feed.domain.model.FlareTrack

interface MusicRepository {
    suspend fun searchMusic(query: String): Result<List<FlareTrack>>
}