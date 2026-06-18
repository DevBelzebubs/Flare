package com.social.flare.features.notifications.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.social.flare.features.notifications.domain.model.NotificationType
import com.social.flare.features.notifications.presentation.components.NotificationItem
import com.social.flare.features.notifications.presentation.components.SuggestedAccountItem
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
                title = {
                    Text(
                        "Notificaciones",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (activeCitizenId == null) {
                Text(
                    "Inicia sesión para ver notificaciones",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
                return@Scaffold
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (uiState.notifications.isEmpty()) {
                        item {
                            Text(
                                "No tienes notificaciones aún",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(
                            items = uiState.notifications,
                            key = { it.id }
                        ) { notification ->
                            val onClick = remember(notification) { {
                                viewModel.onNotificationClick(notification.id)
                                if (notification.type == NotificationType.FOLLOW) {
                                    onNavigateToProfile(notification.actorId)
                                } else if (notification.referencedPostId != null) {
                                    onNavigateToPost(notification.referencedPostId)
                                }
                            } }
                            val onFollowClick = remember(notification) { { viewModel.toggleFollowBack(notification.actorId, isCurrentlyFollowing = false) } }
                            val onAvatarClick = remember { { authorId: String -> onNavigateToProfile(authorId) } }
                            NotificationItem(
                                notification = notification,
                                isFollowingBack = false,
                                onClick = onClick,
                                onFollowClick = onFollowClick,
                                onAvatarClick = onAvatarClick
                            )
                        }
                    }

                    if (uiState.suggestedAccounts.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Suggested accounts",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        items(uiState.suggestedAccounts, key = { it.citizen_id }) { citizen ->
                            val onFollowClick = remember(citizen) { { viewModel.followSuggested(citizen.citizen_id) } }
                            val onAvatarClick = remember(citizen) { { onNavigateToProfile(citizen.citizen_id) } }
                            SuggestedAccountItem(
                                citizen = citizen,
                                isFollowing = citizen.citizen_id in uiState.suggestedFollowedIds,
                                onFollowClick = onFollowClick,
                                onAvatarClick = onAvatarClick
                            )
                        }
                    }
                }
            }
        }
    }
}
