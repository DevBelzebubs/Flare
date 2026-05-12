package com.social.flare.features.profile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.social.flare.features.feed.domain.model.Post

@Composable
fun ProfileGridItem(post: Post, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(0.5.dp, Color.Black)
            .clickable { onClick() }
            .background(Color(0xFF1E1E1E)),
        contentAlignment = Alignment.Center
    ) {
        if (post.mediaUrls.isNotEmpty()) {
            val mediaUrl = post.mediaUrls.first()
            val isVideo = mediaUrl.endsWith(".mp4", ignoreCase = true) ||
                    mediaUrl.endsWith(".webm", ignoreCase = true)
            AsyncImage(
                model = mediaUrl.replace(".mp4", ".jpg"),
                contentDescription = "Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (isVideo) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Video",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp).align(Alignment.TopEnd).padding(4.dp)
                )
            }
        } else if (!post.content.isNullOrBlank()) {
            Text(
                text = post.content,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}