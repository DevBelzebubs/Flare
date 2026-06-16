package com.social.flare.features.feed.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.social.flare.features.feed.presentation.components.FullScreenImageDialog
import com.social.flare.features.feed.presentation.components.PostCard
import com.social.flare.features.feed.presentation.components.StoryCarousel

@Composable
fun FeedScreen(
    activeCitizenId: String?,
    viewModel: FeedViewModel = viewModel(),
    onRequireAuth: () -> Unit,
    onPostClick: (String) -> Unit,
    onStoryClick: (String) -> Unit,
    onNavigateToAddStory: () -> Unit,
    onAuthorClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isGuest = activeCitizenId == null
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

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
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        uiState.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            }
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        StoryCarousel(
                            activeUserAvatarUrl = uiState.activeUser?.avatar_url,
                            activeUserUsername = uiState.activeUser?.username,
                            stories = uiState.stories,
                            onAddStoryClick = {
                                requireAuth { onNavigateToAddStory() }
                            },
                            onStoryClick = { username ->
                                requireAuth { onStoryClick(username) }
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                    }
                    if (uiState.posts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "No hay posts por ahora",
                                        color = MaterialTheme.colorScheme.onBackground,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Sigue a otros ciudadanos o sé el primero en publicar.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            items = uiState.posts,
                            key = { post -> post.id }
                        ) { post ->
                            val displayPost = if (isGuest) post.copy(isLikedByMe = false) else post
                            PostCard(
                                post = displayPost,
                                activeCitizenId = activeCitizenId,
                                onEvent = { event ->
                                    when (event) {
                                        is FeedEvent.OnPostClick -> onPostClick(event.postId)
                                        is FeedEvent.OnAuthorClick -> requireAuth {
                                            onAuthorClick(
                                                event.authorId
                                            )
                                        }

                                        else -> requireAuth { viewModel.onEvent(event) }
                                    }
                                },
                                onImageClick = { url ->
                                    fullScreenImageUrl = url
                                }
                            )
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
    }
}
