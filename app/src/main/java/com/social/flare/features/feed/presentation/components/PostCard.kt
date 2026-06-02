package com.social.flare.features.feed.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.feed.presentation.FeedEvent
import com.social.flare.features.feed.presentation.components.LocationDisplay
import com.social.flare.features.feed.presentation.components.PollDisplay

@Composable
fun PostCard(
    post: Post,
    activeCitizenId: String?,
    modifier: Modifier = Modifier,
    onEvent: (FeedEvent) -> Unit = {},
    onImageClick: (String) -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editContentText by remember { mutableStateOf(post.content ?: "") }
    val isOwner = post.authorId == activeCitizenId

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onEvent(FeedEvent.OnPostClick(post.id)) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF121212)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            PostHeader(
                post = post, // <-- Pasamos el post entero al Header
                isOwner = isOwner,
                menuExpanded = menuExpanded,
                onMenuExpandedChange = { menuExpanded = it },
                onEditClick = {
                    menuExpanded = false
                    editContentText = post.content ?: ""
                    showEditDialog = true
                },
                onDeleteClick = {
                    menuExpanded = false
                    showDeleteDialog = true
                },
                onAuthorClick = {
                    onEvent(FeedEvent.OnAuthorClick(post.authorId))
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            post.content?.let { contentText ->
                Text(
                    text = contentText,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                if (post.mediaUrls.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val mediaUrl = post.mediaUrls.first()
                    val isVideo = mediaUrl.endsWith(".mp4", ignoreCase = true) ||
                            mediaUrl.endsWith(".webm", ignoreCase = true) ||
                            mediaUrl.endsWith(".mkv", ignoreCase = true) ||
                            mediaUrl.endsWith(".mov", ignoreCase = true)
                    if (isVideo) {
                        VideoPlayer(videoUrl = mediaUrl)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 450.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onImageClick(mediaUrl) }
                        ) {
                            AsyncImage(
                                model = mediaUrl,
                                contentDescription = "Post image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!post.pollQuestion.isNullOrBlank() && !post.pollOptions.isNullOrEmpty()) {
                PollDisplay(
                    question = post.pollQuestion,
                    options = post.pollOptions,
                    voteCounts = post.pollVoteCounts,
                    userSelectedOptionIndex = post.userSelectedOptionIndex,
                    onVote = { optionIndex -> onEvent(FeedEvent.OnVoteClick(post.id, optionIndex)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (!post.locationName.isNullOrBlank()) {
                LocationDisplay(
                    locationName = post.locationName,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            PostStats(
                likesCount = post.likesCount,
                commentsCount = post.commentsCount,
                isLikedByMe = post.isLikedByMe
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(4.dp))

            PostActionButtons(
                isLikedByMe = post.isLikedByMe,
                isSavedByMe = post.isSavedByMe,
                isSharedByMe = post.isSharedByMe,
                onLikeClick = { onEvent(FeedEvent.OnLikeClick(post.id)) },
                onCommentClick = { onEvent(FeedEvent.OnCommentClick(post.id)) },
                onSaveClick = { onEvent(FeedEvent.OnSaveClick(post.id)) },
                onShareClick = { onEvent(FeedEvent.OnShareClick(post.id)) },
            )
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Editar Publicación", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = editContentText,
                    onValueChange = { if (it.length <= 500) editContentText = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFF5722)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(FeedEvent.OnEditPost(post.id, editContentText))
                    showEditDialog = false
                }) {
                    Text("Guardar", color = Color(0xFFFF5722))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Eliminar Publicación", color = Color.White) },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar esta publicación? Esta acción no se puede deshacer.",
                    color = Color.LightGray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(FeedEvent.OnDeletePost(post.id))
                    showDeleteDialog = false
                }) {
                    Text("Eliminar", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
private fun PostHeader(
    post: Post,
    isOwner: Boolean,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAuthorClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = post.authorAvatarUrl,
            contentDescription = "Avatar de ${post.authorUsername}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
                .clickable { onAuthorClick() }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onAuthorClick() }
        ) {
            Text(
                text = post.authorDisplayName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = post.authorUsername,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        if (isOwner) {
            Box {
                IconButton(onClick = { onMenuExpandedChange(true) }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = Color.Gray
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { onMenuExpandedChange(false) },
                    modifier = Modifier.background(Color(0xFF1E1E1E))
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar", color = Color.White) },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color.White) },
                        onClick = onEditClick
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color.Red) },
                        onClick = onDeleteClick
                    )
                }
            }
        }
    }
}

@Composable
private fun PostStats(likesCount: Int, commentsCount: Int, isLikedByMe: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Likes",
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
    isSavedByMe: Boolean,
    isSharedByMe: Boolean,
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
            AnimatedLikeButton(
                isLiked = isLikedByMe,
                onClick = onLikeClick
            )

            IconButton(onClick = onCommentClick) {
                Icon(
                    Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comment",
                    tint = Color.White
                )
            }

            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = if (isSharedByMe) Icons.Filled.Repeat else Icons.Outlined.Repeat,
                    contentDescription = "Repost",
                    tint = if (isSharedByMe) Color(0xFFFF5722) else Color.White
                )
            }
        }
        IconButton(
            onClick = onSaveClick
        ) {
            Icon(
                imageVector = if (isSavedByMe) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = "Save Post",
                tint = if (isSavedByMe) Color(0xFFFF5722) else Color.White
            )
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