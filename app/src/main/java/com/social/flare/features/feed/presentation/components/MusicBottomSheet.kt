package com.social.flare.features.feed.presentation.components

import android.media.MediaPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.social.flare.features.feed.domain.model.FlareTrack
import com.social.flare.features.feed.presentation.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicBottomSheet(
    viewModel: MusicViewModel,
    onTrackSelected: (FlareTrack) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val mediaPlayer = remember { MediaPlayer() }
    var currentlyPlayingUrl by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            mediaPlayer.stop()
            onDismiss()
        },
        containerColor = Color(0xFF1E1E1E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.search(it)
                },
                placeholder = { Text("Buscar canción o artista...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.DarkGray,
                    unfocusedContainerColor = Color.DarkGray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.White) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF5722))
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(results, key = { it.id }) { track ->
                        val onPlayPreview = remember(track) { {
                            if (currentlyPlayingUrl == track.previewUrl) {
                                mediaPlayer.pause()
                                currentlyPlayingUrl = null
                            } else {
                                mediaPlayer.reset()
                                mediaPlayer.setDataSource(track.previewUrl)
                                mediaPlayer.prepareAsync()
                                mediaPlayer.setOnPreparedListener {
                                    it.start()
                                    currentlyPlayingUrl = track.previewUrl
                                }
                            }
                        } }
                        val onSelect = remember(track) { {
                            mediaPlayer.stop()
                            onTrackSelected(track)
                        } }
                        MusicTrackItem(
                            track = track,
                            isPlaying = currentlyPlayingUrl == track.previewUrl,
                            onPlayPreview = onPlayPreview,
                            onSelect = onSelect
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MusicTrackItem(
    track: FlareTrack,
    isPlaying: Boolean,
    onPlayPreview: () -> Unit,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.coverUrl,
            contentDescription = null,
            modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(track.artist, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
        }
        IconButton(onClick = onPlayPreview) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Reproducir preview",
                tint = if (isPlaying) Color(0xFFFF5722) else Color.White
            )
        }
    }
}