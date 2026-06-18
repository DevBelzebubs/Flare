package com.social.flare.features.feed.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.social.flare.features.feed.data.local.entity.StoryWithAuthor

@Composable
fun StoryCarousel(
    modifier: Modifier = Modifier,
    activeUserAvatarUrl: String? = null,
    activeUserUsername: String? = null,
    stories: List<StoryWithAuthor>,
    onAddStoryClick: () -> Unit = {},
    onStoryClick: (String) -> Unit = {}
) {
    val myStories = stories.filter { it.authorUsername == activeUserUsername }
    val otherStories = stories.filter { it.authorUsername != activeUserUsername }

    val groupedStories = otherStories.groupBy { it.authorUsername }

    val hasStories = myStories.isNotEmpty()
    val hasUnviewed = myStories.any { !it.isViewedByMe }

    LazyRow(
        modifier = modifier.padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AddStoryItem(
                avatarUrl = activeUserAvatarUrl,
                hasStories = hasStories,
                hasUnviewed = hasUnviewed,
                onAddClick = onAddStoryClick,
                onViewClick = { activeUserUsername?.let { onStoryClick(it) } }
            )
        }

        items(groupedStories.keys.toList()) { username ->
            val userStories = groupedStories[username] ?: emptyList()
            val otherHasUnviewed = userStories.any { !it.isViewedByMe }

            StoryItem(
                username = username,
                avatarUrl = userStories.firstOrNull()?.authorAvatarUrl,
                hasUnviewedStory = otherHasUnviewed,
                onClick = { onStoryClick(username) }
            )
        }
    }
}

@Composable
private fun AddStoryItem(
    avatarUrl: String?,
    hasStories: Boolean,
    hasUnviewed: Boolean,
    onAddClick: () -> Unit,
    onViewClick: () -> Unit
) {
    val ringBrush = if (hasUnviewed) {
        Brush.verticalGradient(colors = listOf(Color(0xFFFF5722), Color(0xFFFF9800)))
    } else {
        SolidColor(MaterialTheme.colorScheme.outline)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.size(68.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (hasStories) {
                            Modifier
                                .border(width = 2.dp, brush = ringBrush, shape = CircleShape)
                                .padding(4.dp)
                        } else {
                            Modifier
                        }
                    )
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        if (hasStories) onViewClick() else onAddClick()
                    }
            ) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Your avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(22.dp)
                    .offset(x = 2.dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(2.dp)
                    .clickable { onAddClick() }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Story",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your story",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StoryItem(username: String, avatarUrl: String? = null, hasUnviewedStory: Boolean, onClick: () -> Unit) {
    val ringBrush = if (hasUnviewedStory) {
        Brush.verticalGradient(colors = listOf(Color(0xFFFF5722), Color(0xFFFF9800)))
    } else {
        SolidColor(MaterialTheme.colorScheme.outline)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .border(width = 2.dp, brush = ringBrush, shape = CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onClick() }
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "$username avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = username,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
