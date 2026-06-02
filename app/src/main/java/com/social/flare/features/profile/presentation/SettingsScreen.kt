package com.social.flare.features.profile.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.social.flare.core.data.SettingsManager
import com.social.flare.features.profile.presentation.components.settings.SettingsDarkModeSelector
import com.social.flare.features.profile.presentation.components.settings.SettingsItem
import com.social.flare.features.profile.presentation.components.settings.SettingsProfileHeader
import com.social.flare.features.profile.presentation.components.settings.SettingsSectionTitle
import com.social.flare.features.profile.presentation.components.settings.SettingsTextSizeSelector
import com.social.flare.features.profile.presentation.components.settings.SettingsToggleItem
import com.social.flare.features.profile.presentation.viewmodel.ProfileUiState
import com.social.flare.features.profile.presentation.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    activeCitizenId: String?,
    profileViewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onLogin: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {}
) {
    val isGuest = activeCitizenId == null
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val pushNotificationsEnabled by settingsManager.pushNotificationsEnabledFlow.collectAsState(initial = false)

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        scope.launch {
            settingsManager.setPushNotificationsEnabled(isGranted)
        }
        if (!isGranted) {
            Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(activeCitizenId) {
        if (activeCitizenId != null) {
            profileViewModel.loadActiveUserProfile(activeCitizenId)
        }
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
            if (isGuest) {
                SettingsProfileHeader(
                    avatarUrl = null,
                    displayName = "Guest User",
                    username = "guest",
                    showEditButton = false
                )
            } else {
                when (profileState) {
                    is ProfileUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFFFF5722))
                        }
                    }

                    is ProfileUiState.Success -> {
                        val success = profileState as ProfileUiState.Success
                        val currentCitizen = success.citizen

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
                            displayName = "User",
                            username = "",
                            onEditClick = onNavigateToEditProfile
                        )
                    }
                }
            }

            if (!isGuest) {
                SettingsSectionTitle("ACCOUNT")
                SettingsItem(Icons.Default.Person, "Edit Profile", onClick = onNavigateToEditProfile)
                SettingsItem(Icons.Default.Lock, "Change Password")
                SettingsItem(Icons.Default.Shield, "Privacy Settings")
            }

            SettingsSectionTitle("NOTIFICATIONS")
            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "Push Notifications",
                checked = pushNotificationsEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        val requiresPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        val permissionGranted = !requiresPermission ||
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED

                        if (permissionGranted) {
                            scope.launch {
                                settingsManager.setPushNotificationsEnabled(true)
                            }
                        } else {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        scope.launch {
                            settingsManager.setPushNotificationsEnabled(false)
                        }
                    }
                }
            )
            if (!isGuest) {
                SettingsToggleItem(Icons.Default.Email, "Email Notifications", false)
            }

            SettingsSectionTitle("DISPLAY")
            SettingsDarkModeSelector()
            SettingsTextSizeSelector()

            SettingsSectionTitle("SUPPORT")
            SettingsItem(Icons.Default.Description, "Privacy Policy", isExternal = true)
            SettingsItem(Icons.Default.Assignment, "Terms of Service", isExternal = true)
            SettingsItem(Icons.Default.Help, "Help Center", isExternal = true)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = if (isGuest) onLogin else onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGuest) Color(0xFFFF5722) else Color(0xFF1A0000)
                ),
                shape = RoundedCornerShape(12.dp),
                border = if (isGuest) null else BorderStroke(1.dp, Color(0xFF440000))
            ) {
                Text(
                    text = if (isGuest) "Log In" else "Log Out",
                    color = if (isGuest) Color.White else Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
