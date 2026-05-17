package com.social.flare.features.profile.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onPostClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val followStats by viewModel.followStats.collectAsStateWithLifecycle() // <-- ESTADO REACTIVO DE SEGUIMIENTO

    LaunchedEffect(citizenId, activeCitizenId) {
        if (citizenId != null) {
            viewModel.loadProfileData(citizenId, activeCitizenId)
        }
    }

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
                    Column(modifier = Modifier.fillMaxSize()) {

                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.TopEnd) {
                            if (citizenId == activeCitizenId) {
                                // Es mi perfil -> Botón ficticio de Editar (o el que ya tengas en ProfileHeaderSection)
                                /* Button(...) { Text("Edit Profile") } */
                            } else if (activeCitizenId != null) {
                                Button(
                                    onClick = { viewModel.toggleFollow(followerId = activeCitizenId, followedId = citizenId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (followStats.isFollowingByMe) Color.DarkGray else Color(0xFFFF5722)
                                    )
                                ) {
                                    Text(
                                        text = if (followStats.isFollowingByMe) "Following" else "Follow",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        ProfileContent(
                            state = state,
                            myPosts = state.myPosts,
                            savedPosts = state.savedPosts,
                            onPostClick = onPostClick
                        )
                    }
                }
                is ProfileUiState.UserNotFound -> {
                    GuestProfileView(onNavigateToLogin)
                }
                is ProfileUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}