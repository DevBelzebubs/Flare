package com.social.flare.features.feed.presentation.components.detailpost

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.social.flare.features.feed.domain.model.Post

@Composable
public fun MainPostDetail(
    post: Post,
    hasParent: Boolean,
    onImageClick: (String) -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (hasParent) {
                    Box(modifier = Modifier.width(2.dp).height(12.dp).background(Color(0xFF2A2A2A)))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                AsyncImage(
                    model = post.authorAvatarUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.DarkGray)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.padding(top = if (hasParent) 12.dp else 16.dp)) {
                Text(post.authorUsername, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("6h ago", color = Color.Gray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        post.content?.let { content ->
            Text(
                text = content,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (post.mediaUrls.isNotEmpty()) {
            val mediaUrl = post.mediaUrls.first()
            AsyncImage(
                model = mediaUrl,
                contentDescription = "Post image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clickable { onImageClick(mediaUrl) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatColumn("${post.likesCount}", "likes")
            StatColumn("${post.commentsCount}", "comments")
            StatColumn("89", "shares")
        }

        HorizontalDivider(color = Color(0xFF1A1A1A), thickness = 1.dp)

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                IconTextButton(
                    icon = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    text = "Like",
                    tint = if (post.isLikedByMe) Color(0xFFFF5722) else Color.White,
                    onClick = onLikeClick
                )
                IconTextButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    text = "Comment",
                    tint = Color.White,
                    onClick = onCommentClick
                )
                IconTextButton(
                    icon = Icons.Outlined.Send,
                    text = "Share",
                    tint = Color.White,
                    onClick = {}
                )
            }
            Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Save", tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}