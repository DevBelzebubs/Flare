package com.social.flare.features.feed.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.social.flare.features.feed.presentation.components.PostCard
import com.social.flare.features.feed.presentation.components.StoryCarousel

@Composable
fun FeedScreen(
    activeCitizenId: String?,
    viewModel: FeedViewModel = viewModel(),
    onRequireAuth: () -> Unit,
    onPostClick: (String) -> Unit,
    onStoryClick: (String) -> Unit,
    onNavigateToAddStory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isGuest = activeCitizenId == null
    val requireAuth: (() -> Unit) -> Unit = { action ->
        if (isGuest) {
            onRequireAuth()
        } else {
            action()
        }
    }

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF5722))
            }
        }
        uiState.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${uiState.error}", color = Color.Red)
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    StoryCarousel(
                        activeUserAvatarUrl = uiState.activeUser?.avatar_url,
                        onAddStoryClick = {
                            onNavigateToAddStory()
                        },
                        onStoryClick = { username ->
                            onStoryClick(username)
                        }
                    )
                    HorizontalDivider(color = Color(0xFF1A1A1A), thickness = 1.dp)
                }

                items(uiState.posts) { post ->
                    val displayPost = if (isGuest) post.copy(isLikedByMe = false) else post
                    PostCard(
                        post = displayPost,
                        activeCitizenId = activeCitizenId,
                        onEvent = { event ->
                            if (event is FeedEvent.OnPostClick) {
                                onPostClick(event.postId)
                            } else {
                                requireAuth { viewModel.onEvent(event) }
                            }
                        }
                    )
                }
            }
        }
    }
}