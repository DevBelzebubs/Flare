package com.social.flare.features.feed.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.flare.features.feed.domain.model.Post

@Composable
fun PostCard(
    post: Post,
    modifier: Modifier = Modifier,
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF121212)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            PostHeader(
                displayName = post.authorDisplayName,
                username = post.authorUsername
            )

            Spacer(modifier = Modifier.height(12.dp))

            post.content?.let { contentText ->
                Text(
                    text = contentText,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            PostStats(likesCount = post.likesCount, commentsCount = post.commentsCount)

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(4.dp))

            PostActionButtons(commentsCount = post.commentsCount, onLikeClick = onLikeClick, onCommentClick = onCommentClick,
                onSaveClick = onSaveClick, onShareClick = onShareClick)
        }
    }
}

@Composable
private fun PostHeader(displayName: String, username: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF5722))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = username,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        IconButton(onClick = { /* Opciones */ }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Opciones",
                tint = Color.Gray
            )
        }
    }
}

@Composable
private fun PostStats(likesCount: Int, commentsCount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Likes",
            tint = Color(0xFFE91E63),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$likesCount",
            color = Color.Gray,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.width(20.dp))

        Icon(
            imageVector = Icons.Outlined.ChatBubbleOutline,
            contentDescription = "Comentarios",
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$commentsCount",
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun PostActionButtons(commentsCount: Int, onLikeClick: () -> Unit,
                              onCommentClick: () -> Unit, onSaveClick: () -> Unit, onShareClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onLikeClick) {
                Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Like", tint = Color.White)
            }
            IconButton(onClick = onCommentClick) {
                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comment", tint = Color.White)
            }
            IconButton(onClick = onShareClick) {
                Icon(Icons.Outlined.Send, contentDescription = "Share", tint = Color.White)
            }
        }

        IconButton(onClick = onSaveClick) {
            Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Save", tint = Color.White)
        }
    }
}