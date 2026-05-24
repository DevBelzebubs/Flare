package com.social.flare.features.profile.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.social.flare.features.profile.presentation.components.settings.SettingsDarkModeSelector
import com.social.flare.features.profile.presentation.components.settings.SettingsItem
import com.social.flare.features.profile.presentation.components.settings.SettingsProfileHeader
import com.social.flare.features.profile.presentation.components.settings.SettingsSectionTitle
import com.social.flare.features.profile.presentation.components.settings.SettingsTextSizeSelector
import com.social.flare.features.profile.presentation.components.settings.SettingsToggleItem
import com.social.flare.features.profile.presentation.viewmodel.ProfileUiState
import com.social.flare.features.profile.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    activeCitizenId: String?,
    profileViewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToAdmin: () -> Unit = {}
) {
    LaunchedEffect(activeCitizenId) {
        activeCitizenId?.let { profileViewModel.loadActiveUserProfile(it) }
    }
    val profileState by profileViewModel.uiState.collectAsState()
    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            when (profileState) {
                is ProfileUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFF5722))
                    }
                }

                is ProfileUiState.Success -> {
                    val success = profileState as ProfileUiState.Success
                    val currentCitizen by success.citizen.collectAsState(initial = null)

                    SettingsProfileHeader(
                        avatarUrl = currentCitizen?.avatar_url,
                        displayName = currentCitizen?.display_name,
                        username = currentCitizen?.username,
                        onEditClick = onNavigateToEditProfile
                    )

                    if (currentCitizen?.is_admin == true) {
                        SettingsSectionTitle("ADMIN")
                        SettingsItem(
                            icon = Icons.Default.AdminPanelSettings,
                            title = "Admin Panel",
                            onClick = onNavigateToAdmin
                        )
                    }
                }

                else -> {
                    SettingsProfileHeader(
                        avatarUrl = null,
                        displayName = "Guest User",
                        username = "guest",
                        onEditClick = {}
                    )
                }
            }

            SettingsSectionTitle("ACCOUNT")
            SettingsItem(Icons.Default.Person, "Edit Profile", onClick = onNavigateToEditProfile)
            SettingsItem(Icons.Default.Lock, "Change Password")
            SettingsItem(Icons.Default.Shield, "Privacy Settings")

            SettingsSectionTitle("NOTIFICATIONS")
            SettingsToggleItem(Icons.Default.Notifications, "Push Notifications", true)
            SettingsToggleItem(Icons.Default.Email, "Email Notifications", false)

            SettingsSectionTitle("DISPLAY")
            SettingsDarkModeSelector()
            SettingsTextSizeSelector()

            SettingsSectionTitle("SUPPORT")
            SettingsItem(Icons.Default.Description, "Privacy Policy", isExternal = true)
            SettingsItem(Icons.Default.Assignment, "Terms of Service", isExternal = true)
            SettingsItem(Icons.Default.Help, "Help Center", isExternal = true)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A0000)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF440000))
            ) {
                Text("Log Out", color = Color.Red, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}