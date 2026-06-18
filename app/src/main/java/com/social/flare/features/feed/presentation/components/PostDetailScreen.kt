package com.social.flare.features.feed.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.social.flare.features.feed.presentation.components.detailpost.MainPostDetail
import com.social.flare.features.feed.presentation.components.detailpost.ParentPostItem
import com.social.flare.features.feed.presentation.components.detailpost.ReplyInputBar
import com.social.flare.features.post.presentation.PostDetailViewModel
import com.social.flare.features.feed.presentation.components.detailpost.CommentItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    activeCitizenId: String?,
    viewModel: PostDetailViewModel,
    onNavigateBack: () -> Unit,
    onCommentNavigate: (String) -> Unit,
    onAuthorClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

    var replyText by remember { mutableStateOf("") }
    var replyingToPostId by remember { mutableStateOf(postId) }
    var replyingToUsername by remember { mutableStateOf<String?>(null) }

    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedMediaUri = uri
        }
    }

    LaunchedEffect(postId, activeCitizenId) {
        viewModel.loadPostDetail(postId, activeCitizenId ?: "")
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Post",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = { Spacer(modifier = Modifier.width(48.dp)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    windowInsets = WindowInsets(0, 0, 0, 0)
                )
            },
            bottomBar = {
                ReplyInputBar(
                    replyText = replyText,
                    onTextChange = { replyText = it },
                    selectedMediaUri = selectedMediaUri,
                    onMediaSelect = {
                        mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                    },
                    onMediaRemove = { selectedMediaUri = null },
                    replyingToUsername = replyingToUsername,
                    onClearTarget = {
                        replyingToPostId = postId
                        replyingToUsername = null
                    },
                    onSend = {
                        activeCitizenId?.let { userId ->
                            if (replyText.isNotBlank() || selectedMediaUri != null) {
                                val mediaList = selectedMediaUri?.let { listOf(it) } ?: emptyList()
                                viewModel.createReply(userId, replyText, replyingToPostId, mediaList)
                                replyText = ""
                                selectedMediaUri = null
                                replyingToPostId = postId
                                replyingToUsername = null
                            }
                        }
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                uiState.errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text(text = "Error: ${uiState.errorMessage}", color = MaterialTheme.colorScheme.error)
                    }
                }
                uiState.postDetail != null -> {
                    val detail = uiState.postDetail!!

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(paddingValues)
                    ) {
                        if (detail.parentPost != null) {
                            item {
                                ParentPostItem(
                                    post = detail.parentPost,
                                    onImageClick = { url -> fullScreenImageUrl = url },
                                    onBodyClick = { onCommentNavigate(detail.parentPost.id) },
                                    onAuthorClick = onAuthorClick,
                                    onVoteClick = { optionIndex ->
                                        activeCitizenId?.let { viewModel.castVote(detail.mainPost.id, it, optionIndex) }
                                    }
                                )
                            }
                        }
                        item {
                            MainPostDetail(
                                post = detail.mainPost,
                                hasParent = detail.parentPost != null,
                                onImageClick = { url -> fullScreenImageUrl = url },
                                onLikeClick = {
                                    activeCitizenId?.let { viewModel.toggleLike(detail.mainPost.id, it) }
                                },
                                onCommentClick = {
                                    replyingToPostId = detail.mainPost.id
                                    replyingToUsername = detail.mainPost.authorUsername
                                },
                                onSaveClick = {
                                    activeCitizenId?.let { viewModel.toggleSave(detail.mainPost.id, it) }
                                },
                                onShareClick = {
                                    activeCitizenId?.let { viewModel.sharePost(it, detail.mainPost.id) }
                                },
                                onEditClick = { newContent ->
                                    activeCitizenId?.let { viewModel.updatePost(detail.mainPost.id, it, newContent) }
                                },
                                onDeleteClick = {
                                    activeCitizenId?.let { viewModel.deletePost(detail.mainPost.id, it) }
                                },
                                onAuthorClick = onAuthorClick,
                                onVoteClick = { optionIndex ->
                                    activeCitizenId?.let { viewModel.castVote(detail.mainPost.id, it, optionIndex) }
                                },
                                activeUserId = activeCitizenId
                            )
                        }
                        item {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                            Text(
                                text = "Comments (${detail.replies.size})",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                        items(detail.replies, key = { it.id }) { reply ->
                            val isNestedReply = reply.parentPostId != detail.mainPost.id
                            val onLikeClick = remember(reply) { {
                                activeCitizenId?.let { viewModel.toggleLike(reply.id, it) }
                                Unit
                            } }
                            val onReplyClick = remember(reply) { {
                                replyingToPostId = reply.id
                                replyingToUsername = reply.authorUsername
                                Unit
                            } }
                            val onImageClick = remember { { url: String -> fullScreenImageUrl = url } }
                            val onBodyClick = remember(reply) { { onCommentNavigate(reply.id) } }
                            CommentItem(
                                post = reply,
                                isNestedReply = isNestedReply,
                                onLikeClick = onLikeClick,
                                onReplyClick = onReplyClick,
                                onImageClick = onImageClick,
                                onBodyClick = onBodyClick,
                                onAuthorClick = onAuthorClick
                            )

                            if (!isNestedReply) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
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
