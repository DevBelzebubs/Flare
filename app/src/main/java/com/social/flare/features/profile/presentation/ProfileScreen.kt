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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val followStats by viewModel.followStats.collectAsStateWithLifecycle()

    LaunchedEffect(citizenId, activeCitizenId) {
        if (citizenId != null) {
            viewModel.loadProfileData(citizenId, activeCitizenId)
        }
    }

    val isOtherProfile = citizenId != null && citizenId != activeCitizenId

    BackHandler(isOtherProfile) { onNavigateBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (citizenId == null) {
            GuestProfileView(onNavigateToLogin)
        } else {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFFFF5722)
                    )
                }
                is ProfileUiState.Success -> {
                    ProfileContent(
                        state = state,
                        myPosts = state.myPosts,
                        savedPosts = state.savedPosts,
                        sharedPosts = state.sharedPosts,
                        onPostClick = onPostClick
                    )

                    if (isOtherProfile) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, start = 4.dp, end = 16.dp)
                        ) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.align(Alignment.TopStart)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color.White
                                )
                            }

                            if (activeCitizenId != null) {
                                Button(
                                    onClick = { viewModel.toggleFollow(followerId = activeCitizenId, followedId = citizenId) },
                                    modifier = Modifier.align(Alignment.TopEnd),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (followStats.isFollowingByMe) Color.DarkGray else Color(0xFFFF5722)
                                    )
                                ) {
                                    Text(
                                        text = if (followStats.isFollowingByMe) "Siguiendo" else "Seguir",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                is ProfileUiState.UserNotFound -> GuestProfileView(onNavigateToLogin)
                is ProfileUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = Color.Red,
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