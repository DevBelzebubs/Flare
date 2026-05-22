package com.social.flare.features.notifications.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.social.flare.features.notifications.domain.model.NotificationType
import com.social.flare.features.notifications.presentation.components.NotificationItem
import com.social.flare.features.notifications.presentation.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    activeCitizenId: String?,
    viewModel: NotificationViewModel,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToPost: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(activeCitizenId) {
        if (activeCitizenId != null) {
            viewModel.initialize(activeCitizenId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (activeCitizenId == null) {
                Text("Inicia sesión para ver notificaciones", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                return@Scaffold
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color(0xFFFF5722), modifier = Modifier.align(Alignment.Center))
            } else if (uiState.notifications.isEmpty()) {
                Text("No tienes notificaciones aún", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.notifications,
                        key = { it.id }
                    ) { notification ->

                        NotificationItem(
                            notification = notification,
                            isFollowingBack = false,
                            onClick = {
                                viewModel.onNotificationClick(notification.id)
                                // Navegamos dependiendo del tipo
                                if (notification.type == NotificationType.FOLLOW) {
                                    onNavigateToProfile(notification.actorId)
                                } else if (notification.referencedPostId != null) {
                                    onNavigateToPost(notification.referencedPostId)
                                }
                            },
                            onFollowClick = {
                                viewModel.toggleFollowBack(notification.actorId, isCurrentlyFollowing = false)
                            },
                            onAvatarClick = { authorId ->
                                onNavigateToProfile(authorId)
                            }
                        )
                    }
                }
            }
        }
    }
}