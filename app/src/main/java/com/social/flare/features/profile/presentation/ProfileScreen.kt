package com.social.flare.features.profile.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.social.flare.features.profile.presentation.components.profile.GuestProfileView
import com.social.flare.features.profile.presentation.components.profile.ProfileContent
import com.social.flare.features.profile.presentation.viewmodel.ProfileUiState
import com.social.flare.features.profile.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    citizenId: String?,
    activeCitizenId: String?,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(LocalContext.current)),
    onNavigateToLogin: () -> Unit = {},
    onPostClick: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToFollowers: ((String) -> Unit)? = null,
    onNavigateToFollowing: ((String) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val followStats by viewModel.followStats.collectAsStateWithLifecycle()
    val isFollowingLoading by viewModel.isFollowingLoading.collectAsStateWithLifecycle()

    val isOtherProfile = citizenId != null && citizenId != activeCitizenId

    BackHandler(isOtherProfile) { onNavigateBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (citizenId == null) {
            GuestProfileView(onNavigateToLogin)
        } else {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is ProfileUiState.Success -> {
                    ProfileContent(
                        state = state,
                        myPosts = state.myPosts,
                        savedPosts = state.savedPosts,
                        sharedPosts = state.sharedPosts,
                        onPostClick = onPostClick,
                        onFollowersClick = { onNavigateToFollowers?.invoke(citizenId) },
                        onFollowingClick = { onNavigateToFollowing?.invoke(citizenId) },
                        isOtherProfile = isOtherProfile,
                        activeCitizenId = activeCitizenId,
                        isFollowingByMe = followStats.isFollowingByMe,
                        isFollowingLoading = isFollowingLoading,
                        followersCount = followStats.followersCount,
                        followingCount = followStats.followingCount,
                        onToggleFollow = {
                            activeCitizenId?.let { follower ->
                                viewModel.toggleFollow(followerId = follower, followedId = citizenId)
                            }
                        }
                    )

                    if (isOtherProfile) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .padding(top = 12.dp, start = 4.dp)
                                .align(Alignment.TopStart)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
                is ProfileUiState.UserNotFound -> GuestProfileView(onNavigateToLogin)
                is ProfileUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        if (isOtherProfile) {
            var dragAccumulator by remember { mutableFloatStateOf(0f) }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(30.dp)
                    .align(Alignment.CenterStart)
                    .pointerInput(onNavigateBack) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragAccumulator = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                dragAccumulator += dragAmount
                                if (dragAccumulator > 150f) {
                                    onNavigateBack()
                                    dragAccumulator = 0f
                                }
                            },
                            onDragEnd = { dragAccumulator = 0f },
                            onDragCancel = { dragAccumulator = 0f }
                        )
                    }
            )
        }
    }
}
