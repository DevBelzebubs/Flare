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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.social.flare.core.utils.TimeUtils.formatRelativeTime
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.feed.presentation.components.LocationDisplay
import com.social.flare.features.feed.presentation.components.PollDisplay
import com.social.flare.features.feed.presentation.components.VideoPlayer

@Composable
public fun MainPostDetail(
    post: Post,
    hasParent: Boolean,
    onImageClick: (String) -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onVoteClick: ((Int) -> Unit)? = null,
    activeUserId: String? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editContentText by remember { mutableStateOf(post.content ?: "") }
    val isOwner = post.authorId == activeUserId

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (hasParent) {
                    Box(modifier = Modifier.width(2.dp).height(12.dp).background(MaterialTheme.colorScheme.outline))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                AsyncImage(
                    model = post.authorAvatarUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f).padding(top = if (hasParent) 12.dp else 16.dp)) {
                Text(post.authorDisplayName, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(post.authorUsername, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("• ${formatRelativeTime(post.createdAt)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }

            if (isOwner) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Opciones",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar", color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                            },
                            onClick = { menuExpanded = false; showEditDialog = true }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            },
                            onClick = { menuExpanded = false; showDeleteDialog = true }
                        )
                    }
                }
            }
        }
        if (post.sharedPostId != null) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Repeat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Publicación compartida",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        post.content?.let { content ->
            Text(
                text = content,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (post.mediaUrls.isNotEmpty()) {
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
                        .heightIn(max = 450.dp)
                )
            } else {
                AsyncImage(
                    model = mediaUrl,
                    contentDescription = "Post image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .heightIn(max = 450.dp)
                        .clickable { onImageClick(mediaUrl) }
                )
            }
        }

        if (!post.pollQuestion.isNullOrBlank() && !post.pollOptions.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            PollDisplay(
                question = post.pollQuestion,
                options = post.pollOptions,
                voteCounts = post.pollVoteCounts,
                userSelectedOptionIndex = post.userSelectedOptionIndex,
                onVote = { optionIndex -> onVoteClick?.invoke(optionIndex) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (!post.locationName.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            LocationDisplay(
                locationName = post.locationName,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatColumn("${post.likesCount}", "likes")
            StatColumn("${post.commentsCount}", "comments")
            StatColumn("${post.sharesCount}", "shares")
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                IconTextButton(
                    icon = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    text = "Like",
                    tint = if (post.isLikedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    onClick = onLikeClick
                )
                IconTextButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    text = "Comment",
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = onCommentClick
                )
                IconTextButton(
                    icon = if (post.isSharedByMe) Icons.Filled.Repeat else Icons.Outlined.Repeat,
                    text = "Repost",
                    tint = if (post.isSharedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    onClick = onShareClick
                )
            }
            IconButton(onClick = onSaveClick) {
                Icon(
                    imageVector = if (post.isSavedByMe) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (post.isSavedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Editar Publicación", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                OutlinedTextField(
                    value = editContentText,
                    onValueChange = { if (it.length <= 500) editContentText = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEditClick(editContentText)
                    showEditDialog = false
                }) { Text("Guardar", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Eliminar Publicación", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar esta publicación? Esta acción no se puede deshacer.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick()
                    showDeleteDialog = false
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
