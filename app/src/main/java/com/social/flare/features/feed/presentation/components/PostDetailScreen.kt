package com.social.flare.features.feed.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(postId, activeCitizenId) {
        viewModel.loadPostDetail(postId, activeCitizenId ?: "")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
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
                    item {
                        MainPostDetail(post = detail.mainPost)
                    }

                    item {
                        HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
                        Text(
                            text = "Comments (${detail.replies.size})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    items(detail.replies) { reply ->
                        CommentItem(
                            post = reply,
                            isReply = false
                        )
                        HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MainPostDetail(post: Post) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (post.authorAvatarUrl != null) {
                AsyncImage(
                    model = post.authorAvatarUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.DarkGray)
                )
            } else {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFCC5500)))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(post.authorDisplayName, color = Color.White, fontWeight = FontWeight.Bold)
                Text(post.authorUsername, color = Color.Gray, fontSize = 12.sp)
            }
        }

        post.content?.let { content ->
            Text(
                text = content,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (post.mediaUrls.isNotEmpty()) {
            val mediaUrl = post.mediaUrls.first()
            val isVideo = mediaUrl.endsWith(".mp4", ignoreCase = true) ||
                    mediaUrl.endsWith(".webm", ignoreCase = true)
            if (isVideo) {
                VideoPlayer(videoUrl = mediaUrl)
            } else {
                AsyncImage(
                    model = mediaUrl,
                    contentDescription = "Post image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(250.dp).background(Color.DarkGray)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatColumn("${post.likesCount}", "likes")
            StatColumn("${post.commentsCount}", "comments")
            StatColumn("0", "shares")
        }

        HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                IconTextButton(if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, "Like", if (post.isLikedByMe) Color(0xFFFF5722) else Color.White)
                Spacer(modifier = Modifier.width(16.dp))
                IconTextButton(Icons.Outlined.ChatBubbleOutline, "Comment", Color.White)
                Spacer(modifier = Modifier.width(16.dp))
                IconTextButton(Icons.Outlined.Send, "Share", Color.White)
            }
            Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Save", tint = Color.White)
        }
    }
}

@Composable
private fun StatColumn(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
private fun IconTextButton(icon: ImageVector, text: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = text, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
fun CommentItem(post: Post, isReply: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (isReply) 56.dp else 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
    ) {
        if (isReply) {
            Box(modifier = Modifier.width(2.dp).height(40.dp).background(Color.DarkGray).padding(end = 12.dp))
            Spacer(modifier = Modifier.width(12.dp))
        }

        if (post.authorAvatarUrl != null) {
            AsyncImage(
                model = post.authorAvatarUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.DarkGray)
            )
        } else {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(if (isReply) Color(0xFFCC5500) else Color.DarkGray))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(post.authorUsername, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("•", color = Color.Gray, fontSize = 12.sp) // Aquí luego pondremos el tiempo real
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(post.content ?: "", color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Like", tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${post.likesCount}", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.width(16.dp))

                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Reply", tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${post.commentsCount}", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.width(16.dp))
                Text("Reply", color = Color(0xFFFF5722), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}