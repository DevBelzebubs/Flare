package com.social.flare.features.feed.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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

            // Pasamos isLikedByMe para que las estadísticas cambien de color
            PostStats(
                likesCount = post.likesCount,
                commentsCount = post.commentsCount,
                isLikedByMe = post.isLikedByMe
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(4.dp))

            // Pasamos isLikedByMe a los botones de acción
            PostActionButtons(
                isLikedByMe = post.isLikedByMe,
                onLikeClick = onLikeClick,
                onCommentClick = onCommentClick,
                onSaveClick = onSaveClick,
                onShareClick = onShareClick
            )
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
private fun PostStats(likesCount: Int, commentsCount: Int, isLikedByMe: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Likes",
            // Si le di like es Naranja Flare, si no, es Gris
            tint = if (isLikedByMe) Color(0xFFFF5722) else Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$likesCount",
            color = if (isLikedByMe) Color(0xFFFF5722) else Color.Gray,
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
private fun PostActionButtons(
    isLikedByMe: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            // ¡AQUÍ REEMPLAZAMOS EL BOTÓN ESTÁTICO POR EL ANIMADO!
            AnimatedLikeButton(
                isLiked = isLikedByMe,
                onClick = onLikeClick
            )

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

@Composable
fun AnimatedLikeButton(
    isLiked: Boolean,
    onClick: () -> Unit
) {
    val tint by animateColorAsState(
        targetValue = if (isLiked) Color(0xFFFF5722) else Color.White,
        animationSpec = tween(durationMillis = 200),
        label = "colorAnimation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scaleAnimation"
    )

    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "Like",
            tint = tint,
            modifier = Modifier.scale(scale)
        )
    }
}