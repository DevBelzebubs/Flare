package com.social.flare.features.profile.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.social.flare.features.profile.presentation.components.profile.GuestProfileView
import com.social.flare.features.profile.presentation.components.profile.ProfileContent
import com.social.flare.features.profile.presentation.viewmodel.ProfileUiState
import com.social.flare.features.profile.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    citizenId: String?,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(LocalContext.current)),
    onNavigateToLogin: () -> Unit = {},
    onPostClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(citizenId) {
        if (citizenId != null) {
            viewModel.loadActiveUserProfile(citizenId)
        }
    }
    Scaffold(containerColor = Color.Black) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (citizenId == null) {
                GuestProfileView(onNavigateToLogin)
            } else {
                when (val state = uiState) {
                    is ProfileUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFFF5722))
                    }
                    is ProfileUiState.Success -> {
                        ProfileContent(
                            state = state,
                            userPosts = state.posts,
                            onPostClick = onPostClick
                        )
                    }
                    is ProfileUiState.UserNotFound -> {
                        GuestProfileView(onNavigateToLogin)
                    }
                    is ProfileUiState.Error -> {
                        Text("Error: ${state.message}", color = Color.Red, modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}