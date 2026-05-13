package com.social.flare.features.post.presentation.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.social.flare.features.feed.presentation.components.VideoPlayer

@Composable
fun AddPostMediaPreview(
    mediaUris: List<Uri>,
    onRemoveMedia: (Uri) -> Unit
) {
    val context = LocalContext.current
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 56.dp)
    ) {
        items(mediaUris) { uri ->
            val mimeType = context.contentResolver.getType(uri)
            val isVideo = mimeType?.startsWith("video") == true

            Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp))) {
                if (isVideo) {
                    VideoPlayer(
                        videoUrl = uri.toString(),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd).padding(4.dp).size(24.dp)
                        .clip(CircleShape).background(Color.Black.copy(alpha = 0.6f))
                        .clickable { onRemoveMedia(uri) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}