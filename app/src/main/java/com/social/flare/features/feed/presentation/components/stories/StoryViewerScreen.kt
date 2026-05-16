package com.social.flare.features.feed.presentation.components.stories

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.social.flare.features.feed.data.local.entity.StoryWithAuthor
import com.social.flare.features.feed.presentation.StoryViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryViewerScreen(
    userStories: List<StoryWithAuthor>,
    activeCitizenId: String?,
    viewModel: StoryViewModel,
    onClose: () -> Unit
) {
    if (userStories.isEmpty()) {
        onClose()
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var replyText by remember { mutableStateOf("") }

    val currentStory = userStories[currentIndex]
    val storyUrl = currentStory.story.media_url
    val avatarUrl = currentStory.authorAvatarUrl
    val username = currentStory.authorUsername
    val timeAgo = getTimeAgo(currentStory.story.created_at)
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(currentIndex) {
        val storyDuration = 5000L
        val interval = 16L
        val totalSteps = storyDuration / interval
        progress = 0f
        for (step in 1..totalSteps) {
            delay(interval)
            progress = step.toFloat() / totalSteps
        }
        if (currentIndex < userStories.size - 1) {
            currentIndex++
        } else {
            onClose()
        }
    }
    LaunchedEffect(currentStory.story.story_id) {
        viewModel.loadCommentsForStory(currentStory.story.story_id)
        activeCitizenId?.let { userId ->
            viewModel.markStoryAsViewed(currentStory.story.story_id, userId)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        if (offset.x < size.width / 2) {
                            if (currentIndex > 0) currentIndex--
                        } else {
                            if (currentIndex < userStories.size - 1) currentIndex++
                            else onClose()
                        }
                    }
                )
            }
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AsyncImage(
                model = storyUrl,
                contentDescription = "Background Blur",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 60.dp)
                    .background(Color.Black.copy(alpha = 0.4f))
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212)))
        }

        AsyncImage(
            model = storyUrl,
            contentDescription = "Story Content",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            StoryProgressBar(
                storiesCount = userStories.size,
                currentIndex = currentIndex,
                progress = progress
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.DarkGray)) {
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))

                Text(text = username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = timeAgo, color = Color.LightGray, fontSize = 14.sp)

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = replyText,
                onValueChange = { replyText = it },
                placeholder = { Text("Add a comment...", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp) },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedBorderColor = Color.White,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = { /* TODO: Like */ }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Like", tint = Color.White, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { /* TODO: Share */ }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Send, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

private fun getTimeAgo(timeInMillis: Long): String {
    val diff = System.currentTimeMillis() - timeInMillis
    val hours = diff / (1000 * 60 * 60)
    val minutes = diff / (1000 * 60)
    return when {
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "Now"
    }
}