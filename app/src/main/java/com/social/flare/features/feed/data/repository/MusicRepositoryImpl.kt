package com.social.flare.features.feed.data.repository

import com.social.flare.features.feed.data.remote.dto.ITunesResponse
import com.social.flare.features.feed.domain.model.FlareTrack
import com.social.flare.features.feed.domain.repository.MusicRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor() : MusicRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val httpClient = HttpClient(Android)

    override suspend fun searchMusic(query: String): Result<List<FlareTrack>> = withContext(
        Dispatchers.IO) {
            try {
                val formattedQuery = query.replace(" ", "+")
                val url = "https://itunes.apple.com/search?term=$formattedQuery&media=music&entity=song&limit=15"
                val response = httpClient.get(url)
                val body = response.bodyAsText()
                val itunesResponse = json.decodeFromString<ITunesResponse>(body)
                val tracks = itunesResponse.results.mapNotNull { dto ->
                    if (dto.previewUrl != null && dto.trackName != null) {
                        FlareTrack(
                            id = dto.trackId?.toString() ?: "",
                            title = dto.trackName,
                            artist = dto.artistName ?: "Unknown Artist",
                            coverUrl = dto.artworkUrl100?.replace("100x100", "300x300")
                                ?: "",
                            previewUrl = dto.previewUrl
                        )
                    } else null
                }
                Result.success(tracks)
            } catch (e: Exception){
                e.printStackTrace()
                Result.failure(e)
            }
    }
}