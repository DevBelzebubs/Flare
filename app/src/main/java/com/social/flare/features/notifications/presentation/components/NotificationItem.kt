package com.social.flare.features.notifications.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.social.flare.core.utils.TimeUtils
import com.social.flare.features.notifications.domain.model.FlareNotification
import com.social.flare.features.notifications.domain.model.NotificationType

@Composable
fun NotificationItem(
    notification: FlareNotification,
    isFollowingBack: Boolean = false,
    onClick: () -> Unit,
    onFollowClick: () -> Unit,
    onAvatarClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!notification.isRead) Color(0xFF121212) else Color.Black) // Fondo ligerísimamente gris si no está leída
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp)) {
            AsyncImage(
                model = notification.actorAvatarUrl,
                contentDescription = "Avatar de ${notification.actorUsername}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .clickable { onAvatarClick(notification.actorId) }
            )
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3))
                        .align(Alignment.TopEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        val text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                append(notification.actorUsername)
            }
            withStyle(style = SpanStyle(color = Color.LightGray)) {
                when (notification.type) {
                    NotificationType.LIKE -> append(" le gusta tu post.")
                    NotificationType.FOLLOW -> append(" empezó a seguirte.")
                    NotificationType.COMMENT -> append(" comentó tu post: ${notification.extraText ?: ""}")
                }
            }
            withStyle(style = SpanStyle(color = Color.Gray)) {
                val timeAgo = TimeUtils.getTimeAgo(notification.createdAt)
                append(" · $timeAgo")
            }
        }

        Text(
            text = text,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        if (notification.type == NotificationType.FOLLOW) {
            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowingBack) Color.DarkGray else Color(0xFFFF5722)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = if (isFollowingBack) "Siguiendo" else "Seguir",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        } else if (notification.referencedPostMediaUrl != null) {
            AsyncImage(
                model = notification.referencedPostMediaUrl,
                contentDescription = "Post thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.DarkGray)
            )
        }
    }
}