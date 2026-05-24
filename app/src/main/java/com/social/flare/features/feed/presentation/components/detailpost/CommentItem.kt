package com.social.flare.features.feed.presentation.components.detailpost

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.social.flare.core.utils.TimeUtils.formatRelativeTime
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.feed.presentation.components.VideoPlayer

@Composable
fun CommentItem(
    post: Post,
    isNestedReply: Boolean,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onImageClick: (String) -> Unit,
    onBodyClick: () -> Unit = {},
    onAuthorClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (isNestedReply) {
                    val lineX = 56.dp.toPx() + 1.dp.toPx()
                    val lineTop = 12.dp.toPx()
                    val lineBottom = size.height - 12.dp.toPx()

                    drawLine(
                        color = Color(0xFF2A2A2A),
                        start = Offset(x = lineX, y = lineTop),
                        end = Offset(x = lineX, y = lineBottom),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
            .clickable { onBodyClick() }
            .padding(
                start = if (isNestedReply) 70.dp else 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            )
    ) {

        AsyncImage(
            model = post.authorAvatarUrl,
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(if (isNestedReply) 32.dp else 40.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
                .clickable { onAuthorClick(post.authorId) }
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onAuthorClick(post.authorId) }
            ) {
                Text(post.authorDisplayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("• ${formatRelativeTime(post.createdAt)}", color = Color.Gray, fontSize = 13.sp)
            }
            Text(post.authorUsername, color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(post.content ?: "", color = Color.White, fontSize = 14.sp, lineHeight = 18.sp)

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
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = "Comment image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onImageClick(mediaUrl) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = { onLikeClick() })
                        .padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLikedByMe) Color(0xFFFF5722) else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${post.likesCount}", color = Color.Gray, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = { onReplyClick() })
                        .padding(end = 4.dp)
                ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Reply", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${post.commentsCount}", color = Color.Gray, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Reply",
                    color = Color(0xFFFF5722),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = { onReplyClick() })
                        .padding(4.dp)
                )
            }
        }
    }
}