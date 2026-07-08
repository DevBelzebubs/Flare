package com.social.flare.features.feed.presentation.components.detailpost

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.social.flare.core.utils.TimeUtils.formatRelativeTime
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.feed.presentation.components.LocationDisplay
import com.social.flare.features.feed.presentation.components.PollDisplay
import com.social.flare.features.feed.presentation.components.VideoPlayer

@Composable
fun ParentPostItem(
    post: Post,
    onImageClick: (String) -> Unit,
    onBodyClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onVoteClick: ((Int) -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable { onBodyClick() }
            .padding(horizontal = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            AsyncImage(
                model = post.authorAvatarUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onAuthorClick(post.authorId) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outline)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onAuthorClick(post.authorId) }
                .padding(top = 16.dp, bottom = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    post.authorUsername,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "• ${formatRelativeTime(post.createdAt)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                post.content ?: "",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )

            if (post.mediaUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val mediaUrl = post.mediaUrls.first()
                val isVideo = mediaUrl.endsWith(".mp4", ignoreCase = true) ||
                        mediaUrl.endsWith(".webm", ignoreCase = true) ||
                        mediaUrl.endsWith(".mkv", ignoreCase = true) ||
                        mediaUrl.endsWith(".mov", ignoreCase = true)
                if (isVideo) {
                    VideoPlayer(
                        videoUrl = mediaUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                    )
                } else {
                    val isGif = mediaUrl.endsWith(".gif", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .heightIn(max = 350.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onImageClick(mediaUrl) }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(mediaUrl)
                                .apply {
                                    if (isGif) {
                                        allowConversionToBitmap(false)
                                    } else {
                                        crossfade(true)
                                    }
                                }
                                .build(),
                            contentDescription = "Post image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isGif) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "GIF",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (!post.pollQuestion.isNullOrBlank() && !post.pollOptions.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PollDisplay(
                    question = post.pollQuestion,
                    options = post.pollOptions,
                    voteCounts = post.pollVoteCounts,
                    userSelectedOptionIndex = post.userSelectedOptionIndex,
                    onVote = { optionIndex -> onVoteClick?.invoke(optionIndex) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (!post.locationName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                LocationDisplay(
                    locationName = post.locationName,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
