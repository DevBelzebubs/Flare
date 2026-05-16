package com.social.flare.features.feed.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send as OutlinedSend
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.post.presentation.PostDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    activeCitizenId: String?,
    viewModel: PostDetailViewModel,
    onNavigateBack: () -> Unit,
    onCommentNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

    var replyText by remember { mutableStateOf("") }
    var replyingToPostId by remember { mutableStateOf(postId) }
    var replyingToUsername by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(postId, activeCitizenId) {
        viewModel.loadPostDetail(postId, activeCitizenId ?: "")
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Post", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = { Spacer(modifier = Modifier.width(48.dp)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
            },
            bottomBar = {
                ReplyInputBar(
                    replyText = replyText,
                    onTextChange = { replyText = it },
                    replyingToUsername = replyingToUsername,
                    onClearTarget = {
                        replyingToPostId = postId
                        replyingToUsername = null
                    },
                    onSend = {
                        activeCitizenId?.let { userId ->
                            if (replyText.isNotBlank()) {
                                viewModel.createReply(userId, replyText, replyingToPostId)
                                replyText = ""
                                replyingToPostId = postId
                                replyingToUsername = null
                            }
                        }
                    }
                )
            },
            containerColor = Color.Black,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFF5722))
                    }
                }
                uiState.errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text(text = "Error: ${uiState.errorMessage}", color = Color.Red)
                    }
                }
                uiState.postDetail != null -> {
                    val detail = uiState.postDetail!!

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(paddingValues)
                    ) {
                        if (detail.parentPost != null) {
                            item {
                                CommentItem(
                                    post = detail.parentPost,
                                    isNestedReply = false,
                                    onLikeClick = { activeCitizenId?.let { viewModel.toggleLike(detail.parentPost.id, it) } },
                                    onReplyClick = {
                                        replyingToPostId = detail.parentPost.id
                                        replyingToUsername = detail.parentPost.authorUsername
                                    },
                                    onImageClick = { url -> fullScreenImageUrl = url },
                                    onBodyClick = { onCommentNavigate(detail.parentPost.id) }
                                )

                                Box(
                                    modifier = Modifier
                                        .padding(start = 34.dp)
                                        .width(2.dp)
                                        .height(16.dp)
                                        .background(Color(0xFF2A2A2A))
                                )
                            }
                        }

                        item {
                            MainPostDetail(
                                post = detail.mainPost,
                                onImageClick = { url -> fullScreenImageUrl = url },
                                onLikeClick = {
                                    activeCitizenId?.let { viewModel.toggleLike(detail.mainPost.id, it) }
                                },
                                onCommentClick = {
                                    replyingToPostId = detail.mainPost.id
                                    replyingToUsername = detail.mainPost.authorUsername
                                }
                            )
                        }

                        // 3. SEPARADOR DE COMENTARIOS
                        item {
                            HorizontalDivider(color = Color(0xFF1A1A1A), thickness = 1.dp)
                            Text(
                                text = "Comments (${detail.replies.size})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }

                        // 4. RESPUESTAS A ESTE POST
                        items(detail.replies) { reply ->
                            val isNestedReply = reply.parentPostId != detail.mainPost.id

                            CommentItem(
                                post = reply,
                                isNestedReply = isNestedReply,
                                onLikeClick = { activeCitizenId?.let { viewModel.toggleLike(reply.id, it) } },
                                onReplyClick = {
                                    replyingToPostId = reply.id
                                    replyingToUsername = reply.authorUsername
                                },
                                onImageClick = { url -> fullScreenImageUrl = url },
                                onBodyClick = { onCommentNavigate(reply.id) } // <-- NAVEGA AL COMENTARIO
                            )

                            if (!isNestedReply) {
                                HorizontalDivider(color = Color(0xFF1A1A1A), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }

        fullScreenImageUrl?.let { url ->
            FullScreenImageDialog(
                imageUrl = url,
                onDismiss = { fullScreenImageUrl = null }
            )
        }
    }
}

@Composable
private fun MainPostDetail(
    post: Post,
    onImageClick: (String) -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = post.authorAvatarUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.DarkGray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(post.authorUsername, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("6h ago", color = Color.Gray, fontSize = 12.sp)
            }
        }

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
                    tint = if (post.isLikedByMe) Color.Red else Color.White,
                    onClick = onLikeClick
                )
                IconTextButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    text = "Comment",
                    tint = Color.White,
                    onClick = onCommentClick
                )
                IconTextButton(
                    icon = Icons.Outlined.OutlinedSend,
                    text = "Share",
                    tint = Color.White,
                    onClick = {}
                )
            }
            Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Save", tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun CommentItem(
    post: Post,
    isNestedReply: Boolean,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onImageClick: (String) -> Unit,
    onBodyClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable { onBodyClick() }
            .padding(
                start = if (isNestedReply) 56.dp else 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            )
    ) {
        if (isNestedReply) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp)
                    .background(Color(0xFF2A2A2A))
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        AsyncImage(
            model = post.authorAvatarUrl,
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(if (isNestedReply) 28.dp else 36.dp).clip(CircleShape).background(Color.DarkGray)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(post.authorUsername, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("• 30 min ago", color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(post.content ?: "", color = Color.White, fontSize = 14.sp, lineHeight = 18.sp)

            if (post.mediaUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = post.mediaUrls.first(),
                    contentDescription = "Comment image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onImageClick(post.mediaUrls.first()) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        // Usamos un Modifier especial sin propagación para no accionar onBodyClick
                        .clickable(onClick = { onLikeClick() })
                        .padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLikedByMe) Color.Red else Color.Gray,
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

@Composable
private fun StatColumn(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
private fun IconTextButton(icon: ImageVector, text: String, tint: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Icon(icon, contentDescription = text, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
fun ReplyInputBar(
    replyText: String,
    onTextChange: (String) -> Unit,
    replyingToUsername: String?,
    onClearTarget: () -> Unit,
    onSend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF121212))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding()
    ) {
        if (replyingToUsername != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = "Replying to ",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = replyingToUsername,
                    color = Color(0xFFFF5722),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear reply target",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .clickable { onClearTarget() }
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = replyText,
                onValueChange = onTextChange,
                placeholder = { Text("Post your reply...", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f),
                maxLines = 4
            )

            IconButton(
                onClick = onSend,
                enabled = replyText.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (replyText.isNotBlank()) Color(0xFFFF5722) else Color.DarkGray
                )
            }
        }
    }
}